package com.manyl.mod.entity;

import com.manyl.mod.data.ManylSavedData;
import com.manyl.mod.registry.ModItems;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Голем-воительница (женский тип воина):
 * - 75% ХП воина-мужчины (27.0 HP);
 * - Полная женская озвучка каждой реплики и боевой/бытовой реакции;
 * - Романтическая ветка (стадии 0..3), плюшки заботы и спасения;
 * - Ревность при стадии 1+ и наличии >1 девушки в отряде:
 *   при 100% подходит, прячет меч, бьёт рукой на 2 HP, возвращает меч и сбрасывает шкалу до 75%
 *   с нормальным перерывом (чтобы не бить игрока постоянно).
 */
public class ValkyrieGolemEntity extends WarriorGolemEntity {
    public static final int MAX_AFFECTION = 100;
    public static final int MAX_JEALOUSY = 100;
    public static final int CONFESS_THRESHOLD = 60;

    public static final int STAGE_STRANGER_ALLY = 0;
    public static final int STAGE_FRIEND = 1;
    public static final int STAGE_CRUSH = 2;
    public static final int STAGE_BELOVED = 3;

    public static final int ACTION_RECRUIT = 0;
    public static final int ACTION_TALK = 1;
    public static final int ACTION_COMPLIMENT = 2;
    public static final int ACTION_GIFT = 3;
    public static final int ACTION_CONFESS = 4;
    public static final int ACTION_TOGGLE_SIT = 5;
    public static final int ACTION_OPEN_INVENTORY = 6;

