package com.manyl.mod.entity;

import com.manyl.mod.data.ManylSavedData;
import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.menu.GolemInventoryMenu;
import com.manyl.mod.network.ModNetworking;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Базовый класс всех прислужников-големов Manyl_mod (2 блока высотой).
 */
public abstract class BaseGolemEntity extends PathfinderMob {
    public static final int MAX_HUNGER = 100;
    public static final int FOOD_SLOT = 0;
    public static final int CARGO_END = 7; // слоты 1..6: груз и оружие
    public static final int ARMOR_HEAD_SLOT = 7;
    public static final int ARMOR_CHEST_SLOT = 8;
    public static final int ARMOR_LEGS_SLOT = 9;
    public static final int ARMOR_FEET_SLOT = 10;
    public static final int SHIELD_SLOT = 11;
    public static final int INVENTORY_SIZE = 12; // 1 еда + 6 груз + 4 броня + 1 щит

    private static final EntityDataAccessor<Integer> DATA_HUNGER =
            SynchedEntityData.defineId(BaseGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_WORKING =
            SynchedEntityData.defineId(BaseGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CARRYING =
            SynchedEntityData.defineId(BaseGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SITTING =
            SynchedEntityData.defineId(BaseGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SUMMON_TICKS =
            SynchedEntityData.defineId(BaseGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EAT_TICKS =
            SynchedEntityData.defineId(BaseGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TICKS =
            SynchedEntityData.defineId(BaseGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DATA_DISPLAY_ITEM =
            SynchedEntityData.defineId(BaseGolemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_DISABLED =
            SynchedEntityData.defineId(BaseGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_KILLS =
            SynchedEntityData.defineId(BaseGolemEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState workAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState carryAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState summonAnimationState = new AnimationState();
    public final AnimationState eatAnimationState = new AnimationState();

    protected final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);

    private UUID ownerUuid;
    private BlockPos homePos;
    private int hungerTickCounter = 0;
    private int starveTickCounter = 0;
    private int hungrySoundCooldown = 0;
    private int repairProgress = 0;

    protected BaseGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.inventory.addListener(container -> {
            updateCarryingFlag();
            syncArmorAndShieldSlots();
            onInventoryChanged();
        });
    }

    public abstract GolemRole getRole();

    protected void onInventoryChanged() {
        // Переопределяется подклассами (например, WarriorGolemEntity для экипировки оружия)
    }

    protected void syncArmorAndShieldSlots() {
        if (level().isClientSide()) return;
        this.setItemSlot(EquipmentSlot.HEAD, inventory.getItem(ARMOR_HEAD_SLOT).copy());
        this.setItemSlot(EquipmentSlot.CHEST, inventory.getItem(ARMOR_CHEST_SLOT).copy());
        this.setItemSlot(EquipmentSlot.LEGS, inventory.getItem(ARMOR_LEGS_SLOT).copy());
        this.setItemSlot(EquipmentSlot.FEET, inventory.getItem(ARMOR_FEET_SLOT).copy());
        this.setItemSlot(EquipmentSlot.OFFHAND, inventory.getItem(SHIELD_SLOT).copy());
    }

    public static AttributeSupplier.Builder createGolemAttributes(GolemRole role) {
        return createMobAttributes()
                .add(Attributes.MAX_HEALTH, role.getMaxHealth())
                .add(Attributes.MOVEMENT_SPEED, role.getMoveSpeed())
                .add(Attributes.ATTACK_DAMAGE, role.getAttackDamage())
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HUNGER, MAX_HUNGER);
        builder.define(DATA_WORKING, false);
        builder.define(DATA_CARRYING, false);
        builder.define(DATA_SITTING, false);
        builder.define(DATA_SUMMON_TICKS, 24);
        builder.define(DATA_EAT_TICKS, 0);
        builder.define(DATA_ATTACK_TICKS, 0);
        builder.define(DATA_DISPLAY_ITEM, ItemStack.EMPTY);
        builder.define(DATA_DISABLED, false);
        builder.define(DATA_KILLS, 0);
    }

    public static boolean checkGuardianSpawnRules(
            EntityType<? extends BaseGolemEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random) {
        if (reason == EntitySpawnReason.SPAWNER || reason == EntitySpawnReason.SPAWN_ITEM_USE) {
            return true;
        }
        return level.getBlockState(pos.below()).isSolidRender()
                && level.getRawBrightness(pos, 0) >= 7
                && level.canSeeSky(pos);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new PickupFoodAndToolGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.85D) {
            @Override
            public boolean canUse() {
                if (isDisabled() || isSitting()) return false;
                if (getOwnerUuid() != null && getOwnerSettings().isFollowOwner()) return false;
                return super.canUse();
            }
        });
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F) {
            @Override
            public boolean canUse() {
                return !isDisabled() && super.canUse();
            }
        });
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !isDisabled() && super.canUse();
            }
        });
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !isDisabled() && getOwnerUuid() == null && distanceToClosestPlayer > 128.0D;
    }

