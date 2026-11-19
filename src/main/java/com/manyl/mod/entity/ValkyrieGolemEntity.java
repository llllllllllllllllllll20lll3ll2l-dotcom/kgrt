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
import net.minecraft.tags.FluidTags;
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
    public static final int ACTION_PAT = 7;
    public static final int ACTION_HUG = 8;
    public static final int ACTION_KISS = 9;
    public static final int ACTION_DANCE = 10;
    public static final int ACTION_WEDDING = 11;
    public static final int ACTION_BLOW_KISS = 12;
    public static final int ACTION_HOLD_HANDS = 13;
    public static final int ACTION_SALUTE = 14;
    public static final int ACTION_SHY = 15;
    public static final int ACTION_BOW = 16;

    public static final int ANIM_NONE = 0;
    public static final int ANIM_PAT = 1;
    public static final int ANIM_HUG = 2;
    public static final int ANIM_KISS = 3;
    public static final int ANIM_DANCE = 4;
    public static final int ANIM_WEDDING = 5;
    public static final int ANIM_BLOW_KISS = 6;
    public static final int ANIM_HOLD_HANDS = 7;
    public static final int ANIM_SALUTE = 8;
    public static final int ANIM_SHY = 9;
    public static final int ANIM_SLAP = 10;
    public static final int ANIM_BOW = 11;

    public static final int PHASE_NONE = 0;
    public static final int PHASE_LOOK_AROUND = 1;
    public static final int PHASE_STRIP_ARMOR = 2;
    public static final int PHASE_WALK_TO_WATER = 3;
    public static final int PHASE_BATHING_SIT = 4;
    public static final int PHASE_REST_UNARMORED = 5;

    public record RomanceActionOutcome(String line, boolean closeScreen) {}

    private static final EntityDataAccessor<Integer> DATA_AFFECTION =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ROMANCE_STAGE =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_JEALOUSY =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_VOW_BOUND =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_BATH_PHASE =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BATH_TICKS =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_INTERACTION_ANIM =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_INTERACTION_TICKS =
            SynchedEntityData.defineId(ValkyrieGolemEntity.class, EntityDataSerializers.INT);

    private long lastTalkTick = -1000L;
    private long lastComplimentTick = -1000L;
    private long lastPatTick = -1000L;
    private long lastHugTick = -1000L;
    private long lastKissTick = -1000L;
    private long lastDanceTick = -1000L;
    private long lastBlowKissTick = -1000L;
    private long lastHoldHandsTick = -1000L;
    private long lastSaluteTick = -1000L;
    private long lastShyTick = -1000L;
    private long lastBowTick = -1000L;
    private long lastWeddingBuffTick = -1000L;
    private long lastHeartParticleTick = 0L;
    private long lastAttackVoiceTick = -1000L;
    private long lastHurtVoiceTick = -1000L;
    private long lastGreetVoiceTick = -1000L;
    private long lastJealousWarnTick = -1000L;
    private long lastCareHealTick = -1000L;
    private long lastShareFoodTick = -1000L;
    private long lastCampfireEventTick = -20000L;
    private long lastFlowerEventTick = -10000L;
    private int rescueCooldownTicks = 0;
    private int daydreamCooldownTicks = 180;
    private BlockPos targetWaterPos = null;
    private int prevInteractionTicks = 0;
    private int lastObservedClientTicks = 0;

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
        return weaponHideTicks > 0 || getBathPhase() >= PHASE_STRIP_ARMOR || getInteractionAnim() >= ANIM_HUG;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AFFECTION, 0);
        builder.define(DATA_ROMANCE_STAGE, STAGE_STRANGER_ALLY);
        builder.define(DATA_JEALOUSY, 0);
        builder.define(DATA_VOW_BOUND, false);
        builder.define(DATA_BATH_PHASE, PHASE_NONE);
        builder.define(DATA_BATH_TICKS, 0);
        builder.define(DATA_INTERACTION_ANIM, ANIM_NONE);
        builder.define(DATA_INTERACTION_TICKS, 0);
    }

    public boolean isVowBound() {
        return entityData.get(DATA_VOW_BOUND);
    }

    public void setVowBound(boolean bound) {
        entityData.set(DATA_VOW_BOUND, bound);
    }

    public int getBathPhase() {
        return entityData.get(DATA_BATH_PHASE);
    }

    public void setBathPhase(int phase) {
        entityData.set(DATA_BATH_PHASE, phase);
        onInventoryChanged();
    }

    public int getBathTicks() {
        return entityData.get(DATA_BATH_TICKS);
    }

    public void setBathTicks(int ticks) {
        entityData.set(DATA_BATH_TICKS, Math.max(0, ticks));
    }

    public int getInteractionAnim() {
        return entityData.get(DATA_INTERACTION_ANIM);
    }

    public void setInteractionAnim(int anim) {
        entityData.set(DATA_INTERACTION_ANIM, anim);
        onInventoryChanged();
    }

    public int getInteractionTicks() {
        return entityData.get(DATA_INTERACTION_TICKS);
    }

    public void setInteractionTicks(int ticks) {
        entityData.set(DATA_INTERACTION_TICKS, Math.max(0, ticks));
    }

    public int getPrevInteractionTicks() {
        return prevInteractionTicks;
    }

    @Override
    public void tick() {
        if (level().isClientSide()) {
            int cur = getInteractionTicks();
            if (getInteractionAnim() == ANIM_NONE) {
                this.prevInteractionTicks = 0;
                this.lastObservedClientTicks = 0;
            } else {
                this.prevInteractionTicks = this.lastObservedClientTicks;
                this.lastObservedClientTicks = cur;
            }
        }
        super.tick();
    }

    @Override
    public boolean isBusyWithRoutine() {
        return getBathPhase() != PHASE_NONE || getInteractionAnim() != ANIM_NONE;
    }

    public boolean isInSwimsuit() {
        int p = getBathPhase();
        return p == PHASE_WALK_TO_WATER || p == PHASE_BATHING_SIT || p == PHASE_REST_UNARMORED
                || (p == PHASE_STRIP_ARMOR && getBathTicks() >= 45);
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
        if (now - lastGreetVoiceTick >= 20L) {
            lastGreetVoiceTick = now;
            if (!isTamed()) {
                playVoice(ModSounds.VALKYRIE_WILD_GREET, 0.80F, 1.0F);
            } else if (getJealousy() >= 70) {
                playVoice(ModSounds.VALKYRIE_JEALOUS, 0.80F, 1.0F);
            } else if (getRomanceStage() >= STAGE_BELOVED) {
                playVoice(ModSounds.VALKYRIE_LOVE, 0.78F, 1.0F);
            } else {
                playVoice(ModSounds.VALKYRIE_GREET, 0.78F, 1.0F);
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
        return handleRomanceActionOutcome(player, actionId).line();
    }

    public RomanceActionOutcome handleRomanceActionOutcome(ServerPlayer player, int actionId) {
        if (actionId == ACTION_RECRUIT) {
            if (!isTamed()) {
                recruitBy(player);
                if (getAffection() < 10) setAffection(10);
                spawnHeartBurst(10);
                playVoice(ModSounds.VALKYRIE_RECRUITED, 0.85F, 1.0F);
                return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.recruited").getString(), false);
            }
            return new RomanceActionOutcome(getGreetingLine(true), false);
        }

        if (!isTamed()) {
            playVoice(ModSounds.VALKYRIE_WILD_GREET, 0.78F, 1.0F);
            return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.need_recruit").getString(), false);
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
                playVoice(talkVoice, 0.82F, 1.0F);
                String key = switch (getRomanceStage()) {
                    case STAGE_BELOVED -> "dialogue.manyl_mod.valkyrie.talk.beloved.0";
                    case STAGE_CRUSH -> "dialogue.manyl_mod.valkyrie.talk.crush.0";
                    case STAGE_FRIEND -> "dialogue.manyl_mod.valkyrie.talk.friend.0";
                    default -> "dialogue.manyl_mod.valkyrie.talk.ally.0";
                };
                return new RomanceActionOutcome(Component.translatable(key).getString(), false);
            }
            case ACTION_COMPLIMENT -> {
                if (now - lastComplimentTick >= 240L) {
                    lastComplimentTick = now;
                    addAffection(8);
                    addJealousy(-30);
                    spawnHeartBurst(7);
                }
                playVoice(ModSounds.VALKYRIE_LAUGH, 0.82F, 1.0F);
                int variant = random.nextInt(2);
                String key = switch (getRomanceStage()) {
                    case STAGE_BELOVED -> "dialogue.manyl_mod.valkyrie.compliment.beloved." + variant;
                    case STAGE_CRUSH -> "dialogue.manyl_mod.valkyrie.compliment.crush." + variant;
                    default -> "dialogue.manyl_mod.valkyrie.compliment.early." + variant;
                };
                return new RomanceActionOutcome(Component.translatable(key).getString(), false);
            }
            case ACTION_GIFT -> {
                return handleGiftOutcome(player);
            }
            case ACTION_CONFESS -> {
                if (getRomanceStage() >= STAGE_BELOVED) {
                    spawnHeartBurst(10);
                    playVoice(ModSounds.VALKYRIE_LOVE, 0.82F, 1.0F);
                    return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.confess_already").getString(), false);
                }
                if (getAffection() < CONFESS_THRESHOLD) {
                    playVoice(ModSounds.VALKYRIE_SIGH, 0.72F, 1.0F);
                    return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.confess_too_early").getString(), false);
                }
                setRomanceStage(STAGE_BELOVED);
                setJealousy(0);
                if (getAffection() < 80) setAffection(80);
                spawnHeartBurst(18);
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ModParticles.SUMMON_RUNE, getX(), getY() + 1.2D, getZ(), 18, 0.45D, 0.55D, 0.45D, 0.04D);
                }
                playVoice(ModSounds.VALKYRIE_CONFESS, 0.88F, 1.0F);
                return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.confess_success").getString(), false);
            }
            case ACTION_PAT -> {
                if (now - lastPatTick >= 160L) {
                    lastPatTick = now;
                    addAffection(5);
                    addJealousy(-15);
                }
                startInteractionAnim(ANIM_PAT, player);
                playVoice(ModSounds.VALKYRIE_LAUGH, 0.82F, 1.03F);
                spawnHeartBurst(6);
                String key = getRomanceStage() >= STAGE_CRUSH
                        ? "dialogue.manyl_mod.valkyrie.anim.pat.warm"
                        : "dialogue.manyl_mod.valkyrie.anim.pat.early";
                return new RomanceActionOutcome(Component.translatable(key).getString(), true);
            }
            case ACTION_HUG -> {
                if (getRomanceStage() < STAGE_FRIEND) {
                    playVoice(ModSounds.VALKYRIE_SIGH, 0.72F, 1.0F);
                    return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.hug.too_early").getString(), false);
                }
                if (now - lastHugTick >= 240L) {
                    lastHugTick = now;
                    addAffection(7);
                    addJealousy(-25);
                    heal(2.0F);
                    player.heal(2.0F);
                }
                startInteractionAnim(ANIM_HUG, player);
                playVoice(ModSounds.VALKYRIE_LOVE, 0.85F, 1.0F);
                spawnHeartBurst(10);
                String key = getRomanceStage() >= STAGE_BELOVED
                        ? "dialogue.manyl_mod.valkyrie.anim.hug.beloved"
                        : "dialogue.manyl_mod.valkyrie.anim.hug.friend";
                return new RomanceActionOutcome(Component.translatable(key).getString(), true);
            }
            case ACTION_KISS -> {
                if (getRomanceStage() < STAGE_CRUSH) {
                    playVoice(ModSounds.VALKYRIE_SIGH, 0.74F, 1.04F);
                    return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.kiss.too_early").getString(), false);
                }
                if (now - lastKissTick >= 300L) {
                    lastKissTick = now;
                    addAffection(10);
                    addJealousy(-35);
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, true, true));
                }
                startInteractionAnim(ANIM_KISS, player);
                playVoice(ModSounds.VALKYRIE_LOVE, 0.86F, 1.04F);
                spawnHeartBurst(14);
                String key = getRomanceStage() >= STAGE_BELOVED
                        ? "dialogue.manyl_mod.valkyrie.anim.kiss.beloved"
                        : "dialogue.manyl_mod.valkyrie.anim.kiss.crush";
                return new RomanceActionOutcome(Component.translatable(key).getString(), true);
            }
            case ACTION_DANCE -> {
                if (now - lastDanceTick >= 240L) {
                    lastDanceTick = now;
                    addAffection(6);
                    addJealousy(-20);
                }
                startInteractionAnim(ANIM_DANCE, player);
                playVoice(ModSounds.VALKYRIE_LAUGH, 0.86F, 1.02F);
                spawnHeartBurst(8);
                return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.dance").getString(), true);
            }
            case ACTION_WEDDING -> {
                if (isVowBound()) {
                    if (now - lastWeddingBuffTick >= 600L) {
                        lastWeddingBuffTick = now;
                        heal(getMaxHealth());
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 0, true, true, true));
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0, true, true, true));
                    }
                    startInteractionAnim(ANIM_WEDDING, player);
                    playVoice(ModSounds.VALKYRIE_VOW, 0.90F, 1.0F);
                    return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.wedding_renew").getString(), true);
                }
                if (consumeVowRingFromPlayer(player)) {
                    applyVowRing(player);
                    return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.wedding_start").getString(), true);
                }
                playVoice(ModSounds.VALKYRIE_SIGH, 0.74F, 1.0F);
                return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.wedding_need_ring").getString(), false);
            }
            case ACTION_BLOW_KISS -> {
                if (getRomanceStage() < STAGE_FRIEND) {
                    playVoice(ModSounds.VALKYRIE_SIGH, 0.72F, 1.0F);
                    return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.blow_kiss.too_early").getString(), false);
                }
                if (now - lastBlowKissTick >= 200L) {
                    lastBlowKissTick = now;
                    addAffection(6);
                    addJealousy(-20);
                }
                startInteractionAnim(ANIM_BLOW_KISS, player);
                playVoice(ModSounds.VALKYRIE_LAUGH, 0.85F, 1.05F);
                spawnHeartBurst(8);
                return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.blow_kiss").getString(), true);
            }
            case ACTION_HOLD_HANDS -> {
                if (getRomanceStage() < STAGE_FRIEND) {
                    playVoice(ModSounds.VALKYRIE_SIGH, 0.72F, 1.0F);
                    return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.hold_hands.too_early").getString(), false);
                }
                if (now - lastHoldHandsTick >= 200L) {
                    lastHoldHandsTick = now;
                    addAffection(6);
                    addJealousy(-20);
                }
                startInteractionAnim(ANIM_HOLD_HANDS, player);
                playVoice(ModSounds.VALKYRIE_LOVE, 0.82F, 1.02F);
                spawnHeartBurst(7);
                return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.hold_hands").getString(), true);
            }
            case ACTION_SALUTE -> {
                if (now - lastSaluteTick >= 160L) {
                    lastSaluteTick = now;
                    addAffection(3);
                }
                startInteractionAnim(ANIM_SALUTE, player);
                playVoice(ModSounds.VALKYRIE_GREET, 0.85F, 1.02F);
                return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.salute").getString(), true);
            }
            case ACTION_SHY -> {
                if (now - lastShyTick >= 180L) {
                    lastShyTick = now;
                    addAffection(4);
                    addJealousy(-10);
                }
                startInteractionAnim(ANIM_SHY, player);
                playVoice(ModSounds.VALKYRIE_DAYDREAM, 0.78F, 1.04F);
                spawnHeartBurst(5);
                return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.shy").getString(), true);
            }
            case ACTION_BOW -> {
                if (now - lastBowTick >= 180L) {
                    lastBowTick = now;
                    addAffection(4);
                }
                startInteractionAnim(ANIM_BOW, player);
                playVoice(ModSounds.VALKYRIE_GREET, 0.82F, 0.98F);
                spawnHeartBurst(4);
                return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.anim.bow").getString(), true);
            }
            case ACTION_TOGGLE_SIT -> {
                setSitting(!isSitting());
                playSound(ModSounds.GOLEM_STEP, 0.18F, isSitting() ? 0.85F : 1.25F);
                playVoice(isSitting() ? ModSounds.VALKYRIE_SIGH : ModSounds.VALKYRIE_GREET, 0.72F, 1.0F);
                return new RomanceActionOutcome(Component.translatable(
                        isSitting() ? "dialogue.manyl_mod.valkyrie.mode_stay" : "dialogue.manyl_mod.valkyrie.mode_follow"
                ).getString(), false);
            }
            default -> {
                return new RomanceActionOutcome(getGreetingLine(true), false);
            }
        }
    }

    private boolean consumeVowRingFromPlayer(ServerPlayer player) {
        if (player.getMainHandItem().is(ModItems.VOW_RING)) {
            if (!player.getAbilities().instabuild) {
                player.getMainHandItem().shrink(1);
            }
            return true;
        }
        if (player.getOffhandItem().is(ModItems.VOW_RING)) {
            if (!player.getAbilities().instabuild) {
                player.getOffhandItem().shrink(1);
            }
            return true;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack st = player.getInventory().getItem(i);
            if (st.is(ModItems.VOW_RING)) {
                if (!player.getAbilities().instabuild) {
                    st.shrink(1);
                }
                return true;
            }
        }
        return false;
    }

    private RomanceActionOutcome handleGiftOutcome(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            playVoice(ModSounds.VALKYRIE_SIGH, 0.72F, 1.0F);
            return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.gift_hint").getString(), false);
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
            return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.gift.vow_ring").getString(), true);
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
            playVoice(ModSounds.VALKYRIE_SIGH, 0.72F, 1.0F);
            return new RomanceActionOutcome(Component.translatable("dialogue.manyl_mod.valkyrie.gift_hint").getString(), false);
        }

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        addAffection(gain);
        addJealousy(-jealousyDrop);
        heal(4.0F);
        setEatTicks(16);
        playVoice(giftSound, 0.82F, 1.0F);
        spawnHeartBurst(12);
        return new RomanceActionOutcome(Component.translatable(responseKey).getString(), false);
    }

    public void startInteractionAnim(int animType, ServerPlayer player) {
        if (level() instanceof ServerLevel sl && getBathPhase() != PHASE_NONE) {
            cancelBathRoutine(sl, false);
        }
        setInteractionAnim(animType);
        setInteractionTicks(0);
        getNavigation().stop();
        if (player != null && player.isAlive()) {
            faceEntity(player);
            if ((animType == ANIM_HUG || animType == ANIM_KISS) && distanceToSqr(player) > 2.2D && distanceToSqr(player) <= 36.0D) {
                getNavigation().moveTo(player, 1.15D);
            }
        }
    }

    private void faceEntity(LivingEntity target) {
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        setYRot(yaw);
        setYHeadRot(yaw);
        setYBodyRot(yaw);
        getLookControl().setLookAt(target, 45.0F, 35.0F);
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
        }
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 0, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 2400, 0, true, true, true));
        startInteractionAnim(ANIM_WEDDING, player);
        playVoice(ModSounds.VALKYRIE_VOW, 0.90F, 1.0F);
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
        return ModSounds.GOLEM_HURT;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && !isDisabled()) {
            long now = level.getGameTime();
            if (now - lastHurtVoiceTick >= 25L) {
                lastHurtVoiceTick = now;
                playVoice(ModSounds.VALKYRIE_HURT, 0.75F, 1.0F);
            }
            if (getBathPhase() != PHASE_NONE) {
                cancelBathRoutine(level, false);
            }
            if (getInteractionAnim() != ANIM_NONE) {
                setInteractionAnim(ANIM_NONE);
                setInteractionTicks(0);
            }
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        long now = level.getGameTime();
        if (now - lastAttackVoiceTick >= 45L && random.nextFloat() < 0.60F) {
            lastAttackVoiceTick = now;
            playVoice(ModSounds.VALKYRIE_ATTACK, 0.72F, 0.98F + random.nextFloat() * 0.08F);
        }
        return super.doHurtTarget(level, target);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) return;
        if (isDisabled()) {
            if (getBathPhase() != PHASE_NONE) {
                setBathPhase(PHASE_NONE);
                setBathTicks(0);
            }
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
            if (getBathPhase() != PHASE_NONE) {
                setBathPhase(PHASE_NONE);
                setBathTicks(0);
            }
            return;
        }

        ServerLevel sl = (ServerLevel) level();
        LivingEntity owner = getOwnerEntity();

        // 0a. Анимации взаимодействия (погладить, объятие, поцелуй, танец, свадьба)
        tickInteractionAnimation(sl, owner);

        // 0. Переработанная анимация: оглядеться -> снять броню (купальник) -> зайти и сесть в водоём 3x3+
        //    Игроки в Spectator НЕ прерывают и НЕ блокируют анимацию!
        if (getInteractionAnim() == ANIM_NONE) {
            tickBathAndArmorRoutine(sl);
        }

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
            if (getBathPhase() != PHASE_NONE) {
                cancelBathRoutine(sl, false);
            }
            double distSq = distanceToSqr(serverOwner);
            if (distSq <= 6.25D) {
                getNavigation().stop();
                getLookControl().setLookAt(serverOwner, 30.0F, 30.0F);
                weaponHideTicks = 34;
                onInventoryChanged();
                pendingSlapTicks = 6;
            } else if (distSq <= 625.0D && !isSitting()) {
                getLookControl().setLookAt(serverOwner, 30.0F, 30.0F);
                getNavigation().moveTo(serverOwner, 1.20D);
            }
        }
    }

    private void tickInteractionAnimation(ServerLevel sl, LivingEntity owner) {
        int anim = getInteractionAnim();
        if (anim == ANIM_NONE) return;

        if (getTarget() != null || hurtTime > 0) {
            setInteractionAnim(ANIM_NONE);
            setInteractionTicks(0);
            return;
        }

        int t = getInteractionTicks() + 1;
        setInteractionTicks(t);

        int duration = switch (anim) {
            case ANIM_PAT -> 45;
            case ANIM_HUG -> 65;
            case ANIM_KISS -> 75;
            case ANIM_DANCE -> 100;
            case ANIM_WEDDING -> 130;
            case ANIM_BLOW_KISS -> 60;
            case ANIM_HOLD_HANDS -> 70;
            case ANIM_SALUTE -> 55;
            case ANIM_SHY -> 65;
            case ANIM_SLAP -> 36;
            case ANIM_BOW -> 70;
            default -> 0;
        };

        if (owner != null && owner.isAlive()) {
            if ((anim == ANIM_HUG || anim == ANIM_KISS) && t <= 14 && distanceToSqr(owner) > 2.8D && distanceToSqr(owner) <= 36.0D) {
                getNavigation().moveTo(owner, 1.15D);
            } else {
                getNavigation().stop();
            }
            if (anim != ANIM_DANCE && !(anim == ANIM_WEDDING && t >= 72 && t <= 112)) {
                faceEntity(owner);
            }
        } else {
            getNavigation().stop();
        }

        switch (anim) {
            case ANIM_PAT -> {
                if (t == 8 || t == 18 || t == 28 || t == 38) {
                    sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 2.05D, getZ(), 2, 0.22D, 0.15D, 0.22D, 0.02D);
                    sl.sendParticles(ModParticles.CORE_HEART, getX(), getY() + 1.85D, getZ(), 2, 0.2D, 0.12D, 0.2D, 0.02D);
                }
            }
            case ANIM_HUG -> {
                if (t == 20 || t == 32 || t == 44) {
                    double mx = owner != null ? (getX() + owner.getX()) * 0.5D : getX();
                    double mz = owner != null ? (getZ() + owner.getZ()) * 0.5D : getZ();
                    sl.sendParticles(ParticleTypes.HEART, mx, getY() + 1.45D, mz, 5, 0.35D, 0.3D, 0.35D, 0.03D);
                    sl.sendParticles(ModParticles.CORE_HEART, mx, getY() + 1.25D, mz, 3, 0.25D, 0.25D, 0.25D, 0.03D);
                }
            }
            case ANIM_KISS -> {
                if (t == 24 || t == 36 || t == 48) {
                    double mx = owner != null ? (getX() * 0.45D + owner.getX() * 0.55D) : getX();
                    double mz = owner != null ? (getZ() * 0.45D + owner.getZ() * 0.55D) : getZ();
                    sl.sendParticles(ParticleTypes.HEART, mx, getY() + 1.75D, mz, 7, 0.25D, 0.25D, 0.25D, 0.03D);
                    sl.sendParticles(ModParticles.CORE_HEART, mx, getY() + 1.65D, mz, 4, 0.2D, 0.2D, 0.2D, 0.04D);
                }
                if (t == 36) {
                    playVoice(ModSounds.VALKYRIE_LAUGH, 0.72F, 1.08F);
                }
            }
            case ANIM_DANCE -> {
                if (t % 12 == 0) {
                    sl.sendParticles(ParticleTypes.NOTE, getX(), getY() + 2.15D, getZ(), 2, 0.35D, 0.15D, 0.35D, 0.5D);
                    sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.75D, getZ(), 2, 0.35D, 0.2D, 0.35D, 0.02D);
                }
            }
            case ANIM_WEDDING -> {
                if (t % 8 == 0) {
                    double angle = (t * 0.45D);
                    double rx = getX() + Math.cos(angle) * 0.85D;
                    double rz = getZ() + Math.sin(angle) * 0.85D;
                    sl.sendParticles(ParticleTypes.END_ROD, rx, getY() + 0.5D + (t / 130.0D) * 1.6D, rz, 2, 0.05D, 0.05D, 0.05D, 0.02D);
                    sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.85D, getZ(), 2, 0.45D, 0.25D, 0.45D, 0.02D);
                }
                if (t == 54) {
                    sl.playSound(null, getX(), getY(), getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, getSoundSource(), 0.75F, 1.15F);
                    sl.sendParticles(ModParticles.SUMMON_RUNE, getX(), getY() + 1.2D, getZ(), 20, 0.55D, 0.65D, 0.55D, 0.05D);
                    sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.7D, getZ(), 16, 0.5D, 0.45D, 0.5D, 0.04D);
                }
                if (t == 76) {
                    playVoice(ModSounds.VALKYRIE_CONFESS, 0.86F, 1.02F);
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getY() + 1.5D, getZ(), 28, 0.55D, 0.7D, 0.55D, 0.18D);
                }
            }
            case ANIM_BLOW_KISS -> {
                if (t == 28 || t == 36 || t == 44) {
                    double px = owner != null ? Mth.lerp((t - 24) / 24.0D, getX(), owner.getX()) : getX();
                    double pz = owner != null ? Mth.lerp((t - 24) / 24.0D, getZ(), owner.getZ()) : getZ();
                    sl.sendParticles(ParticleTypes.HEART, px, getY() + 1.65D, pz, 4, 0.15D, 0.15D, 0.15D, 0.02D);
                    sl.sendParticles(ModParticles.CORE_HEART, px, getY() + 1.55D, pz, 3, 0.12D, 0.12D, 0.12D, 0.02D);
                }
            }
            case ANIM_HOLD_HANDS -> {
                if (t % 14 == 0) {
                    double mx = owner != null ? (getX() + owner.getX()) * 0.5D : getX();
                    double mz = owner != null ? (getZ() + owner.getZ()) * 0.5D : getZ();
                    sl.sendParticles(ParticleTypes.HEART, mx, getY() + 1.15D, mz, 2, 0.2D, 0.15D, 0.2D, 0.01D);
                }
            }
            case ANIM_SALUTE -> {
                if (t == 12) {
                    sl.sendParticles(ModParticles.SUMMON_RUNE, getX(), getY() + 1.4D, getZ(), 8, 0.35D, 0.35D, 0.35D, 0.02D);
                }
            }
            case ANIM_SHY -> {
                if (t == 15 || t == 35 || t == 50) {
                    sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.85D, getZ(), 2, 0.22D, 0.12D, 0.22D, 0.01D);
                }
            }
            case ANIM_BOW -> {
                if (t == 24 || t == 48) {
                    sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.2D, getZ(), 3, 0.3D, 0.2D, 0.3D, 0.01D);
                }
            }
        }

        if (t >= duration) {
            setInteractionAnim(ANIM_NONE);
            setInteractionTicks(0);
        }
    }

    private void tickBathAndArmorRoutine(ServerLevel sl) {
        if (getTarget() != null || getAttackTicks() > 0 || hurtTime > 0 || isWorking()) {
            if (getBathPhase() != PHASE_NONE) cancelBathRoutine(sl, false);
            return;
        }

        // Проверяем только не-Spectator игроков (Spectator не засчитывается за смотрящего!)
        boolean nonSpectatorWatching = false;
        for (Player p : sl.players()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            if (p.distanceToSqr(this) <= 400.0D && isPlayerLookingAtMe(p)) {
                nonSpectatorWatching = true;
                break;
            }
        }

        if (nonSpectatorWatching) {
            if (getBathPhase() != PHASE_NONE) {
                cancelBathRoutine(sl, true);
            }
            return;
        }

        int phase = getBathPhase();
        int ticks = getBathTicks() + 1;

        switch (phase) {
            case PHASE_NONE -> {
                if (!isSitting() && daydreamCooldownTicks <= 0 && tickCount % 40 == 0 && random.nextFloat() < 0.45F) {
                    targetWaterPos = findWaterPoolCenter(10);
                    setBathPhase(PHASE_LOOK_AROUND);
                    setBathTicks(0);
                    getNavigation().stop();
                }
            }
            case PHASE_LOOK_AROUND -> {
                getNavigation().stop();
                setBathTicks(ticks);
                if (ticks >= 40) { // Огляделась по сторонам (2 сек) -> снимает броню
                    setBathPhase(PHASE_STRIP_ARMOR);
                    setBathTicks(0);
                    playVoice(ModSounds.VALKYRIE_DAYDREAM, 0.65F, 1.0F);
                }
            }
            case PHASE_STRIP_ARMOR -> {
                getNavigation().stop();
                setBathTicks(ticks);
                if (ticks >= 60) { // Броня снята (одета в скромный закрытый купальник)
                    if (targetWaterPos != null && isValid3x3WaterPool(targetWaterPos)) {
                        setBathPhase(PHASE_WALK_TO_WATER);
                        setBathTicks(0);
                    } else {
                        setBathPhase(PHASE_REST_UNARMORED);
                        setBathTicks(0);
                    }
                }
            }
            case PHASE_WALK_TO_WATER -> {
                setBathTicks(ticks);
                if (isInWater()) {
                    Vec3 dm = getDeltaMovement();
                    if (dm.y < 0.0D) {
                        setDeltaMovement(dm.x, Math.max(dm.y, 0.02D), dm.z);
                    }
                }
                if (targetWaterPos == null || !isValid3x3WaterPool(targetWaterPos) || ticks > 160) {
                    setBathPhase(PHASE_REST_UNARMORED);
                    setBathTicks(0);
                    break;
                }
                double dx = targetWaterPos.getX() + 0.5D - getX();
                double dz = targetWaterPos.getZ() + 0.5D - getZ();
                double distHorizSq = dx * dx + dz * dz;
                if (distHorizSq <= 1.6D || (isInWater() && distHorizSq <= 3.2D)) {
                    getNavigation().stop();
                    setBathPhase(PHASE_BATHING_SIT);
                    setBathTicks(0);
                    playVoice(ModSounds.VALKYRIE_LAUGH, 0.72F, 1.0F);
                    sl.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.3D, getZ(), 10, 0.3D, 0.15D, 0.3D, 0.05D);
                } else {
                    getNavigation().moveTo(targetWaterPos.getX() + 0.5D, targetWaterPos.getY(), targetWaterPos.getZ() + 0.5D, 1.05D);
                }
            }
            case PHASE_BATHING_SIT -> {
                getNavigation().stop();
                setBathTicks(ticks);
                if (isInWater()) {
                    // Мягкая плавучесть, чтобы во время сидения в воде не тонула на дно
                    boolean waterAbove = level().getFluidState(blockPosition().above()).is(FluidTags.WATER);
                    Vec3 dm = getDeltaMovement();
                    double targetVy = waterAbove ? 0.05D : Math.max(dm.y, -0.005D);
                    setDeltaMovement(dm.x * 0.45D, targetVy, dm.z * 0.45D);
                }
                if (ticks % 20 == 0) {
                    sl.sendParticles(ParticleTypes.BUBBLE, getX(), getY() + 0.45D, getZ(), 3, 0.25D, 0.1D, 0.25D, 0.02D);
                    sl.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.55D, getZ(), 2, 0.30D, 0.05D, 0.30D, 0.02D);
                }
                if (ticks % 90 == 0) {
                    sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.6D, getZ(), 2, 0.2D, 0.15D, 0.2D, 0.01D);
                }
                if (ticks >= 300) { // Накупалась (15 сек) -> спокойно одевается
                    cancelBathRoutine(sl, false);
                }
            }
            case PHASE_REST_UNARMORED -> {
                getNavigation().stop();
                setBathTicks(ticks);
                if (ticks % 60 == 0) {
                    sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.9D, getZ(), 1, 0.15D, 0.1D, 0.15D, 0.01D);
                }
                if (ticks >= 160) {
                    cancelBathRoutine(sl, false);
                }
            }
        }
    }

    private void cancelBathRoutine(ServerLevel sl, boolean shySpotted) {
        setBathPhase(PHASE_NONE);
        setBathTicks(0);
        targetWaterPos = null;
        daydreamCooldownTicks = shySpotted ? 260 : 500;
        if (shySpotted) {
            playVoice(ModSounds.VALKYRIE_SIGH, 0.72F, 1.05F);
            sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.85D, getZ(), 2, 0.18D, 0.12D, 0.18D, 0.01D);
            setInteractionAnim(ANIM_SHY);
            setInteractionTicks(0);
        }
    }

    private BlockPos findWaterPoolCenter(int radius) {
        BlockPos origin = blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -3; dy <= 2; dy++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (isValid3x3WaterPool(candidate)) {
                        double d = candidate.distSqr(origin);
                        if (d < bestDist) {
                            bestDist = d;
                            best = candidate.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }

    private boolean isValid3x3WaterPool(BlockPos center) {
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                BlockPos p = center.offset(ox, 0, oz);
                if (!level().getFluidState(p).is(FluidTags.WATER)) return false;
                if (!level().isEmptyBlock(p.above()) && !level().getFluidState(p.above()).is(FluidTags.WATER)) return false;
            }
        }
        return true;
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
                playVoice(ModSounds.VALKYRIE_CAMPFIRE, 0.85F, 1.0F);
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
            playVoice(ModSounds.VALKYRIE_LOVE, 0.80F, 1.0F);
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
                playVoice(ModSounds.VALKYRIE_RESCUE, 0.90F, 1.0F);
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
            playVoice(ModSounds.VALKYRIE_LOVE, 0.72F, 1.0F);
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
                playVoice(ModSounds.VALKYRIE_GIFT_SWEET, 0.72F, 1.0F);
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
                playVoice(ModSounds.VALKYRIE_JEALOUS, 0.78F, 1.0F);
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
        startInteractionAnim(ANIM_SLAP, sp);
        sp.hurtServer(sl, damageSources().mobAttack(this), 2.0F);
        sp.knockback(0.25D, getX() - sp.getX(), getZ() - sp.getZ());

        sl.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_WEAK, getSoundSource(), 0.7F, 1.15F);
        playVoice(ModSounds.VALKYRIE_JEALOUS, 0.85F, 1.02F);
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
