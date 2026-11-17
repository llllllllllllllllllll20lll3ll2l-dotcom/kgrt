package com.manyl.mod.entity;

import com.manyl.mod.entity.ai.FollowOwnerGoal;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * Голем-сортировщик (макс. 6 на игрока).
 * Подбирает предметы с земли (или берёт из привязанного сундука) и раскладывает
 * по сундукам, где уже хранятся совпадающие типы предметов.
 */
public class SorterGolemEntity extends BaseGolemEntity {
    public SorterGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public GolemRole getRole() {
        return GolemRole.SORTER;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new SorterWorkGoal(this));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.1D, 9.0F, 3.0F));
    }

    private static class SorterWorkGoal extends Goal {
        private final SorterGolemEntity golem;
        private ItemEntity targetItem;
        private BlockPos targetChest;
        private int scanCooldown;

        SorterWorkGoal(SorterGolemEntity golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (golem.isSitting() || golem.getHunger() <= 5) return false;
            if (scanCooldown > 0) {
                scanCooldown--;
                return false;
            }
            scanCooldown = 12;

            if (!golem.isCargoFull()) {
                targetItem = findNearestItemEntity();
                if (targetItem != null) return true;
            }
            if (golem.hasCargo()) {
                targetChest = findBestDestinationChest();
                return targetChest != null;
            }
            // Если привязан входной сундук homePos — пробуем взять из него предмет и рассортировать в другой сундук
            BlockPos home = golem.getHomePos();
            if (home != null && golem.level().getBlockEntity(home) instanceof Container homeContainer) {
                if (hasAnyNonEmptyStack(homeContainer) && findNonHomeContainer() != null) {
                    targetChest = home;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (golem.isSitting() || golem.getHunger() <= 5) return false;
            if (targetItem != null) {
                return targetItem.isAlive() && !golem.isCargoFull();
            }
            return targetChest != null && golem.level().getBlockEntity(targetChest) instanceof Container;
        }

        @Override
        public void stop() {
            targetItem = null;
            targetChest = null;
            golem.setWorking(false);
            golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (targetItem != null) {
                if (!targetItem.isAlive()) {
                    targetItem = null;
                    return;
                }
                double d = golem.distanceToSqr(targetItem);
                if (d > 2.5D) {
                    golem.getNavigation().moveTo(targetItem, 1.15D);
                } else {
                    ItemStack stack = targetItem.getItem();
                    ItemStack leftover = golem.addToCargo(stack);
                    if (leftover.isEmpty()) {
                        targetItem.discard();
                    } else {
                        targetItem.setItem(leftover);
                    }
                    golem.playSound(ModSounds.GOLEM_WORK_SORT, 0.65F, 1.25F);
                    targetItem = null;
                }
                return;
            }

            if (targetChest != null) {
                double d = golem.position().distanceToSqr(
                        targetChest.getX() + 0.5D, targetChest.getY() + 0.5D, targetChest.getZ() + 0.5D);
                if (d > 4.2D) {
                    golem.getNavigation().moveTo(targetChest.getX() + 0.5D, targetChest.getY(), targetChest.getZ() + 0.5D, 1.1D);
                } else {
                    golem.getNavigation().stop();
                    BlockEntity be = golem.level().getBlockEntity(targetChest);
                    if (be instanceof Container c) {
                        if (golem.hasCargo()) {
                            if (golem.depositCargoIntoContainer(c)) {
                                fx(targetChest);
                            }
                        } else if (targetChest.equals(golem.getHomePos())) {
                            pullFromInputChest(c);
                            fx(targetChest);
                        }
                    }
                    targetChest = null;
                }
            }
        }

        private void fx(BlockPos pos) {
            golem.playSound(ModSounds.GOLEM_WORK_SORT, 0.8F, 1.0F);
            if (golem.level() instanceof ServerLevel sl) {
                sl.sendParticles(ModParticles.GOLEM_SPARK,
                        pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D,
                        4, 0.2D, 0.2D, 0.2D, 0.02D);
            }
        }

        private void pullFromInputChest(Container input) {
            for (int i = 0; i < input.getContainerSize(); i++) {
                ItemStack s = input.getItem(i);
                if (!s.isEmpty()) {
                    ItemStack rem = golem.addToCargo(s);
                    input.setItem(i, rem);
                    input.setChanged();
                    if (golem.isCargoFull()) break;
                }
            }
        }

        private ItemEntity findNearestItemEntity() {
            List<ItemEntity> items = golem.level().getEntitiesOfClass(
                    ItemEntity.class,
                    golem.getBoundingBox().inflate(10.0D, 4.0D, 10.0D),
                    e -> e.isAlive() && !e.getItem().isEmpty()
            );
            ItemEntity best = null;
            double bestD = Double.MAX_VALUE;
            for (ItemEntity item : items) {
                double d = golem.distanceToSqr(item);
                if (d < bestD) {
                    bestD = d;
                    best = item;
                }
            }
            return best;
        }

        private BlockPos findBestDestinationChest() {
            BlockPos origin = golem.blockPosition();
            BlockPos matching = null;
            double matchingDist = Double.MAX_VALUE;
            BlockPos fallback = null;
            double fallbackDist = Double.MAX_VALUE;

            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-10, -3, -10), origin.offset(10, 3, 10))) {
                BlockEntity be = golem.level().getBlockEntity(pos);
                if (!(be instanceof Container c)) continue;
                double d = pos.distSqr(origin);

                if (containsMatchingCargo(c) && hasSpaceForCargo(c)) {
                    if (d < matchingDist) {
                        matchingDist = d;
                        matching = pos.immutable();
                    }
                } else if (hasSpaceForCargo(c) && !pos.equals(golem.getHomePos())) {
                    if (d < fallbackDist) {
                        fallbackDist = d;
                        fallback = pos.immutable();
                    }
                }
            }
            if (matching != null) return matching;
            if (fallback != null) return fallback;
            return golem.findNearestContainerPos(10);
        }

        private BlockPos findNonHomeContainer() {
            BlockPos origin = golem.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-10, -3, -10), origin.offset(10, 3, 10))) {
                if (!pos.equals(golem.getHomePos()) && golem.level().getBlockEntity(pos) instanceof Container) {
                    return pos.immutable();
                }
            }
            return null;
        }

        private boolean hasAnyNonEmptyStack(Container c) {
            for (int i = 0; i < c.getContainerSize(); i++) {
                if (!c.getItem(i).isEmpty()) return true;
            }
            return false;
        }

        private boolean containsMatchingCargo(Container c) {
            for (int i = 1; i < BaseGolemEntity.INVENTORY_SIZE; i++) {
                ItemStack cargo = golem.getGolemInventory().getItem(i);
                if (cargo.isEmpty()) continue;
                for (int j = 0; j < c.getContainerSize(); j++) {
                    ItemStack dest = c.getItem(j);
                    if (!dest.isEmpty() && ItemStack.isSameItem(dest, cargo)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean hasSpaceForCargo(Container c) {
            for (int j = 0; j < c.getContainerSize(); j++) {
                ItemStack dest = c.getItem(j);
                if (dest.isEmpty() || dest.getCount() < dest.getMaxStackSize()) return true;
            }
            return false;
        }
    }
}
