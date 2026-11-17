package com.manyl.mod.entity;

import com.manyl.mod.data.ManylSavedData;
import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.menu.GolemInventoryMenu;
import com.manyl.mod.registry.ModItems;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Базовый класс всех прислужников-големов Manyl_mod (~0.8 блока высотой).
 */
public abstract class BaseGolemEntity extends PathfinderMob {
    public static final int MAX_HUNGER = 100;
    public static final int FOOD_SLOT = 0;
    public static final int INVENTORY_SIZE = 7; // 1 слот еды + 6 слотов груза/снаряжения

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

    protected BaseGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.inventory.addListener(container -> {
            updateCarryingFlag();
            onInventoryChanged();
        });
    }

    public abstract GolemRole getRole();

    protected void onInventoryChanged() {
        // Переопределяется подклассами (например, WarriorGolemEntity для экипировки оружия)
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
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            updateClientAnimations();
            return;
        }

        if (getSummonTicks() > 0) setSummonTicks(getSummonTicks() - 1);
        if (getEatTicks() > 0) setEatTicks(getEatTicks() - 1);
        if (getAttackTicks() > 0) setAttackTicks(getAttackTicks() - 1);

        int drainThreshold = isWorking() ? 160 : 260;
        hungerTickCounter++;
        if (hungerTickCounter >= drainThreshold) {
            hungerTickCounter = 0;
            int current = getHunger();
            if (current > 0) {
                setHunger(current - 1);
            }
        }

        if (getHunger() <= 55 && tickCount % 20 == 0) {
            tryEatFromInventory();
        }

        if (getHunger() > 0 && getHunger() <= 20) {
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

        if (getHunger() <= 0) {
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

        for (int i = 1; i < INVENTORY_SIZE; i++) {
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
        for (int i = 1; i < INVENTORY_SIZE; i++) {
            if (inventory.getItem(i).isEmpty()) {
                inventory.setItem(i, remaining.copy());
                inventory.setChanged();
                return ItemStack.EMPTY;
            }
        }
        return remaining;
    }

    public boolean hasCargo() {
        for (int i = 1; i < INVENTORY_SIZE; i++) {
            if (!inventory.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    public boolean isCargoFull() {
        for (int i = 1; i < INVENTORY_SIZE; i++) {
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
        for (int i = 1; i < INVENTORY_SIZE; i++) {
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
        ItemStack held = player.getItemInHand(hand);
        if (held.is(ModItems.SUMMON_STAFF)) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide()) {
            if (player.isShiftKeyDown()) {
                setSitting(!isSitting());
                playSound(ModSounds.GOLEM_STEP, 0.16F, isSitting() ? 0.8F : 1.2F);
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
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        setAttackTicks(12);
        playSound(ModSounds.GOLEM_ATTACK, 0.50F, 0.95F + random.nextFloat() * 0.15F);
        level.sendParticles(ModParticles.GOLEM_SPARK,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                6, 0.2D, 0.2D, 0.2D, 0.05D);
        return super.doHurtTarget(level, target);
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
        in.getString("Owner").ifPresent(s -> {
            try { this.ownerUuid = UUID.fromString(s); } catch (IllegalArgumentException ignored) {}
        });
        this.homePos = in.read("HomePos", BlockPos.CODEC).orElse(null);
        in.read("Inventory", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
            for (int i = 0; i < Math.min(list.size(), INVENTORY_SIZE); i++) {
                inventory.setItem(i, list.get(i));
            }
            updateCarryingFlag();
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
}
