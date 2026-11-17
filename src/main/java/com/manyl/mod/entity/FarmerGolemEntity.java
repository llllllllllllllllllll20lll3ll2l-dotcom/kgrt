package com.manyl.mod.entity;

import com.manyl.mod.entity.ai.FollowOwnerGoal;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.List;

/**
 * Голем-фермер (макс. 2 на игрока).
 * Поддерживает фильтр культур через команду !crop <культура|all>.
 */
public class FarmerGolemEntity extends BaseGolemEntity {
    public FarmerGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public GolemRole getRole() {
        return GolemRole.FARMER;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new FarmerWorkGoal(this));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.05D, 8.0F, 3.0F));
    }

    private static class FarmerWorkGoal extends Goal {
        private final FarmerGolemEntity golem;
        private BlockPos targetCrop;
        private BlockPos depositChestPos;
        private int workTicks;
        private int scanCooldown;

        FarmerWorkGoal(FarmerGolemEntity golem) {
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
            scanCooldown = 20;

            if (golem.isCargoFull()) {
                depositChestPos = golem.findNearestContainerPos(10);
                return depositChestPos != null;
            }
            targetCrop = findMatureCrop();
            if (targetCrop != null) return true;

            if (golem.hasCargo()) {
                depositChestPos = golem.findNearestContainerPos(10);
                return depositChestPos != null;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (golem.isSitting() || golem.getHunger() <= 5) return false;
            if (depositChestPos != null) {
                return golem.hasCargo() && golem.level().getBlockEntity(depositChestPos) instanceof Container;
            }
            return targetCrop != null && isMatureAndAllowed(golem.level().getBlockState(targetCrop));
        }

        @Override
        public void start() { workTicks = 0; }

        @Override
        public void stop() {
            targetCrop = null;
            depositChestPos = null;
            workTicks = 0;
            golem.setWorking(false);
            golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (depositChestPos != null) {
                golem.setWorking(false);
                double d = golem.position().distanceToSqr(
                        depositChestPos.getX() + 0.5D, depositChestPos.getY() + 0.5D, depositChestPos.getZ() + 0.5D);
                if (d > 4.5D) {
                    golem.getNavigation().moveTo(depositChestPos.getX() + 0.5D, depositChestPos.getY(), depositChestPos.getZ() + 0.5D, 1.05D);
                } else {
                    golem.getNavigation().stop();
                    BlockEntity be = golem.level().getBlockEntity(depositChestPos);
                    if (be instanceof Container c && golem.depositCargoIntoContainer(c)) {
                        golem.playSound(ModSounds.GOLEM_WORK_SORT, 0.35F, 1.1F);
                    }
                    depositChestPos = null;
                }
                return;
            }

            if (targetCrop == null) return;
            double distSq = golem.position().distanceToSqr(
                    targetCrop.getX() + 0.5D, targetCrop.getY() + 0.2D, targetCrop.getZ() + 0.5D);

            if (distSq > 4.0D) {
                golem.setWorking(false);
                golem.getNavigation().moveTo(targetCrop.getX() + 0.5D, targetCrop.getY(), targetCrop.getZ() + 0.5D, 1.05D);
                return;
            }

            golem.getNavigation().stop();
            golem.getLookControl().setLookAt(targetCrop.getX() + 0.5D, targetCrop.getY() + 0.3D, targetCrop.getZ() + 0.5D);
            golem.setWorking(true);
            workTicks++;

            if (workTicks == 1) {
                golem.playSound(ModSounds.GOLEM_WORK_FARM, 0.35F, 1.0F);
                if (golem.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ModParticles.GOLEM_SPARK,
                            targetCrop.getX() + 0.5D, targetCrop.getY() + 0.4D, targetCrop.getZ() + 0.5D,
                            4, 0.25D, 0.2D, 0.25D, 0.02D);
                }
            }

            if (workTicks >= 16 && golem.level() instanceof ServerLevel sl) {
                BlockState state = sl.getBlockState(targetCrop);
                Block cropBlock = state.getBlock();
                List<ItemStack> drops = Block.getDrops(state, sl, targetCrop, null, golem, ItemStack.EMPTY);

                boolean replanted = false;
                for (ItemStack drop : drops) {
                    if (!replanted && drop.getItem() instanceof BlockItem bi && bi.getBlock() == cropBlock) {
                        drop.shrink(1);
                        sl.setBlock(targetCrop, cropBlock.defaultBlockState(), 3);
                        replanted = true;
                    }
                    if (!drop.isEmpty()) {
                        ItemStack leftover = golem.addToCargo(drop);
                        if (!leftover.isEmpty()) {
                            Block.popResource(sl, targetCrop, leftover);
                        }
                    }
                }
                if (!replanted) {
                    sl.destroyBlock(targetCrop, false, golem);
                }
                golem.setWorking(false);
                targetCrop = null;
            }
        }

        private BlockPos findMatureCrop() {
            BlockPos center = golem.getHomePos() != null ? golem.getHomePos() : golem.blockPosition();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -2, -8), center.offset(8, 2, 8))) {
                if (isMatureAndAllowed(golem.level().getBlockState(pos))) {
                    double d = pos.distSqr(golem.blockPosition());
                    if (d < bestDist) {
                        bestDist = d;
                        best = pos.immutable();
                    }
                }
            }
            return best;
        }

        private boolean isMatureAndAllowed(BlockState state) {
            boolean mature = false;
            if (state.getBlock() instanceof CropBlock crop) {
                mature = crop.isMaxAge(state);
            } else if (state.getBlock() instanceof NetherWartBlock) {
                mature = state.getValue(NetherWartBlock.AGE) >= 3;
            }
            if (!mature) return false;

            String cropFilter = golem.getOwnerSettings().getCropFilter();
            if (cropFilter == null || cropFilter.isEmpty()) return true;
            String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            return path.contains(cropFilter.toLowerCase());
        }
    }
}