    private static final EntityDataAccessor<Integer> DATA_AFFECTION =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ROMANCE_STAGE =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_JEALOUSY =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_VOW_BOUND =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_DAYDREAM_TICKS =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.INT);

    private long lastTalkTick = -1000L;
    private long lastComplimentTick = -1000L;
    private long lastHeartParticleTick = 0L;
    private long lastAttackVoiceTick = -1000L;
    private long lastGreetVoiceTick = -1000L;
    private long lastJealousWarnTick = -1000L;
    private long lastCareHealTick = -1000L;
    private long lastShareFoodTick = -1000L;
    private long lastCampfireEventTick = -20000L;
    private long lastFlowerEventTick = -10000L;
    private int rescueCooldownTicks = 0;
    private int daydreamCooldownTicks = 200;

    // Сценарий удара рукой от ревности со скрытием меча + антиспам-кулдаун
    private int weaponHideTicks = 0;
    private int pendingSlapTicks = 0;
    private int slapCooldownTicks = 0;
    private boolean jealousWarned50 = false;

    public ValkyrieGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public GolemRole getRole() {
        return GolemRole.VALKYRIE;
    }

    @Override
    protected boolean isWeaponHidden() {
        return weaponHideTicks > 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AFFECTION, 0);
        builder.define(DATA_ROMANCE_STAGE, STAGE_STRANGER_ALLY);
        builder.define(DATA_JEALOUSY, 0);
        builder.define(DATA_VOW_BOUND, false);
        builder.define(DATA_DAYDREAM_TICKS, 0);
    }

    public boolean isVowBound() {
        return entityData.get(DATA_VOW_BOUND);
    }

    public void setVowBound(boolean bound) {
        entityData.set(DATA_VOW_BOUND, bound);
    }

    public int getDaydreamTicks() {
        return entityData.get(DATA_DAYDREAM_TICKS);
    }

    public void setDaydreamTicks(int ticks) {
        entityData.set(DATA_DAYDREAM_TICKS, Math.max(0, ticks));
    }

    public boolean isDaydreaming() {
        return getDaydreamTicks() > 0;
    }

    public int getAffection() {
        return entityData.get(DATA_AFFECTION);
    }

    public void setAffection(int value) {
        int clamped = Mth.clamp(value, 0, MAX_AFFECTION);
        entityData.set(DATA_AFFECTION, clamped);
        updateRomanceStageFromAffection();
    }

    public int getRomanceStage() {
        return entityData.get(DATA_ROMANCE_STAGE);
    }

    public void setRomanceStage(int stage) {
        entityData.set(DATA_ROMANCE_STAGE, Mth.clamp(stage, STAGE_STRANGER_ALLY, STAGE_BELOVED));
    }

    public int getJealousy() {
        return entityData.get(DATA_JEALOUSY);
    }

    public void setJealousy(int value) {
        int clamped = Mth.clamp(value, 0, MAX_JEALOUSY);
        entityData.set(DATA_JEALOUSY, clamped);
        if (clamped < 40) {
            jealousWarned50 = false;
        }
    }

    public void addJealousy(int delta) {
        setJealousy(getJealousy() + delta);
    }

    private void updateRomanceStageFromAffection() {
        int currentStage = getRomanceStage();
        if (currentStage >= STAGE_BELOVED) return;
        int aff = getAffection();
        if (aff >= 45) {
            setRomanceStage(STAGE_CRUSH);
        } else if (aff >= 20) {
            setRomanceStage(STAGE_FRIEND);
        } else {
            setRomanceStage(STAGE_STRANGER_ALLY);
        }
    }

    public void addAffection(int delta) {
        setAffection(getAffection() + delta);
    }

    public void playGreetVoice() {
        long now = level().getGameTime();
        if (now - lastGreetVoiceTick >= 60L) {
            lastGreetVoiceTick = now;
            if (!isTamed()) {
                playSound(ModSounds.VALKYRIE_WILD_GREET, 0.80F, 1.0F);
            } else if (getJealousy() >= 70) {
                playSound(ModSounds.VALKYRIE_JEALOUS, 0.80F, 1.0F);
            } else if (getRomanceStage() >= STAGE_BELOVED) {
                playSound(ModSounds.VALKYRIE_LOVE, 0.78F, 1.0F);
            } else {
                playSound(ModSounds.VALKYRIE_GREET, 0.78F, 1.0F);
            }
        }
    }

    public String getGreetingLine(boolean tamed) {
        if (!tamed) {
            return Component.translatable("dialogue.manyl_mod.valkyrie.wild_greeting").getString();
        }
        if (getJealousy() >= 70) {
            return Component.translatable("dialogue.manyl_mod.valkyrie.greet.jealous").getString();
        }
        return switch (getRomanceStage()) {
            case STAGE_BELOVED -> Component.translatable("dialogue.manyl_mod.valkyrie.greet.beloved").getString();
            case STAGE_CRUSH -> Component.translatable("dialogue.manyl_mod.valkyrie.greet.crush").getString();
            case STAGE_FRIEND -> Component.translatable("dialogue.manyl_mod.valkyrie.greet.friend").getString();
            default -> Component.translatable("dialogue.manyl_mod.valkyrie.greet.ally").getString();
        };
    }

    public String handleRomanceAction(ServerPlayer player, int actionId) {
        if (actionId == ACTION_RECRUIT) {
            if (!isTamed()) {
                recruitBy(player);
                if (getAffection() < 10) setAffection(10);
                spawnHeartBurst(10);
                playSound(ModSounds.VALKYRIE_RECRUITED, 0.85F, 1.0F);
                return Component.translatable("dialogue.manyl_mod.valkyrie.recruited").getString();
            }
            return getGreetingLine(true);
        }

        if (!isTamed()) {
            playSound(ModSounds.VALKYRIE_WILD_GREET, 0.78F, 1.0F);
            return Component.translatable("dialogue.manyl_mod.valkyrie.need_recruit").getString();
        }

        long now = level().getGameTime();
        switch (actionId) {
            case ACTION_TALK -> {
                if (now - lastTalkTick >= 200L) {
                    lastTalkTick = now;
                    addAffection(4);
                    addJealousy(-18);
                    spawnHeartBurst(4);
                }
                SoundEvent talkVoice = switch (getRomanceStage()) {
                    case STAGE_BELOVED -> ModSounds.VALKYRIE_TALK_BELOVED;
                    case STAGE_CRUSH -> ModSounds.VALKYRIE_TALK_CRUSH;
                    case STAGE_FRIEND -> ModSounds.VALKYRIE_TALK_FRIEND;
                    default -> ModSounds.VALKYRIE_TALK_ALLY;
                };
                playSound(talkVoice, 0.82F, 1.0F);
                String key = switch (getRomanceStage()) {
                    case STAGE_BELOVED -> "dialogue.manyl_mod.valkyrie.talk.beloved.0";
                    case STAGE_CRUSH -> "dialogue.manyl_mod.valkyrie.talk.crush.0";
                    case STAGE_FRIEND -> "dialogue.manyl_mod.valkyrie.talk.friend.0";
                    default -> "dialogue.manyl_mod.valkyrie.talk.ally.0";
                };
                return Component.translatable(key).getString();
            }
            case ACTION_COMPLIMENT -> {
                if (now - lastComplimentTick >= 240L) {
                    lastComplimentTick = now;
                    addAffection(8);
                    addJealousy(-30);
                    spawnHeartBurst(7);
                }
                playSound(ModSounds.VALKYRIE_LAUGH, 0.82F, 1.0F);
                int variant = random.nextInt(2);
                String key = switch (getRomanceStage()) {
                    case STAGE_BELOVED -> "dialogue.manyl_mod.valkyrie.compliment.beloved." + variant;
                    case STAGE_CRUSH -> "dialogue.manyl_mod.valkyrie.compliment.crush." + variant;
                    default -> "dialogue.manyl_mod.valkyrie.compliment.early." + variant;
                };
                return Component.translatable(key).getString();
            }
            case ACTION_GIFT -> {
                return handleGift(player);
            }
            case ACTION_CONFESS -> {
                if (getRomanceStage() >= STAGE_BELOVED) {
                    spawnHeartBurst(10);
                    playSound(ModSounds.VALKYRIE_LOVE, 0.82F, 1.0F);
                    return Component.translatable("dialogue.manyl_mod.valkyrie.confess_already").getString();
                }
                if (getAffection() < CONFESS_THRESHOLD) {
                    playSound(ModSounds.VALKYRIE_SIGH, 0.72F, 1.0F);
                    return Component.translatable("dialogue.manyl_mod.valkyrie.confess_too_early").getString();
                }
                setRomanceStage(STAGE_BELOVED);
                setJealousy(0);
                if (getAffection() < 80) setAffection(80);
                spawnHeartBurst(18);
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ModParticles.SUMMON_RUNE, getX(), getY() + 1.2D, getZ(), 18, 0.45D, 0.55D, 0.45D, 0.04D);
                    sl.playSound(null, getX(), getY(), getZ(), ModSounds.VALKYRIE_CONFESS, getSoundSource(), 0.88F, 1.0F);
                }
                return Component.translatable("dialogue.manyl_mod.valkyrie.confess_success").getString();
            }
            case ACTION_TOGGLE_SIT -> {
                setSitting(!isSitting());
                playSound(ModSounds.GOLEM_STEP, 0.18F, isSitting() ? 0.85F : 1.25F);
                playSound(isSitting() ? ModSounds.VALKYRIE_SIGH : ModSounds.VALKYRIE_GREET, 0.72F, 1.0F);
                return Component.translatable(
                        isSitting() ? "dialogue.manyl_mod.valkyrie.mode_stay" : "dialogue.manyl_mod.valkyrie.mode_follow"
                ).getString();
            }
            default -> {
                return getGreetingLine(true);
            }
        }
    }

    private String handleGift(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            playSound(ModSounds.VALKYRIE_SIGH, 0.72F, 1.0F);
            return Component.translatable("dialogue.manyl_mod.valkyrie.gift_hint").getString();
        }

        int gain = 0;
        int jealousyDrop = 40;
        String responseKey = null;
        SoundEvent giftSound = ModSounds.VALKYRIE_GIFT_LOVE;

        if (held.is(ModItems.VOW_RING)) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            applyVowRing(player);
            return Component.translatable("dialogue.manyl_mod.valkyrie.gift.vow_ring").getString();
        } else if (held.is(ItemTags.FLOWERS)) {
            gain = 16;
            jealousyDrop = 55;
            responseKey = "dialogue.manyl_mod.valkyrie.gift.flower";
        } else if (held.is(Items.DIAMOND)) {
            gain = 25;
            jealousyDrop = 65;
            responseKey = "dialogue.manyl_mod.valkyrie.gift.diamond";
        } else if (held.is(Items.AMETHYST_SHARD) || held.is(Items.EMERALD)) {
            gain = 18;
            jealousyDrop = 50;
            responseKey = "dialogue.manyl_mod.valkyrie.gift.gem";
        } else if (held.is(Items.GOLD_INGOT)) {
            gain = 14;
            jealousyDrop = 40;
            responseKey = "dialogue.manyl_mod.valkyrie.gift.gold";
        } else if (isSweetGift(held)) {
            gain = 15;
            jealousyDrop = 45;
            responseKey = "dialogue.manyl_mod.valkyrie.gift.sweet";
            giftSound = ModSounds.VALKYRIE_GIFT_SWEET;
            restoreFromFood(held);
        } else {
            FoodProperties food = held.get(DataComponents.FOOD);
            if (food != null) {
                gain = 10;
                jealousyDrop = 35;
                responseKey = "dialogue.manyl_mod.valkyrie.gift.food";
                giftSound = ModSounds.VALKYRIE_GIFT_SWEET;
                restoreFromFood(held);
            }
        }

        if (gain <= 0) {
            playSound(ModSounds.VALKYRIE_SIGH, 0.72F, 1.0F);
            return Component.translatable("dialogue.manyl_mod.valkyrie.gift_hint").getString();
        }

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        addAffection(gain);
        addJealousy(-jealousyDrop);
        heal(4.0F);
        setEatTicks(16);
        playSound(giftSound, 0.82F, 1.0F);
        spawnHeartBurst(12);
        return Component.translatable(responseKey).getString();
    }

    public void applyVowRing(ServerPlayer player) {
        setVowBound(true);
        setRomanceStage(STAGE_BELOVED);
        setAffection(MAX_AFFECTION);
        setJealousy(0);
        heal(getMaxHealth());
        spawnHeartBurst(20);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ModParticles.SUMMON_RUNE, getX(), getY() + 1.2D, getZ(), 22, 0.5D, 0.6D, 0.5D, 0.05D);
            sl.playSound(null, getX(), getY(), getZ(), ModSounds.VALKYRIE_VOW, getSoundSource(), 0.90F, 1.0F);
        }
        player.displayClientMessage(Component.translatable("msg.manyl_mod.vow_ring_accepted"), false);
    }

    private boolean isSweetGift(ItemStack stack) {
        return stack.is(Items.APPLE)
                || stack.is(Items.GOLDEN_APPLE)
                || stack.is(Items.COOKIE)
                || stack.is(Items.CAKE)
                || stack.is(Items.PUMPKIN_PIE)
                || stack.is(Items.SWEET_BERRIES)
                || stack.is(Items.HONEY_BOTTLE);
    }

    private void restoreFromFood(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        int restore = food != null ? Math.max(15, food.nutrition() * 8) : 25;
        setHunger(Math.min(MAX_HUNGER, getHunger() + restore));
    }

    private void spawnHeartBurst(int count) {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.55D, getZ(), count, 0.35D, 0.35D, 0.35D, 0.02D);
            sl.sendParticles(ModParticles.CORE_HEART, getX(), getY() + 1.25D, getZ(), Math.max(3, count / 2), 0.25D, 0.25D, 0.25D, 0.03D);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.VALKYRIE_HURT;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        long now = level.getGameTime();
        if (now - lastAttackVoiceTick >= 45L && random.nextFloat() < 0.60F) {
            lastAttackVoiceTick = now;
            playSound(ModSounds.VALKYRIE_ATTACK, 0.72F, 0.98F + random.nextFloat() * 0.08F);
        }
        return super.doHurtTarget(level, target);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) return;
        if (isDisabled()) {
            setDaydreamTicks(0);
            return;
        }

        if (rescueCooldownTicks > 0) rescueCooldownTicks--;
        if (slapCooldownTicks > 0) slapCooldownTicks--;
        if (daydreamCooldownTicks > 0) daydreamCooldownTicks--;

        // Таймер скрытия меча и возврат меча в руку после шлепка ревности
        if (weaponHideTicks > 0) {
            weaponHideTicks--;
            if (weaponHideTicks == 0) {
                onInventoryChanged();
            }
        }

        // Выполнение лёгкого удара рукой на 2 HP после того, как меч спрятан
        if (pendingSlapTicks > 0) {
            pendingSlapTicks--;
            if (pendingSlapTicks == 0) {
                performJealousSlap();
            }
        }

        if (!isTamed()) {
            setDaydreamTicks(0);
            return;
        }

        ServerLevel sl = (ServerLevel) level();
        LivingEntity owner = getOwnerEntity();

        // 0. Анимация мечтательного снятия шлема и вздоха (игроки в Spectator НЕ прерывают!)
        tickDaydreaming(sl);

        // 1. Плюшки за уровень влюблённости (забота, подпитка и спасение при низком ХП)
        tickRomanceCarePerks(sl, owner);

        // 2. Романтические события: вечер у костра и цветок в пути
        tickRomanticEvents(sl, owner);

        // 3. Ревность (активируется при стадии 1+ и наличии >1 девушки в отряде, блокируется Кольцом Обета)
        tickJealousySystem(sl, owner);

        // 4. Если ревность достигла 100% И прошёл перерыв — подходит, прячет меч и легонько бьёт рукой
        if (getJealousy() >= MAX_JEALOUSY
                && !isVowBound()
                && slapCooldownTicks <= 0
                && pendingSlapTicks == 0
                && owner instanceof ServerPlayer serverOwner
                && serverOwner.isAlive()) {
            double distSq = distanceToSqr(serverOwner);
            if (distSq <= 6.25D) {
                getNavigation().stop();
                getLookControl().setLookAt(serverOwner, 30.0F, 30.0F);
                weaponHideTicks = 34; // прячем меч на ~1.7 сек
                onInventoryChanged(); // убираем меч из руки
                pendingSlapTicks = 6; // через 0.3 сек бьём голой рукой!
            } else if (distSq <= 625.0D && !isSitting()) {
                getLookControl().setLookAt(serverOwner, 30.0F, 30.0F);
                getNavigation().moveTo(serverOwner, 1.20D);
            }
        }
    }

    private void tickDaydreaming(ServerLevel sl) {
        if (getTarget() != null || getAttackTicks() > 0 || hurtTime > 0 || isWorking()) {
            setDaydreamTicks(0);
            return;
        }

        boolean nonSpectatorWatching = false;
        List<? extends Player> players = sl.players();
        for (Player p : players) {
            if (!p.isAlive() || p.isSpectator()) continue; // Spectator НЕ мешает и видит анимацию!
            if (p.distanceToSqr(this) <= 400.0D && isPlayerLookingAtMe(p)) {
                nonSpectatorWatching = true;
                break;
            }
        }

        if (nonSpectatorWatching) {
            if (getDaydreamTicks() > 0) {
                setDaydreamTicks(0);
                daydreamCooldownTicks = 240;
                playSound(ModSounds.VALKYRIE_SIGH, 0.72F, 1.05F);
                sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.85D, getZ(), 2, 0.18D, 0.12D, 0.18D, 0.01D);
            }
            return;
        }

        int current = getDaydreamTicks();
        if (current > 0) {
            setDaydreamTicks(current - 1);
            if (current == 110 || current == 55) {
                sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.95D, getZ(), 1, 0.15D, 0.1D, 0.15D, 0.01D);
            }
        } else if (daydreamCooldownTicks <= 0 && tickCount % 40 == 0 && random.nextFloat() < 0.35F) {
            setDaydreamTicks(160);
            daydreamCooldownTicks = 500;
            playSound(ModSounds.VALKYRIE_DAYDREAM, 0.65F, 1.0F);
        }
    }

    private boolean isPlayerLookingAtMe(Player player) {
        Vec3 view = player.getViewVector(1.0F).normalize();
        Vec3 toEntity = new Vec3(getX() - player.getX(), getEyeY() - player.getEyeY(), getZ() - player.getZ());
        double len = toEntity.length();
        if (len < 0.01D) return true;
        double dot = view.dot(toEntity.scale(1.0D / len));
        return dot > 0.68D && player.hasLineOfSight(this);
    }

    private void tickRomanticEvents(ServerLevel sl, LivingEntity owner) {
        if (!(owner instanceof ServerPlayer sp) || !sp.isAlive() || tickCount % 80 != 0) return;
        double distSq = distanceToSqr(sp);
        if (distSq > 64.0D || getTarget() != null) return;

        long now = sl.getGameTime();
        long dayTime = sl.getDayTime() % 24000L;

        // Вечернее событие у костра (ночь + тёплый костёр рядом)
        if (dayTime >= 12500L && dayTime <= 23500L && now - lastCampfireEventTick >= 12000L) {
            BlockPos campfire = findLitCampfire(5);
            if (campfire != null) {
                lastCampfireEventTick = now;
                getLookControl().setLookAt(sp, 30.0F, 30.0F);
                addAffection(12);
                spawnHeartBurst(10);
                playSound(ModSounds.VALKYRIE_CAMPFIRE, 0.85F, 1.0F);
                sp.displayClientMessage(Component.translatable("msg.manyl_mod.valkyrie_campfire_event"), false);
                return;
            }
        }

        // Дневной романтический подарок: цветок в путешествии (при стадии 1+)
        if (getRomanceStage() >= STAGE_FRIEND && dayTime < 12000L && now - lastFlowerEventTick >= 6000L
                && random.nextFloat() < 0.14F) {
            lastFlowerEventTick = now;
            Item[] flowers = {Items.POPPY, Items.DANDELION, Items.CORNFLOWER, Items.LILY_OF_THE_VALLEY, Items.PINK_TULIP};
            ItemStack flower = new ItemStack(flowers[random.nextInt(flowers.length)]);
            if (!sp.getInventory().add(flower)) {
                sp.drop(flower, false);
            }
            addAffection(8);
            spawnHeartBurst(8);
            playSound(ModSounds.VALKYRIE_LOVE, 0.80F, 1.0F);
            sp.displayClientMessage(Component.translatable("msg.manyl_mod.valkyrie_flower_event"), false);
        }
    }

    private BlockPos findLitCampfire(int radius) {
        BlockPos center = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, 2, radius))) {
            BlockState st = level().getBlockState(pos);
            if ((st.is(Blocks.CAMPFIRE) || st.is(Blocks.SOUL_CAMPFIRE))
                    && st.hasProperty(CampfireBlock.LIT)
                    && st.getValue(CampfireBlock.LIT)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private void tickRomanceCarePerks(ServerLevel sl, LivingEntity owner) {
        int stage = getRomanceStage();
        if (stage <= STAGE_STRANGER_ALLY || owner == null || !owner.isAlive()) return;

        double distSq = distanceToSqr(owner);

        // Стадия 3: самоисцеление, постоянная аура заботы и ЭКСТРЕННОЕ СПАСЕНИЕ при низком ХП игрока
        if (stage >= STAGE_BELOVED) {
            if (tickCount % 100 == 0) {
                heal(1.0F);
                if (distSq <= 100.0D) {
                    owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false, true));
                    owner.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 0, true, false, true));
                }
            }
            if (tickCount - lastHeartParticleTick >= 45 && distSq <= 36.0D) {
                lastHeartParticleTick = tickCount;
                sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.85D, getZ(), 1, 0.2D, 0.15D, 0.2D, 0.01D);
            }

            // Спасение при критическом ХП (<= 6.0 HP = 3 сердечка)
            if (rescueCooldownTicks <= 0 && owner.getHealth() <= 6.0F && distSq <= 324.0D) {
                rescueCooldownTicks = 1800; // 90 секунд перезарядки
                owner.heal(6.0F);
                owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, true, true));
                owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, true, true));
                owner.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, false, true, true));
                sl.sendParticles(ParticleTypes.HEART, owner.getX(), owner.getY() + 1.2D, owner.getZ(), 14, 0.45D, 0.5D, 0.45D, 0.03D);
                sl.sendParticles(ModParticles.SUMMON_RUNE, owner.getX(), owner.getY() + 0.8D, owner.getZ(), 14, 0.4D, 0.4D, 0.4D, 0.04D);
                playSound(ModSounds.VALKYRIE_RESCUE, 0.90F, 1.0F);
                if (owner instanceof ServerPlayer sp) {
                    sp.displayClientMessage(Component.translatable("msg.manyl_mod.valkyrie_rescue"), true);
                }
            }
        } else if (stage >= STAGE_CRUSH) {
            if (tickCount % 120 == 0 && distSq <= 100.0D) {
                owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, false, true));
                owner.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 0, true, false, true));
            }
        }

        // Стадии 1+: подлечивание игрока при падении ХП ниже 50% (раз в 25 сек)
        long now = sl.getGameTime();
        if (distSq <= 196.0D && owner.getHealth() < owner.getMaxHealth() * 0.50F && now - lastCareHealTick >= 500L) {
            lastCareHealTick = now;
            owner.heal(2.0F);
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false, true));
            spawnHeartBurst(5);
            playSound(ModSounds.VALKYRIE_LOVE, 0.72F, 1.0F);
            if (owner instanceof ServerPlayer sp && stage >= STAGE_FRIEND) {
                sp.displayClientMessage(Component.translatable("msg.manyl_mod.valkyrie_care_heal"), true);
            }
        }

        // Стадии 2+: если игрок проголодался (<= 14), заботливо делится припасами (раз в 45 сек)
        if (stage >= STAGE_CRUSH && distSq <= 100.0D && owner instanceof ServerPlayer sp
                && sp.getFoodData().getFoodLevel() <= 14 && now - lastShareFoodTick >= 900L) {
            if (consumeAnyFoodFromInventory() || getHunger() >= 60) {
                lastShareFoodTick = now;
                sp.getFoodData().eat(3, 0.4F);
                if (getHunger() >= 60) setHunger(getHunger() - 10);
                spawnHeartBurst(4);
                playSound(ModSounds.VALKYRIE_GIFT_SWEET, 0.72F, 1.0F);
                sp.displayClientMessage(Component.translatable("msg.manyl_mod.valkyrie_share_food"), true);
            }
        }
    }

    private boolean consumeAnyFoodFromInventory() {
        for (int i = 0; i < CARGO_END; i++) {
            ItemStack s = inventory.getItem(i);
            if (!s.isEmpty() && s.has(DataComponents.FOOD)) {
                s.shrink(1);
                if (s.isEmpty()) inventory.setItem(i, ItemStack.EMPTY);
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }

    private void tickJealousySystem(ServerLevel sl, LivingEntity owner) {
        // Проверка каждые 3 секунды (60 тиков) — спокойный, комфортный ритм без постоянного спама ударами
        if (tickCount % 60 != 0) return;

        if (isVowBound() || getRomanceStage() < STAGE_FRIEND || getOwnerUuid() == null) {
            if (getJealousy() > 0) setJealousy(0);
            return;
        }

        int totalValkyries = ManylSavedData.getValkyrieCount(sl.getServer(), getOwnerUuid());
        if (totalValkyries <= 1) {
            if (getJealousy() > 0) addJealousy(-5);
            return;
        }

        List<ValkyrieGolemEntity> nearbyRivals = sl.getEntitiesOfClass(
                ValkyrieGolemEntity.class,
                getBoundingBox().inflate(18.0D, 6.0D, 18.0D),
                other -> other != this && other.isAlive() && getOwnerUuid().equals(other.getOwnerUuid())
        );

        if (!nearbyRivals.isEmpty()) {
            // +1 каждые 3 сек (~5 минут с 0% до 100%, ~75 сек с 75% до 100%)
            addJealousy(getRomanceStage() >= STAGE_CRUSH ? 2 : 1);
        } else if (tickCount % 240 == 0) {
            addJealousy(1);
        }

        if (getJealousy() >= 50 && !jealousWarned50) {
            jealousWarned50 = true;
            long now = sl.getGameTime();
            if (now - lastJealousWarnTick >= 400L) {
                lastJealousWarnTick = now;
                playSound(ModSounds.VALKYRIE_JEALOUS, 0.78F, 1.0F);
                if (owner instanceof ServerPlayer sp) {
                    sp.displayClientMessage(Component.translatable("msg.manyl_mod.valkyrie_jealous_warn"), true);
                }
            }
        }
    }

    private void performJealousSlap() {
        if (!(level() instanceof ServerLevel sl)) return;
        LivingEntity owner = getOwnerEntity();
        if (!(owner instanceof ServerPlayer sp) || !sp.isAlive() || distanceToSqr(sp) > 9.0D) {
            return;
        }

        swing(InteractionHand.MAIN_HAND);
        setAttackTicks(12);
        sp.hurtServer(sl, damageSources().mobAttack(this), 2.0F);
        sp.knockback(0.25D, getX() - sp.getX(), getZ() - sp.getZ());

        sl.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_WEAK, getSoundSource(), 0.7F, 1.15F);
        playSound(ModSounds.VALKYRIE_JEALOUS, 0.85F, 1.02F);
        sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + 1.85D, getZ(), 5, 0.25D, 0.2D, 0.25D, 0.02D);

        // Шкала ревности падает на 25% (100% -> 75%) + 45 сек спокойствия
        setJealousy(Math.max(0, getJealousy() - 25));
        slapCooldownTicks = 900;
        sp.displayClientMessage(Component.translatable("msg.manyl_mod.valkyrie_jealous_slap"), false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("ValkyrieAffection", getAffection());
        out.putInt("ValkyrieRomanceStage", getRomanceStage());
        out.putInt("ValkyrieJealousy", getJealousy());
        out.putBoolean("ValkyrieVowBound", isVowBound());
        out.putInt("ValkyrieRescueCd", rescueCooldownTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        setVowBound(in.getBooleanOr("ValkyrieVowBound", false));
        setRomanceStage(in.getIntOr("ValkyrieRomanceStage", STAGE_STRANGER_ALLY));
        setAffection(in.getIntOr("ValkyrieAffection", 0));
        setJealousy(isVowBound() ? 0 : in.getIntOr("ValkyrieJealousy", 0));
        this.rescueCooldownTicks = in.getIntOr("ValkyrieRescueCd", 0);
    }
}
