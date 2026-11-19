package com.manyl.mod.entity;

import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Голем-воительница (женский тип воина) с тёплой, нежной романтической веткой отношений,
 * подарками, диалогами и заботой о своём избраннике.
 */
public class ValkyrieGolemEntity extends WarriorGolemEntity {
    public static final int MAX_AFFECTION = 100;
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

    private long lastTalkTick = -1000L;
    private long lastComplimentTick = -1000L;
    private long lastHeartParticleTick = 0L;

    public ValkyrieGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public GolemRole getRole() {
        return GolemRole.VALKYRIE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AFFECTION, 0);
        builder.define(DATA_ROMANCE_STAGE, STAGE_STRANGER_ALLY);
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

    public String getGreetingLine(boolean tamed) {
        if (!tamed) {
            return Component.translatable("dialogue.manyl_mod.valkyrie.wild_greeting").getString();
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
                return Component.translatable("dialogue.manyl_mod.valkyrie.recruited").getString();
            }
            return getGreetingLine(true);
        }

        if (!isTamed()) {
            return Component.translatable("dialogue.manyl_mod.valkyrie.need_recruit").getString();
        }

        long now = level().getGameTime();
        switch (actionId) {
            case ACTION_TALK -> {
                if (now - lastTalkTick >= 200L) {
                    lastTalkTick = now;
                    addAffection(4);
                    spawnHeartBurst(4);
                }
                int variant = random.nextInt(3);
                String key = switch (getRomanceStage()) {
                    case STAGE_BELOVED -> "dialogue.manyl_mod.valkyrie.talk.beloved." + variant;
                    case STAGE_CRUSH -> "dialogue.manyl_mod.valkyrie.talk.crush." + variant;
                    case STAGE_FRIEND -> "dialogue.manyl_mod.valkyrie.talk.friend." + variant;
                    default -> "dialogue.manyl_mod.valkyrie.talk.ally." + variant;
                };
                return Component.translatable(key).getString();
            }
            case ACTION_COMPLIMENT -> {
                if (now - lastComplimentTick >= 300L) {
                    lastComplimentTick = now;
                    addAffection(8);
                    spawnHeartBurst(7);
                    playSound(ModSounds.GOLEM_EAT, 0.35F, 1.35F);
                }
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
                    return Component.translatable("dialogue.manyl_mod.valkyrie.confess_already").getString();
                }
                if (getAffection() < CONFESS_THRESHOLD) {
                    return Component.translatable("dialogue.manyl_mod.valkyrie.confess_too_early").getString();
                }
                setRomanceStage(STAGE_BELOVED);
                if (getAffection() < 80) setAffection(80);
                spawnHeartBurst(18);
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ModParticles.SUMMON_RUNE, getX(), getY() + 1.2D, getZ(), 18, 0.45D, 0.55D, 0.45D, 0.04D);
                    sl.playSound(null, getX(), getY(), getZ(), ModSounds.GOLEM_SUMMON, getSoundSource(), 0.7F, 1.45F);
                }
                return Component.translatable("dialogue.manyl_mod.valkyrie.confess_success").getString();
            }
            case ACTION_TOGGLE_SIT -> {
                setSitting(!isSitting());
                playSound(ModSounds.GOLEM_STEP, 0.18F, isSitting() ? 0.85F : 1.25F);
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
            return Component.translatable("dialogue.manyl_mod.valkyrie.gift_hint").getString();
        }

        int gain = 0;
        String responseKey = null;

        if (held.is(ItemTags.FLOWERS)) {
            gain = 16;
            responseKey = "dialogue.manyl_mod.valkyrie.gift.flower";
        } else if (held.is(Items.DIAMOND)) {
            gain = 25;
            responseKey = "dialogue.manyl_mod.valkyrie.gift.diamond";
        } else if (held.is(Items.AMETHYST_SHARD) || held.is(Items.EMERALD)) {
            gain = 18;
            responseKey = "dialogue.manyl_mod.valkyrie.gift.gem";
        } else if (held.is(Items.GOLD_INGOT)) {
            gain = 14;
            responseKey = "dialogue.manyl_mod.valkyrie.gift.gold";
        } else if (isSweetGift(held)) {
            gain = 15;
            responseKey = "dialogue.manyl_mod.valkyrie.gift.sweet";
            restoreFromFood(held);
        } else {
            FoodProperties food = held.get(DataComponents.FOOD);
            if (food != null) {
                gain = 10;
                responseKey = "dialogue.manyl_mod.valkyrie.gift.food";
                restoreFromFood(held);
            }
        }

        if (gain <= 0) {
            return Component.translatable("dialogue.manyl_mod.valkyrie.gift_hint").getString();
        }

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        addAffection(gain);
        heal(4.0F);
        setEatTicks(16);
        playSound(ModSounds.GOLEM_EAT, 0.45F, 1.25F);
        spawnHeartBurst(12);
        return Component.translatable(responseKey).getString();
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
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) return;

        // Аура заботы и нежности для Возлюбленной (стадия 3)
        if (getRomanceStage() >= STAGE_BELOVED && isTamed()) {
            if (tickCount % 100 == 0) {
                heal(1.0F);
                LivingEntity owner = getOwnerEntity();
                if (owner != null && owner.isAlive() && distanceToSqr(owner) <= 81.0D) {
                    owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 90, 0, true, false, true));
                }
            }
            if (tickCount - lastHeartParticleTick >= 45 && level() instanceof ServerLevel sl) {
                lastHeartParticleTick = tickCount;
                LivingEntity owner = getOwnerEntity();
                if (owner != null && distanceToSqr(owner) <= 36.0D) {
                    sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1.85D, getZ(), 1, 0.2D, 0.15D, 0.2D, 0.01D);
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("ValkyrieAffection", getAffection());
        out.putInt("ValkyrieRomanceStage", getRomanceStage());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        setRomanceStage(in.getIntOr("ValkyrieRomanceStage", STAGE_STRANGER_ALLY));
        setAffection(in.getIntOr("ValkyrieAffection", 0));
    }
}