    public boolean isTamed() {
        return getOwnerUuid() != null;
    }

    public void recruitBy(ServerPlayer player) {
        setDisabled(false);
        setOwnerUuid(player.getUUID());
        setSitting(false);
        if (getHomePos() == null) {
            setHomePos(blockPosition());
        }
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ModParticles.SUMMON_RUNE, getX(), getY() + 0.9D, getZ(), 16, 0.35D, 0.45D, 0.35D, 0.04D);
            sl.sendParticles(ModParticles.CORE_HEART, getX(), getY() + 1.4D, getZ(), 8, 0.3D, 0.3D, 0.3D, 0.03D);
            if (!(this instanceof ValkyrieGolemEntity)) {
                sl.playSound(null, getX(), getY(), getZ(), ModSounds.WARRIOR_GREET, getSoundSource(), 0.82F, 1.0F);
            } else {
                sl.playSound(null, getX(), getY(), getZ(), ModSounds.GOLEM_SUMMON, getSoundSource(), 0.75F, 1.18F);
            }
        }
        player.displayClientMessage(Component.translatable("msg.manyl_mod.recruited",
                Component.translatable(getRole().getTranslationKey())), true);
        ModNetworking.sendStateSync(player);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            updateClientAnimations();
            return;
        }

        if (isDisabled()) {
            getNavigation().stop();
            setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
            if (tickCount % 25 == 0 && level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        getX(), getY() + 1.1D, getZ(), 3, 0.25D, 0.3D, 0.25D, 0.02D);
            }
            return;
        }

        if (getSummonTicks() > 0) setSummonTicks(getSummonTicks() - 1);
        if (getEatTicks() > 0) setEatTicks(getEatTicks() - 1);
        if (getAttackTicks() > 0) setAttackTicks(getAttackTicks() - 1);

        if (isTamed()) {
            int drainThreshold = isWorking() ? 160 : 260;
            hungerTickCounter++;
            if (hungerTickCounter >= drainThreshold) {
                hungerTickCounter = 0;
                int current = getHunger();
                if (current > 0) {
                    setHunger(current - 1);
                }
            }
        } else {
            hungerTickCounter = 0;
            starveTickCounter = 0;
            if (getHunger() < MAX_HUNGER) {
                setHunger(MAX_HUNGER);
            }
        }

        if (tickCount % 5 == 0) {
            pickupNearbyFoodAndTools(1.65D);
        }

        if (getHunger() <= 55 && tickCount % 20 == 0) {
            tryEatFromInventory();
        }

        if (isTamed() && getHunger() > 0 && getHunger() <= 20) {
            if (hungrySoundCooldown <= 0) {
                playSound(ModSounds.GOLEM_HUNGRY, 0.35F, 1.0F);
                hungrySoundCooldown = 140;
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ModParticles.GOLEM_SPARK,
                            getX(), getY() + 0.75D, getZ(), 3, 0.2D, 0.15D, 0.2D, 0.02D);
                }
            } else {
                hungrySoundCooldown--;
            }
        }

        if (isTamed() && getHunger() <= 0) {
            starveTickCounter++;
            if (starveTickCounter >= 40) {
                starveTickCounter = 0;
                if (level() instanceof ServerLevel serverLevel) {
                    hurtServer(serverLevel, damageSources().starve(), 2.0F);
                }
            }
        } else {
            starveTickCounter = 0;
        }
    }

    private void updateClientAnimations() {
        idleAnimationState.startIfStopped(tickCount);

        if (getSummonTicks() > 0) summonAnimationState.startIfStopped(tickCount);
        else summonAnimationState.stop();

        if (getEatTicks() > 0) eatAnimationState.startIfStopped(tickCount);
        else eatAnimationState.stop();

        if (getAttackTicks() > 0) attackAnimationState.startIfStopped(tickCount);
        else attackAnimationState.stop();

        if (isWorking()) workAnimationState.startIfStopped(tickCount);
        else workAnimationState.stop();

        if (isCarrying() && !isWorking() && getAttackTicks() <= 0) carryAnimationState.startIfStopped(tickCount);
        else carryAnimationState.stop();

        if (isSitting()) sitAnimationState.startIfStopped(tickCount);
        else sitAnimationState.stop();
    }

    public boolean tryEatFromInventory() {
        ItemStack stack = inventory.getItem(FOOD_SLOT);
        if (stack.isEmpty()) {
            for (int i = 1; i < CARGO_END; i++) {
                ItemStack s = inventory.getItem(i);
                if (!s.isEmpty() && isFoodItem(s)) {
                    inventory.setItem(FOOD_SLOT, s.copy());
                    inventory.setItem(i, ItemStack.EMPTY);
                    inventory.setChanged();
                    stack = inventory.getItem(FOOD_SLOT);
                    break;
                }
            }
        }
        if (stack.isEmpty()) return false;
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) return false;
        if (getHunger() >= MAX_HUNGER && getHealth() >= getMaxHealth()) return false;

        int restore = Math.max(12, food.nutrition() * 8);
        setHunger(Math.min(MAX_HUNGER, getHunger() + restore));
        heal(Math.max(2.0F, food.nutrition() * 0.6F));
        stack.shrink(1);
        inventory.setChanged();

        setEatTicks(18);
        playSound(ModSounds.GOLEM_EAT, 0.45F, 0.95F + random.nextFloat() * 0.15F);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ModParticles.CORE_HEART,
                    getX(), getY() + 0.7D, getZ(), 5, 0.25D, 0.25D, 0.25D, 0.03D);
        }
        return true;
    }

    public ItemStack addToCargo(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack remaining = stack.copy();

        for (int i = 1; i < CARGO_END; i++) {
            ItemStack slot = inventory.getItem(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, remaining)) {
                int space = Math.min(slot.getMaxStackSize(), inventory.getMaxStackSize()) - slot.getCount();
                if (space > 0) {
                    int move = Math.min(space, remaining.getCount());
                    slot.grow(move);
                    remaining.shrink(move);
                    inventory.setChanged();
                    if (remaining.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }
        for (int i = 1; i < CARGO_END; i++) {
            if (inventory.getItem(i).isEmpty()) {
                inventory.setItem(i, remaining.copy());
                inventory.setChanged();
                return ItemStack.EMPTY;
            }
        }
        return remaining;
    }

    protected boolean isProtectedGearSlot(int slot) {
        return false;
    }

    public static boolean isFoodItem(ItemStack stack) {
        return !stack.isEmpty() && stack.has(DataComponents.FOOD);
    }

    public static boolean isToolOrWeaponItem(ItemStack stack) {
        if (stack.isEmpty() || stack.has(DataComponents.FOOD)) return false;
        if (stack.has(DataComponents.TOOL)) return true;
        if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES)) {
            return true;
        }
        if (stack.is(Items.BOW) || stack.is(Items.CROSSBOW) || stack.is(Items.TRIDENT)
                || stack.is(Items.SHIELD) || stack.is(Items.MACE) || stack.is(Items.SHEARS)
                || stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FISHING_ROD) || stack.is(Items.BRUSH)) {
            return true;
        }
        if (stack.isDamageableItem()) {
            ItemAttributeModifiers mods = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            for (ItemAttributeModifiers.Entry entry : mods.modifiers()) {
                if (entry.attribute().equals(Attributes.ATTACK_DAMAGE) && entry.modifier().amount() > 0.0D) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canAcceptFoodOrTool(ItemStack stack) {
        if (isFoodItem(stack)) {
            ItemStack foodSlot = inventory.getItem(FOOD_SLOT);
            if (foodSlot.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(foodSlot, stack)
                    && foodSlot.getCount() < Math.min(foodSlot.getMaxStackSize(), inventory.getMaxStackSize())) {
                return true;
            }
            return canFitInCargo(stack);
        }
        if (isToolOrWeaponItem(stack)) {
            return canFitInCargo(stack);
        }
        return false;
    }

    private boolean canFitInCargo(ItemStack stack) {
        for (int i = 1; i < CARGO_END; i++) {
            ItemStack slot = inventory.getItem(i);
            if (slot.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(slot, stack)
                    && slot.getCount() < Math.min(slot.getMaxStackSize(), inventory.getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }

    public ItemStack pickupFoodOrTool(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack remaining = stack.copy();

        if (isFoodItem(remaining)) {
            ItemStack slot0 = inventory.getItem(FOOD_SLOT);
            if (slot0.isEmpty()) {
                inventory.setItem(FOOD_SLOT, remaining.copy());
                inventory.setChanged();
                return ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameComponents(slot0, remaining)) {
                int space = Math.min(slot0.getMaxStackSize(), inventory.getMaxStackSize()) - slot0.getCount();
                if (space > 0) {
                    int move = Math.min(space, remaining.getCount());
                    slot0.grow(move);
                    remaining.shrink(move);
                    inventory.setChanged();
                    if (remaining.isEmpty()) return ItemStack.EMPTY;
                }
            }
            return addToCargo(remaining);
        }

        if (isToolOrWeaponItem(remaining)) {
            return addToCargo(remaining);
        }
        return remaining;
    }

    public void pickupNearbyFoodAndTools(double radius) {
        if (level().isClientSide() || !isAlive()) return;
        List<ItemEntity> items = level().getEntitiesOfClass(
                ItemEntity.class,
                getBoundingBox().inflate(radius, 1.2D, radius),
                item -> item.isAlive() && !item.hasPickUpDelay()
                        && (isFoodItem(item.getItem()) || isToolOrWeaponItem(item.getItem()))
        );
        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getItem();
            if (!canAcceptFoodOrTool(stack)) continue;
            int before = stack.getCount();
            ItemStack leftover = pickupFoodOrTool(stack);
            int taken = before - leftover.getCount();
            if (taken > 0) {
                take(itemEntity, taken);
                playSound(SoundEvents.ITEM_PICKUP, 0.25F, 1.0F + random.nextFloat() * 0.2F);
                if (leftover.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(leftover);
                }
            }
        }
    }

    public boolean hasCargo() {
        for (int i = 1; i < CARGO_END; i++) {
            if (isProtectedGearSlot(i)) continue;
            if (!inventory.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    public boolean isCargoFull() {
        for (int i = 1; i < CARGO_END; i++) {
            ItemStack s = inventory.getItem(i);
            if (s.isEmpty() || s.getCount() < s.getMaxStackSize()) return false;
        }
        return true;
    }

    private void updateCarryingFlag() {
        if (!level().isClientSide()) {
            setCarrying(hasCargo());
        }
    }

    public boolean depositCargoIntoContainer(Container target) {
        boolean movedAny = false;
        for (int i = 1; i < CARGO_END; i++) {
            if (isProtectedGearSlot(i)) continue;
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;

            for (int j = 0; j < target.getContainerSize(); j++) {
                ItemStack dest = target.getItem(j);
                if (!dest.isEmpty() && ItemStack.isSameItemSameComponents(dest, stack)) {
                    int space = Math.min(dest.getMaxStackSize(), target.getMaxStackSize()) - dest.getCount();
                    if (space > 0) {
                        int move = Math.min(space, stack.getCount());
                        dest.grow(move);
                        stack.shrink(move);
                        movedAny = true;
                        if (stack.isEmpty()) break;
                    }
                }
            }
            if (!stack.isEmpty()) {
                for (int j = 0; j < target.getContainerSize(); j++) {
                    if (target.getItem(j).isEmpty() && target.canPlaceItem(j, stack)) {
                        target.setItem(j, stack.copy());
                        stack.setCount(0);
                        movedAny = true;
                        break;
                    }
                }
            }
            if (stack.isEmpty()) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        if (movedAny) {
            target.setChanged();
            inventory.setChanged();
        }
        return movedAny;
    }

    public BlockPos findNearestContainerPos(int radius) {
        if (homePos != null && level().getBlockEntity(homePos) instanceof Container) {
            return homePos;
        }
        BlockPos origin = blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -2, -radius), origin.offset(radius, 2, radius))) {
            BlockEntity be = level().getBlockEntity(pos);
            if (be instanceof Container) {
                double d = pos.distSqr(origin);
                if (d < bestDist) {
                    bestDist = d;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (isDisabled()) {
                return handleDisabledInteract(serverPlayer, held);
            }

            if (held.is(Items.NAME_TAG) && held.has(DataComponents.CUSTOM_NAME)) {
                setCustomName(held.get(DataComponents.CUSTOM_NAME));
                setCustomNameVisible(true);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                playSound(SoundEvents.ANVIL_USE, 0.4F, 1.4F);
                return InteractionResult.SUCCESS;
            }

            if (this instanceof ValkyrieGolemEntity valkyrie) {
                if (isTamed() && held.is(ModItems.VOW_RING)) {
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                    valkyrie.applyVowRing(serverPlayer);
                    return InteractionResult.SUCCESS;
                }
                if (isTamed() && player.isShiftKeyDown()) {
                    openGolemMenu(player);
                    return InteractionResult.SUCCESS;
                }
                ModNetworking.sendOpenValkyrieDialogue(serverPlayer, valkyrie);
                return InteractionResult.SUCCESS;
            }

            if (!isTamed()) {
                recruitBy(serverPlayer);
                return InteractionResult.SUCCESS;
            }

            if (player.isShiftKeyDown()) {
                setSitting(!isSitting());
                playSound(ModSounds.GOLEM_STEP, 0.16F, isSitting() ? 0.8F : 1.2F);
                playSound(ModSounds.WARRIOR_ORDER, 0.72F, 1.0F);
                serverPlayer.displayClientMessage(Component.translatable(
                        isSitting() ? "msg.manyl_mod.mode_stay" : "msg.manyl_mod.mode_follow",
                        Component.translatable(getRole().getTranslationKey())), true);
                return InteractionResult.SUCCESS;
            }

            FoodProperties food = held.get(DataComponents.FOOD);
            if (food != null && getHunger() < MAX_HUNGER) {
                int restore = Math.max(12, food.nutrition() * 8);
                setHunger(Math.min(MAX_HUNGER, getHunger() + restore));
                heal(Math.max(2.0F, food.nutrition() * 0.6F));
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                setEatTicks(18);
                playSound(ModSounds.GOLEM_EAT, 0.50F, 1.0F);
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ModParticles.CORE_HEART,
                            getX(), getY() + 0.7D, getZ(), 6, 0.25D, 0.25D, 0.25D, 0.04D);
                }
                return InteractionResult.SUCCESS;
            }
            openGolemMenu(player);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleDisabledInteract(ServerPlayer player, ItemStack held) {
        int step = 0;
        if (held.is(Items.DIAMOND)) step = 2;
        else if (held.is(Items.IRON_INGOT) || held.is(Items.REDSTONE)) step = 1;

        if (step <= 0) {
            player.displayClientMessage(Component.translatable("msg.manyl_mod.repair_hint", repairProgress, 2), true);
            playSound(ModSounds.GOLEM_HURT, 0.45F, 0.75F);
            return InteractionResult.SUCCESS;
        }

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        repairProgress += step;
        playSound(SoundEvents.ANVIL_USE, 0.55F, 1.2F);
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ModParticles.GOLEM_SPARK, getX(), getY() + 1.0D, getZ(), 10, 0.3D, 0.3D, 0.3D, 0.04D);
        }

        if (repairProgress >= 2) {
            repairProgress = 0;
            setDisabled(false);
            setHealth(getMaxHealth());
            setHunger(MAX_HUNGER);
            recruitBy(player);
            if (this instanceof ValkyrieGolemEntity) {
                playSound(ModSounds.VALKYRIE_REPAIRED, 0.85F, 1.0F);
            }
            player.displayClientMessage(Component.translatable("msg.manyl_mod.repaired",
                    Component.translatable(getRole().getTranslationKey())), false);
        } else {
            player.displayClientMessage(Component.translatable("msg.manyl_mod.repair_progress", repairProgress, 2), true);
        }
        return InteractionResult.SUCCESS;
    }

    public void openInventoryMenuFromDialogue(Player player) {
        if (!level().isClientSide()) {
            openGolemMenu(player);
        }
    }

    private void openGolemMenu(Player player) {
        player.openMenu(new ExtendedScreenHandlerFactory<Integer>() {
            @Override
            public Integer getScreenOpeningData(ServerPlayer serverPlayer) {
                return BaseGolemEntity.this.getId();
            }

            @Override
            public Component getDisplayName() {
                return BaseGolemEntity.this.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player p) {
                return new GolemInventoryMenu(syncId, playerInv, BaseGolemEntity.this);
            }
        });
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide()) {
            Containers.dropContents(level(), blockPosition(), inventory);
            if (getHunger() <= 0 && ownerUuid != null && level() instanceof ServerLevel sl) {
                Player owner = sl.getServer().getPlayerList().getPlayer(ownerUuid);
                if (owner != null) {
                    owner.displayClientMessage(Component.translatable("msg.manyl_mod.starved", getDisplayName()), false);
                }
            }
        }
        super.die(source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isDisabled() && source.getEntity() instanceof Player) {
            return false; // Игрок не сломает случайно спящего стража из руин
        }
        ItemStack shield = inventory.getItem(SHIELD_SLOT);
        if (!shield.isEmpty() && shield.is(Items.SHIELD) && source.getEntity() != null && random.nextFloat() < 0.30F) {
            amount *= 0.60F;
            playSound(SoundEvents.SHIELD_BLOCK.value(), 0.75F, 0.95F + random.nextFloat() * 0.1F);
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        setAttackTicks(12);
        playSound(ModSounds.GOLEM_ATTACK, 0.50F, 0.95F + random.nextFloat() * 0.15F);
        level.sendParticles(ModParticles.GOLEM_SPARK,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                6, 0.2D, 0.2D, 0.2D, 0.05D);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            float bonus = getRankBonusDamage();
            if (bonus > 0.0F && living.isAlive()) {
                living.hurtServer(level, damageSources().mobAttack(this), bonus);
            }
            if (living.isDeadOrDying()) {
                addKill();
            }
        }
        return hit;
    }

    public PlayerGolemSettings getOwnerSettings() {
        if (level() instanceof ServerLevel sl && ownerUuid != null) {
            return ManylSavedData.get(sl.getServer()).getSettings(ownerUuid);
        }
        return new PlayerGolemSettings();
    }

    public LivingEntity getOwnerEntity() {
        if (ownerUuid == null) return null;
        return level().getPlayerByUUID(ownerUuid);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.GOLEM_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return ModSounds.GOLEM_DEATH; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(ModSounds.GOLEM_STEP, 0.16F, 0.9F + random.nextFloat() * 0.2F);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("Hunger", getHunger());
        out.putBoolean("Sitting", isSitting());
        out.putBoolean("Disabled", isDisabled());
        out.putInt("RepairProgress", repairProgress);
        out.putInt("Kills", getKills());
        if (ownerUuid != null) out.putString("Owner", ownerUuid.toString());
        if (homePos != null) out.store("HomePos", BlockPos.CODEC, homePos);
        List<ItemStack> items = new ArrayList<>(INVENTORY_SIZE);
        for (int i = 0; i < INVENTORY_SIZE; i++) items.add(inventory.getItem(i));
        out.store("Inventory", ItemStack.OPTIONAL_CODEC.listOf(), items);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        setHunger(in.getIntOr("Hunger", MAX_HUNGER));
        setSitting(in.getBooleanOr("Sitting", false));
        setDisabled(in.getBooleanOr("Disabled", false));
        this.repairProgress = in.getIntOr("RepairProgress", 0);
        setKills(in.getIntOr("Kills", 0));
        in.getString("Owner").ifPresent(s -> {
            try { this.ownerUuid = UUID.fromString(s); } catch (IllegalArgumentException ignored) {}
        });
        this.homePos = in.read("HomePos", BlockPos.CODEC).orElse(null);
        in.read("Inventory", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
            for (int i = 0; i < Math.min(list.size(), INVENTORY_SIZE); i++) {
                inventory.setItem(i, list.get(i));
            }
            updateCarryingFlag();
            syncArmorAndShieldSlots();
            onInventoryChanged();
        });
    }

    public SimpleContainer getGolemInventory() { return inventory; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public void setOwnerUuid(UUID ownerUuid) { this.ownerUuid = ownerUuid; }
    public BlockPos getHomePos() { return homePos; }
    public void setHomePos(BlockPos homePos) { this.homePos = homePos; }

    public int getHunger() { return entityData.get(DATA_HUNGER); }
    public void setHunger(int hunger) { entityData.set(DATA_HUNGER, Math.max(0, Math.min(MAX_HUNGER, hunger))); }

    public boolean isWorking() { return entityData.get(DATA_WORKING); }
    public void setWorking(boolean working) { entityData.set(DATA_WORKING, working); }

    public boolean isCarrying() { return entityData.get(DATA_CARRYING); }
    public void setCarrying(boolean carrying) { entityData.set(DATA_CARRYING, carrying); }

    public boolean isSitting() { return entityData.get(DATA_SITTING); }
    public void setSitting(boolean sitting) { entityData.set(DATA_SITTING, sitting); }

    public int getSummonTicks() { return entityData.get(DATA_SUMMON_TICKS); }
    public void setSummonTicks(int ticks) { entityData.set(DATA_SUMMON_TICKS, Math.max(0, ticks)); }

    public int getEatTicks() { return entityData.get(DATA_EAT_TICKS); }
    public void setEatTicks(int ticks) { entityData.set(DATA_EAT_TICKS, Math.max(0, ticks)); }

    public int getAttackTicks() { return entityData.get(DATA_ATTACK_TICKS); }
    public void setAttackTicks(int ticks) { entityData.set(DATA_ATTACK_TICKS, Math.max(0, ticks)); }

    public ItemStack getDisplayItem() { return entityData.get(DATA_DISPLAY_ITEM); }
    public void setDisplayItem(ItemStack stack) { entityData.set(DATA_DISPLAY_ITEM, stack == null ? ItemStack.EMPTY : stack.copy()); }

    public boolean isDisabled() { return entityData.get(DATA_DISABLED); }
    public void setDisabled(boolean disabled) { entityData.set(DATA_DISABLED, disabled); }

    public int getKills() { return entityData.get(DATA_KILLS); }
    public void setKills(int kills) { entityData.set(DATA_KILLS, Math.max(0, kills)); }
    public void addKill() { setKills(getKills() + 1); }

    public int getRankTier() {
        int k = getKills();
        if (k >= 30) return 3;
        if (k >= 15) return 2;
        if (k >= 5) return 1;
        return 0;
    }

    public float getRankBonusDamage() {
        return switch (getRankTier()) {
            case 3 -> 4.0F;
            case 2 -> 2.5F;
            case 1 -> 1.0F;
            default -> 0.0F;
        };
    }

    public String getRankTranslationKey() {
        return switch (getRankTier()) {
            case 3 -> "rank.manyl_mod.master";
            case 2 -> "rank.manyl_mod.veteran";
            case 1 -> "rank.manyl_mod.guard";
            default -> "rank.manyl_mod.recruit";
        };
    }

    @Override
    public Component getDisplayName() {
        Component base = super.getDisplayName();
        if (isDisabled()) {
            return Component.literal("[×] ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(base.copy().withStyle(ChatFormatting.GRAY));
        }
        if (!isTamed()) {
            return base;
        }
        net.minecraft.network.chat.MutableComponent result = base.copy();
        int tier = getRankTier();
        if (tier > 0) {
            String stars = switch (tier) {
                case 3 -> " ★★★";
                case 2 -> " ★★";
                default -> " ★";
            };
            ChatFormatting starColor = switch (tier) {
                case 3 -> ChatFormatting.AQUA;
                case 2 -> ChatFormatting.GOLD;
                default -> ChatFormatting.YELLOW;
            };
            result.append(Component.literal(stars).withStyle(starColor));
        }
        if (this instanceof ValkyrieGolemEntity valkyrie) {
            if (valkyrie.isVowBound()) {
                result.append(Component.literal(" ♥★").withStyle(ChatFormatting.LIGHT_PURPLE));
            } else if (valkyrie.getRomanceStage() > 0) {
                String hearts = " " + "♥".repeat(Math.min(3, valkyrie.getRomanceStage()));
                result.append(Component.literal(hearts).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
        return result;
    }

    protected static class PickupFoodAndToolGoal extends Goal {
        private final BaseGolemEntity golem;
        private ItemEntity targetItem;
        private int scanCooldown;

        public PickupFoodAndToolGoal(BaseGolemEntity golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (golem.isDisabled() || golem.isSitting() || golem.isWorking()) return false;
            if (scanCooldown > 0) {
                scanCooldown--;
                return false;
            }
            scanCooldown = 10;
            targetItem = findBestFoodOrToolItem();
            return targetItem != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (golem.isDisabled() || golem.isSitting() || golem.isWorking()) return false;
            return targetItem != null && targetItem.isAlive()
                    && !targetItem.hasPickUpDelay()
                    && golem.canAcceptFoodOrTool(targetItem.getItem())
                    && golem.distanceToSqr(targetItem) <= 100.0D;
        }

        @Override
        public void stop() {
            targetItem = null;
            golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (targetItem == null || !targetItem.isAlive()) return;
            golem.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);
            double distSq = golem.distanceToSqr(targetItem);
            if (distSq > 2.25D) {
                golem.getNavigation().moveTo(targetItem, 1.15D);
            } else {
                golem.pickupNearbyFoodAndTools(2.0D);
                targetItem = null;
            }
        }

        private ItemEntity findBestFoodOrToolItem() {
            List<ItemEntity> list = golem.level().getEntitiesOfClass(
                    ItemEntity.class,
                    golem.getBoundingBox().inflate(8.5D, 3.0D, 8.5D),
                    item -> item.isAlive() && !item.hasPickUpDelay()
                            && (isFoodItem(item.getItem()) || isToolOrWeaponItem(item.getItem()))
                            && golem.canAcceptFoodOrTool(item.getItem())
            );
            ItemEntity best = null;
            double bestScore = Double.MAX_VALUE;
            for (ItemEntity item : list) {
                double d = golem.distanceToSqr(item);
                // Голодный голем сильнее тянется к еде
                if (isFoodItem(item.getItem()) && golem.getHunger() <= 60) {
                    d *= 0.45D;
                }
                if (d < bestScore) {
                    bestScore = d;
                    best = item;
                }
            }
            return best;
        }
    }
}
