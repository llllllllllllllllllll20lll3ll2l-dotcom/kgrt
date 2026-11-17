package com.manyl.mod.entity;

import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.entity.ai.FollowOwnerGoal;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.List;

/**
 * Голем-копатель (макс. 4 на игрока).
 * Добывает блоки в настроенном радиусе и режиме (Карьер / Туннель / Только руды),
 * складывает добычу в груз и выгружает в ближайший или привязанный сундук.
 */
public class DiggerGolemEntity extends BaseGolemEntity {
    public DiggerGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public GolemRole getRole() {
        return GolemRole.DIGGER;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new DiggerWorkGoal(this));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.05D, 7.0F, 3.0F));
    }

    private static class DiggerWorkGoal extends Goal {
        private final DiggerGolemEntity golem;
        private BlockPos targetPos;
        private BlockPos depositChestPos;
        private int digProgress;
        private int scanCooldown;

        DiggerWorkGoal(DiggerGolemEntity golem) {
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
            scanCooldown = 15;

            if (golem.isCargoFull()) {
                depositChestPos = golem.findNearestContainerPos(10);
                return depositChestPos != null;
            }
            targetPos = findTargetBlock();
            if (targetPos != null) return true;

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
            return targetPos != null && isValidDigTarget(targetPos, golem.getOwnerSettings().getDigMode());
        }

        @Override
        public void start() {
            digProgress = 0;
        }

        @Override
        public void stop() {
            targetPos = null;
            depositChestPos = null;
            digProgress = 0;
            golem.setWorking(false);
            golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (depositChestPos != null) {
                golem.setWorking(false);
                double dist = golem.position().distanceToSqr(
                        depositChestPos.getX() + 0.5D, depositChestPos.getY() + 0.5D, depositChestPos.getZ() + 0.5D);
                if (dist > 4.5D) {
                    golem.getNavigation().moveTo(depositChestPos.getX() + 0.5D, depositChestPos.getY(), depositChestPos.getZ() + 0.5D, 1.05D);
                } else {
                    golem.getNavigation().stop();
                    BlockEntity be = golem.level().getBlockEntity(depositChestPos);
                    if (be instanceof Container container) {
                        if (golem.depositCargoIntoContainer(container)) {
                            golem.playSound(ModSounds.GOLEM_WORK_SORT, 0.7F, 1.0F);
                        }
                    }
                    depositChestPos = null;
                }
                return;
            }

            if (targetPos == null) return;
            double distSq = golem.position().distanceToSqr(
                    targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D);

            if (distSq > 6.25D) {
                golem.setWorking(false);
                golem.getNavigation().moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 1.05D);
                return;
            }

            golem.getNavigation().stop();
            golem.getLookControl().setLookAt(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D);
            golem.setWorking(true);

            digProgress++;
            if (digProgress % 8 == 1) {
                golem.playSound(ModSounds.GOLEM_WORK_DIG, 0.7F, 0.9F + golem.getRandom().nextFloat() * 0.2F);
                if (golem.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ModParticles.GOLEM_SPARK,
                            targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D,
                            3, 0.25D, 0.25D, 0.25D, 0.03D);
                }
            }

            BlockState state = golem.level().getBlockState(targetPos);
            float hardness = state.getDestroySpeed(golem.level(), targetPos);
            int requiredTicks = Math.max(10, Math.min(60, (int) (hardness * 10.0F)));

            if (digProgress >= requiredTicks && golem.level() instanceof ServerLevel sl) {
                List<ItemStack> drops = Block.getDrops(state, sl, targetPos, sl.getBlockEntity(targetPos), golem, ItemStack.EMPTY);
                sl.destroyBlock(targetPos, false, golem);
                for (ItemStack drop : drops) {
                    ItemStack leftover = golem.addToCargo(drop);
                    if (!leftover.isEmpty()) {
                        Block.popResource(sl, targetPos, leftover);
                    }
                }
                golem.setWorking(false);
                targetPos = null;
            }
        }

        private BlockPos findTargetBlock() {
            PlayerGolemSettings settings = golem.getOwnerSettings();
            int radius = settings.getDiggerRadius();
            DigMode mode = settings.getDigMode();

            BlockPos center = golem.getHomePos();
            if (center == null) {
                LivingEntity owner = golem.getOwnerEntity();
                center = (settings.isFollowOwner() && owner != null) ? owner.blockPosition() : golem.blockPosition();
            }

            int minY = mode == DigMode.TUNNEL ? center.getY() : center.getY() - 2;
            int maxY = mode == DigMode.TUNNEL ? center.getY() + 1 : center.getY() + 2;

            BlockPos best = null;
            double bestScore = Double.MAX_VALUE;
            for (int y = maxY; y >= minY; y--) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                        if (isValidDigTarget(pos, mode)) {
                            double d = pos.distSqr(golem.blockPosition());
                            // В режиме карьера предпочитаем верхние слои сначала
                            double score = d + (maxY - y) * 8.0D;
                            if (score < bestScore) {
                                bestScore = score;
                                best = pos;
                            }
                        }
                    }
                }
            }
            return best;
        }

        private boolean isValidDigTarget(BlockPos pos, DigMode mode) {
            BlockState state = golem.level().getBlockState(pos);
            if (state.isAir() || state.liquid()) return false;
            float speed = state.getDestroySpeed(golem.level(), pos);
            if (speed < 0.0F || speed > 55.0F) return false;
            if (golem.level().getBlockEntity(pos) != null) return false;
            if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL)
                    || state.is(Blocks.FARMLAND) || state.is(Blocks.TORCH)) {
                return false;
            }

            if (mode == DigMode.ORES_ONLY) {
                String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
                return path.contains("_ore") || state.is(Blocks.ANCIENT_DEBRIS) || state.is(Blocks.RAW_IRON_BLOCK) || state.is(Blocks.RAW_COPPER_BLOCK) || state.is(Blocks.RAW_GOLD_BLOCK);
            }
            // Не копаем блок прямо под ногами самого голема
            if (pos.equals(golem.blockPosition().below())) return false;
            return true;
        }
    }
}
