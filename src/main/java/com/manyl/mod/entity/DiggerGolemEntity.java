package com.manyl.mod.entity;

import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.data.QuarryPlanner;
import com.manyl.mod.entity.ai.FollowOwnerGoal;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Голем-копатель (макс. 4 на игрока).
 * Умеет:
 * - копать размеченный карьер послойно со строительством ровной спиральной каменной лестницы по кругу;
 * - спускаться по построенной лестнице в пещеры/шахты и застраивать края зоны выделения камнем;
 * - закрывать и осушать лаву;
 * - распределять цели между союзниками и оставлять 1 стак стройблоков при заполнении (руду не выкидывает).
 */
public class DiggerGolemEntity extends BaseGolemEntity {
    private BlockPos reservedTargetPos;

    public DiggerGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.LAVA, 16.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 8.0F);
    }

    @Override
    public GolemRole getRole() {
        return GolemRole.DIGGER;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    public BlockPos getReservedTargetPos() {
        return reservedTargetPos;
    }

    public void setReservedTargetPos(BlockPos pos) {
        this.reservedTargetPos = pos;
    }

    @Override
    public ItemStack addToCargo(ItemStack stack) {
        ItemStack remaining = super.addToCargo(stack);
        if (!remaining.isEmpty() || isCargoFull()) {
            if (pruneExcessBuildingBlocks() && !remaining.isEmpty()) {
                remaining = super.addToCargo(remaining);
            }
        }
        return remaining;
    }

    public boolean pruneExcessBuildingBlocks() {
        if (level().isClientSide()) return false;
        if (!getOwnerSettings().isDiggerDropTrash()) return false;
        if (!isCargoFull()) return false;

        int keepSlot = -1;
        int keepCount = -1;
        List<Integer> trashSlots = new ArrayList<>();

        for (int i = 1; i < INVENTORY_SIZE; i++) {
            ItemStack s = inventory.getItem(i);
            if (!s.isEmpty() && isSecondaryBuildingBlock(s)) {
                trashSlots.add(i);
                if (s.getCount() > keepCount) {
                    keepCount = s.getCount();
                    keepSlot = i;
                }
            }
        }

        if (trashSlots.size() <= 1) return false;

        boolean droppedAny = false;
        for (int slot : trashSlots) {
            if (slot == keepSlot) continue;
            ItemStack toDrop = inventory.getItem(slot);
            if (!toDrop.isEmpty()) {
                if (level() instanceof ServerLevel sl) {
                    Block.popResource(sl, blockPosition(), toDrop.copy());
                }
                inventory.setItem(slot, ItemStack.EMPTY);
                droppedAny = true;
            }
        }
        if (droppedAny) inventory.setChanged();
        return droppedAny;
    }

    public static boolean isSecondaryBuildingBlock(ItemStack stack) {
        if (stack.isEmpty() || stack.has(DataComponents.FOOD)) return false;
        if (!(stack.getItem() instanceof BlockItem)) return false;

        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (isValuableResourcePath(path)) return false;

        return path.equals("stone") || path.equals("cobblestone")
                || path.equals("deepslate") || path.equals("cobbled_deepslate")
                || path.equals("dirt") || path.equals("coarse_dirt") || path.equals("rooted_dirt")
                || path.equals("grass_block") || path.equals("gravel")
                || path.equals("sand") || path.equals("red_sand") || path.equals("sandstone")
                || path.equals("andesite") || path.equals("diorite") || path.equals("granite")
                || path.equals("tuff") || path.equals("calcite")
                || path.equals("netherrack") || path.equals("basalt") || path.equals("blackstone")
                || path.equals("end_stone") || path.equals("mud") || path.equals("clay")
                || path.endsWith("_terracotta") || path.equals("terracotta");
    }

    private static boolean isValuableResourcePath(String path) {
        return path.contains("_ore")
                || path.startsWith("raw_")
                || path.contains("diamond")
                || path.contains("emerald")
                || path.contains("gold")
                || path.contains("iron")
                || path.contains("copper")
                || path.contains("coal")
                || path.contains("lapis")
                || path.contains("redstone")
                || path.contains("quartz")
                || path.contains("amethyst")
                || path.contains("debris")
                || path.contains("netherite")
                || path.equals("obsidian")
                || path.equals("glowstone_dust");
    }

    private BlockState takeOneScaffoldBlock() {
        for (int i = 1; i < INVENTORY_SIZE; i++) {
            ItemStack s = inventory.getItem(i);
            if (!s.isEmpty() && isSecondaryBuildingBlock(s) && s.getItem() instanceof BlockItem bi) {
                BlockState bs = bi.getBlock().defaultBlockState();
                s.shrink(1);
                if (s.isEmpty()) inventory.setItem(i, ItemStack.EMPTY);
                inventory.setChanged();
                return bs;
            }
        }
        return null;
    }

    private void consumeOneStoneIfPresent() {
        for (int i = 1; i < INVENTORY_SIZE; i++) {
            ItemStack s = inventory.getItem(i);
            if (!s.isEmpty() && isSecondaryBuildingBlock(s)) {
                s.shrink(1);
                if (s.isEmpty()) inventory.setItem(i, ItemStack.EMPTY);
                inventory.setChanged();
                return;
            }
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new DiggerWorkGoal(this));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.05D, 7.0F, 3.0F));
    }

    private static class DiggerWorkGoal extends Goal {
        private final DiggerGolemEntity golem;
        private final Map<BlockPos, Integer> tempSkipUntilTick = new HashMap<>();
        private BlockPos targetPos;
        private BlockPos buildPos;
        private BlockState buildState;
        private boolean buildIsLavaFix;
        private BlockPos tunnelBlockPos;
        private BlockPos entryPos;
        private BlockPos depositChestPos;
        private boolean escaping;
        private int digProgress;
        private int scanCooldown;
        private int stuckTicks;
        private double lastDistToGoal = Double.MAX_VALUE;

        DiggerWorkGoal(DiggerGolemEntity golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean isTempSkipped(BlockPos pos) {
            Integer until = tempSkipUntilTick.get(pos);
            if (until == null) return false;
            if (golem.tickCount >= until) {
                tempSkipUntilTick.remove(pos);
                return false;
            }
            return true;
        }

        private void skipTemporarily(BlockPos pos, int ticks) {
            if (pos != null) {
                tempSkipUntilTick.put(pos.immutable(), golem.tickCount + ticks);
            }
        }

        @Override
        public boolean canUse() {
            if (golem.isSitting() || golem.getHunger() <= 5) return false;
            if (scanCooldown > 0) {
                scanCooldown--;
                return false;
            }
            scanCooldown = 6;

            if (findNearbyLavaFix(golem.blockPosition(), 3)) {
                return true;
            }

            if (golem.isCargoFull()) {
                golem.pruneExcessBuildingBlocks();
            }
            if (golem.isCargoFull()) {
                depositChestPos = golem.findNearestContainerPos(12);
                if (depositChestPos != null) {
                    if (needToClimbOutForChest(depositChestPos)) {
                        escaping = true;
                    }
                    return true;
                }
            }

            PlayerGolemSettings settings = golem.getOwnerSettings();
            if (settings.isQuarryActive() && settings.hasQuarryBounds()) {
                if (selectNextQuarryTask(settings)) {
                    return true;
                }
                // Если весь карьер уже выкопан или есть груз — несём в сундук (и только потом поднимаемся наверх)
                if (golem.hasCargo()) {
                    depositChestPos = golem.findNearestContainerPos(12);
                    if (depositChestPos != null) {
                        if (needToClimbOutForChest(depositChestPos)) escaping = true;
                        return true;
                    }
                }
                if (needToClimbOut()) {
                    escaping = true;
                    return true;
                }
                return false;
            }

            targetPos = findTargetBlock();
            if (targetPos != null) {
                golem.setReservedTargetPos(targetPos);
                if (entryPos == null && targetPos.getY() < golem.blockPosition().getY()) {
                    entryPos = golem.blockPosition();
                }
                return true;
            }

            if (needToClimbOut()) {
                escaping = true;
                return true;
            }

            if (golem.hasCargo()) {
                depositChestPos = golem.findNearestContainerPos(12);
                return depositChestPos != null;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (golem.isSitting() || golem.getHunger() <= 5) return false;
            if (escaping) return needToClimbOut();
            if (depositChestPos != null) {
                return golem.hasCargo() && golem.level().getBlockEntity(depositChestPos) instanceof Container;
            }
            if (buildPos != null && buildState != null) {
                BlockState cur = golem.level().getBlockState(buildPos);
                if (buildIsLavaFix) {
                    return cur.getFluidState().is(FluidTags.LAVA);
                }
                if (!cur.is(buildState.getBlock())) return true;
                return buildState.hasProperty(StairBlock.FACING)
                        && cur.getValue(StairBlock.FACING) != buildState.getValue(StairBlock.FACING);
            }
            PlayerGolemSettings s = golem.getOwnerSettings();
            if (s.isQuarryActive() && s.hasQuarryBounds()) {
                return targetPos != null && isValidQuarryDigTarget(targetPos, s);
            }
            return targetPos != null && isValidDigTarget(targetPos, s.getDigMode(), s.getExtractFilter());
        }

        @Override
        public void start() {
            digProgress = 0;
            tunnelBlockPos = null;
            stuckTicks = 0;
            lastDistToGoal = Double.MAX_VALUE;
        }

        @Override
        public void stop() {
            targetPos = null;
            buildPos = null;
            buildState = null;
            buildIsLavaFix = false;
            tunnelBlockPos = null;
            depositChestPos = null;
            escaping = false;
            digProgress = 0;
            stuckTicks = 0;
            golem.setReservedTargetPos(null);
            golem.setWorking(false);
            golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (escaping) {
                tickEscapeToSurface();
                return;
            }

            if (depositChestPos != null) {
                golem.setWorking(false);
                double dist = golem.position().distanceToSqr(
                        depositChestPos.getX() + 0.5D, depositChestPos.getY() + 0.5D, depositChestPos.getZ() + 0.5D);
                if (dist > 4.5D) {
                    golem.getNavigation().moveTo(depositChestPos.getX() + 0.5D, depositChestPos.getY(), depositChestPos.getZ() + 0.5D, 1.08D);
                } else {
                    golem.getNavigation().stop();
                    BlockEntity be = golem.level().getBlockEntity(depositChestPos);
                    if (be instanceof Container container && golem.depositCargoIntoContainer(container)) {
                        golem.playSound(ModSounds.GOLEM_WORK_SORT, 0.35F, 1.0F);
                    }
                    depositChestPos = null;
                }
                return;
            }

            if (buildPos != null && buildState != null) {
                tickBuildOrSeal();
                return;
            }

            if (targetPos == null) return;

            if (findNearbyLavaFix(targetPos, 1)) {
                targetPos = null;
                return;
            }

            BlockPos activeDigPos = (tunnelBlockPos != null && isBreakableForTunnel(tunnelBlockPos)) ? tunnelBlockPos : targetPos;
            double distToTarget = golem.position().distanceToSqr(
                    targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D);

            PlayerGolemSettings settings = golem.getOwnerSettings();
            boolean inQuarry = settings.isQuarryActive() && settings.hasQuarryBounds();

            if (!inQuarry && settings.isDiggerTunnelToOre() && tunnelBlockPos == null && (distToTarget > 6.25D || !hasLineOfSightToBlock(targetPos))) {
                if (distToTarget <= 6.25D && !hasLineOfSightToBlock(targetPos)) {
                    tunnelBlockPos = findBlockingStepToward(targetPos);
                } else {
                    golem.setWorking(false);
                    Path path = golem.getNavigation().createPath(targetPos, 1);
                    boolean reachable = (path != null && path.canReach());
                    if (reachable) {
                        golem.getNavigation().moveTo(path, 1.08D);
                        if (Math.abs(distToTarget - lastDistToGoal) < 0.02D) stuckTicks++;
                        else stuckTicks = 0;
                        lastDistToGoal = distToTarget;
                    } else {
                        stuckTicks += 5;
                    }

                    if (stuckTicks >= 12) {
                        stuckTicks = 0;
                        BlockPos step = findBlockingStepToward(targetPos);
                        if (step != null) {
                            tunnelBlockPos = step;
                            digProgress = 0;
                        }
                    }
                    if (tunnelBlockPos == null) return;
                }
                activeDigPos = (tunnelBlockPos != null) ? tunnelBlockPos : targetPos;
            }

            double activeDistSq = golem.position().distanceToSqr(
                    activeDigPos.getX() + 0.5D, activeDigPos.getY() + 0.5D, activeDigPos.getZ() + 0.5D);
            if (activeDistSq > 7.5D) {
                golem.setWorking(false);
                moveTowardQuarryOrBlock(activeDigPos, inQuarry);
                if (Math.abs(activeDistSq - lastDistToGoal) < 0.04D) {
                    stuckTicks++;
                } else {
                    stuckTicks = 0;
                    lastDistToGoal = activeDistSq;
                }
                if (stuckTicks >= 45) {
                    // Не можем дойти к этому блоку — временно пропускаем, чтобы переключиться на ступеньку/другой край
                    skipTemporarily(activeDigPos, 60);
                    targetPos = null;
                    tunnelBlockPos = null;
                    stuckTicks = 0;
                    golem.setReservedTargetPos(null);
                    return;
                }
                if (activeDistSq > 16.0D || stuckTicks < 18) {
                    return;
                }
            }

            stuckTicks = 0;
            golem.getNavigation().stop();
            golem.getLookControl().setLookAt(activeDigPos.getX() + 0.5D, activeDigPos.getY() + 0.5D, activeDigPos.getZ() + 0.5D);
            golem.setWorking(true);

            digProgress++;
            if (digProgress % 14 == 1) {
                golem.playSound(ModSounds.GOLEM_WORK_DIG, 0.32F, 0.9F + golem.getRandom().nextFloat() * 0.2F);
                if (golem.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ModParticles.GOLEM_SPARK,
                            activeDigPos.getX() + 0.5D, activeDigPos.getY() + 0.5D, activeDigPos.getZ() + 0.5D,
                            3, 0.25D, 0.25D, 0.25D, 0.03D);
                }
            }

            BlockState state = golem.level().getBlockState(activeDigPos);
            float hardness = state.getDestroySpeed(golem.level(), activeDigPos);
            int requiredTicks = Math.max(8, Math.min(55, (int) (hardness * 9.0F)));

            if (digProgress >= requiredTicks && golem.level() instanceof ServerLevel sl) {
                digProgress = 0;
                List<ItemStack> drops = Block.getDrops(state, sl, activeDigPos, sl.getBlockEntity(activeDigPos), golem, ItemStack.EMPTY);
                sl.destroyBlock(activeDigPos, false, golem);
                for (ItemStack drop : drops) {
                    ItemStack leftover = golem.addToCargo(drop);
                    if (!leftover.isEmpty()) {
                        Block.popResource(sl, activeDigPos, leftover);
                    }
                }
                golem.pruneExcessBuildingBlocks();
                golem.setWorking(false);

                if (findNearbyLavaFix(activeDigPos, 2)) {
                    targetPos = null;
                    tunnelBlockPos = null;
                    return;
                }

                if (activeDigPos.equals(tunnelBlockPos)) {
                    tunnelBlockPos = null;
                    golem.getNavigation().moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 1.05D);
                } else {
                    targetPos = null;
                    golem.setReservedTargetPos(null);
                    if (settings.isQuarryActive() && settings.hasQuarryBounds()) {
                        selectNextQuarryTask(settings);
                    } else if (golem.isCargoFull() || findTargetBlock() == null) {
                        if (needToClimbOut()) {
                            escaping = true;
                        }
                    }
                }
            }
        }

        private void tickBuildOrSeal() {
            double distSq = golem.position().distanceToSqr(
                    buildPos.getX() + 0.5D, buildPos.getY() + 0.5D, buildPos.getZ() + 0.5D);
            double maxReachSq = buildIsLavaFix ? 20.25D : 25.0D; // до 5 блоков с уступа/лестницы над пещерой

            if (distSq > 9.0D) {
                golem.setWorking(false);
                PlayerGolemSettings s = golem.getOwnerSettings();
                moveTowardQuarryOrBlock(buildPos, s.isQuarryActive() && s.hasQuarryBounds());

                if (Math.abs(distSq - lastDistToGoal) < 0.04D) {
                    stuckTicks++;
                } else {
                    stuckTicks = 0;
                    lastDistToGoal = distSq;
                }

                if (stuckTicks >= 40) {
                    // Если до этой точки стенки/лавы никак не дойти через открытую шахту — временно пропустим,
                    // чтобы построить следующий шаг лестницы и подойти ближе!
                    skipTemporarily(buildPos, 60);
                    buildPos = null;
                    buildState = null;
                    buildIsLavaFix = false;
                    stuckTicks = 0;
                    golem.setReservedTargetPos(null);
                    return;
                }

                if (distSq > maxReachSq || stuckTicks < 14) {
                    return;
                }
            }

            golem.getNavigation().stop();
            golem.getLookControl().setLookAt(buildPos.getX() + 0.5D, buildPos.getY() + 0.5D, buildPos.getZ() + 0.5D);
            golem.setWorking(true);

            digProgress++;
            int req = buildIsLavaFix ? 4 : 5;
            if (digProgress >= req && golem.level() instanceof ServerLevel sl) {
                digProgress = 0;
                stuckTicks = 0;
                BlockState current = sl.getBlockState(buildPos);

                if (buildIsLavaFix) {
                    sl.setBlock(buildPos, buildState, 3);
                    if (buildState.is(Blocks.STONE)) {
                        golem.consumeOneStoneIfPresent();
                    }
                    for (BlockPos near : BlockPos.betweenClosed(buildPos.offset(-1, -1, -1), buildPos.offset(1, 1, 1))) {
                        FluidState fs = sl.getFluidState(near);
                        if (fs.is(FluidTags.LAVA) && !fs.isSource()) {
                            sl.setBlock(near, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                    golem.playSound(SoundEvents.FIRE_EXTINGUISH, 0.28F, 1.1F + golem.getRandom().nextFloat() * 0.2F);
                    sl.sendParticles(ModParticles.GOLEM_SPARK,
                            buildPos.getX() + 0.5D, buildPos.getY() + 0.7D, buildPos.getZ() + 0.5D,
                            4, 0.25D, 0.25D, 0.25D, 0.03D);
                } else {
                    if (!current.isAir() && !current.liquid() && !current.getCollisionShape(sl, buildPos).isEmpty()) {
                        List<ItemStack> drops = Block.getDrops(current, sl, buildPos, null, golem, ItemStack.EMPTY);
                        sl.destroyBlock(buildPos, false, golem);
                        for (ItemStack d : drops) {
                            ItemStack left = golem.addToCargo(d);
                            if (!left.isEmpty()) Block.popResource(sl, buildPos, left);
                        }
                    }
                    if (golem.blockPosition().equals(buildPos)) {
                        golem.setPos(golem.getX(), buildPos.getY() + 1.05D, golem.getZ());
                    }
                    golem.consumeOneStoneIfPresent();
                    sl.setBlock(buildPos, buildState, 3);
                    golem.playSound(ModSounds.GOLEM_STEP, 0.25F, 0.95F);
                    sl.sendParticles(ModParticles.GOLEM_SPARK,
                            buildPos.getX() + 0.5D, buildPos.getY() + 0.6D, buildPos.getZ() + 0.5D,
                            3, 0.2D, 0.2D, 0.2D, 0.02D);
                }

                golem.setWorking(false);
                buildPos = null;
                buildState = null;
                buildIsLavaFix = false;
                golem.setReservedTargetPos(null);
            }
        }

        /**
         * Направляет копателя к цели. Если между големом и целью в карьере пустота (шахта/пещера),
         * голем идёт по уже построенным каменным ступенькам спиральной лестницы вниз к уровню цели!
         */
        private void moveTowardQuarryOrBlock(BlockPos goalPos, boolean inQuarry) {
            Path direct = golem.getNavigation().createPath(goalPos, 1);
            if (direct != null && direct.canReach()) {
                golem.getNavigation().moveTo(direct, 1.08D);
                return;
            }
            // Пробуем точку стояния прямо на блоке над целью или рядом с краем
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos beside = goalPos.relative(dir);
                if (hasSolidStandableGround(beside)) {
                    Path sidePath = golem.getNavigation().createPath(beside, 1);
                    if (sidePath != null && sidePath.canReach()) {
                        golem.getNavigation().moveTo(sidePath, 1.08D);
                        return;
                    }
                }
            }

            if (inQuarry) {
                PlayerGolemSettings s = golem.getOwnerSettings();
                if (s.isQuarryStairs()) {
                    BlockPos stairStep = findBestReachableStairStep(s, goalPos);
                    if (stairStep != null) {
                        golem.getNavigation().moveTo(stairStep.getX() + 0.5D, stairStep.getY() + 1.0D, stairStep.getZ() + 0.5D, 1.08D);
                        return;
                    }
                }
            }

            golem.getNavigation().moveTo(goalPos.getX() + 0.5D, goalPos.getY(), goalPos.getZ() + 0.5D, 1.05D);
        }

        private boolean hasSolidStandableGround(BlockPos airPos) {
            BlockState at = golem.level().getBlockState(airPos);
            BlockState below = golem.level().getBlockState(airPos.below());
            return (at.isAir() || at.getCollisionShape(golem.level(), airPos).isEmpty())
                    && !below.isAir() && !below.liquid() && !below.getCollisionShape(golem.level(), airPos.below()).isEmpty();
        }

        /**
         * Находит достижимую построенную ступеньку спиральной лестницы, которая ведёт ближе всего к цели по высоте/горизонтали.
         */
        private BlockPos findBestReachableStairStep(PlayerGolemSettings s, BlockPos goalPos) {
            int minX = s.getQuarryMinX(), maxX = s.getQuarryMaxX();
            int minZ = s.getQuarryMinZ(), maxZ = s.getQuarryMaxZ();
            int topY = s.getQuarryTopY(), bottomY = s.getQuarryBottomY();

            BlockPos best = null;
            double bestScore = Double.MAX_VALUE;
            for (int y = topY; y >= bottomY; y--) {
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos p = new BlockPos(x, y, z);
                        BlockState desired = QuarryPlanner.getDesiredStairBlock(s, p);
                        if (desired == null || QuarryPlanner.isSupportUnderStair(s, p)) continue;
                        if (!golem.level().getBlockState(p).is(desired.getBlock())) continue;
                        if (!golem.level().getBlockState(p.above()).isAir()) continue;
                        if (p.distSqr(golem.blockPosition()) < 1.2D) continue; // не стоять на месте на той же ступеньке

                        Path path = golem.getNavigation().createPath(p.above(), 1);
                        if (path == null || !path.canReach()) continue;

                        // Сильно поощряем ступеньки, которые приближают по высоте (или ниже к шахте) и ближе к цели
                        double dy = Math.abs(p.getY() - goalPos.getY());
                        double score = dy * 18.0D + p.distSqr(goalPos);
                        if (score < bestScore) {
                            bestScore = score;
                            best = p;
                        }
                    }
                }
            }
            return best;
        }

        private int measureAirDropBelow(BlockPos pos, int maxCheck) {
            int air = 0;
            for (int dy = 1; dy <= maxCheck; dy++) {
                BlockState st = golem.level().getBlockState(pos.below(dy));
                if (st.isAir() || st.getCollisionShape(golem.level(), pos.below(dy)).isEmpty()) {
                    air++;
                } else {
                    break;
                }
            }
            return air;
        }

        private boolean findNearbyLavaFix(BlockPos center, int r) {
            PlayerGolemSettings s = golem.getOwnerSettings();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            BlockState bestFix = null;

            for (int dy = 1; dy >= -1; dy--) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        BlockPos p = center.offset(dx, dy, dz);
                        if (isTempSkipped(p)) continue;
                        FluidState fs = golem.level().getFluidState(p);
                        if (!fs.is(FluidTags.LAVA)) continue;
                        double d = p.distSqr(golem.blockPosition());
                        if (d < bestDist) {
                            bestDist = d;
                            best = p;
                            boolean onBorderOrBottom = isOnQuarryOuterWallOrFloor(s, p) || fs.isSource();
                            bestFix = onBorderOrBottom ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        }
                    }
                }
            }
            if (best != null) {
                this.buildPos = best;
                this.buildState = bestFix;
                this.buildIsLavaFix = true;
                this.targetPos = null;
                golem.setReservedTargetPos(best);
                return true;
            }
            return false;
        }

        private boolean isOnQuarryOuterWallOrFloor(PlayerGolemSettings s, BlockPos p) {
            if (!s.isQuarryActive() || !s.hasQuarryBounds()) return true;
            return p.getX() < s.getQuarryMinX() || p.getX() > s.getQuarryMaxX()
                    || p.getZ() < s.getQuarryMinZ() || p.getZ() > s.getQuarryMaxZ()
                    || p.getY() < s.getQuarryBottomY();
        }

        /**
         * Выбор задачи в карьере:
         * При появлении шахты/пещеры под копателями сначала строится следующая ступенька спиральной лестницы
         * (всегда от края предыдущей ступеньки, поэтому 100% достижима!) и застраиваются прилегающие к ней края зоны выделения,
         * а также закрывается/сушится лава.
         */
        private boolean selectNextQuarryTask(PlayerGolemSettings s) {
            int minX = s.getQuarryMinX(), maxX = s.getQuarryMaxX();
            int minZ = s.getQuarryMinZ(), maxZ = s.getQuarryMaxZ();
            int topY = s.getQuarryTopY(), bottomY = s.getQuarryBottomY();

            if (entryPos == null) {
                entryPos = new BlockPos(minX, topY + 1, minZ);
            }

            List<DiggerGolemEntity> peerDiggers = golem.level().getEntitiesOfClass(
                    DiggerGolemEntity.class,
                    golem.getBoundingBox().inflate(32.0D),
                    other -> other != golem && other.isAlive()
            );

            for (int y = topY; y >= bottomY; y--) {
                // 1. Лава на уровне y (или y - 1)
                BlockPos bestLava = null;
                BlockState bestLavaFix = null;
                double bestLavaDist = Double.MAX_VALUE;
                for (int checkY = y; checkY >= Math.max(bottomY - 1, y - 1); checkY--) {
                    for (int x = minX - 1; x <= maxX + 1; x++) {
                        for (int z = minZ - 1; z <= maxZ + 1; z++) {
                            BlockPos p = new BlockPos(x, checkY, z);
                            if (isTempSkipped(p)) continue;
                            FluidState fs = golem.level().getFluidState(p);
                            if (!fs.is(FluidTags.LAVA)) continue;
                            if (isClaimedByCloserPeer(p, peerDiggers)) continue;
                            double d = p.distSqr(golem.blockPosition());
                            if (d < bestLavaDist) {
                                bestLavaDist = d;
                                bestLava = p;
                                boolean sealStone = isOnQuarryOuterWallOrFloor(s, p) || fs.isSource();
                                bestLavaFix = sealStone ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState();
                            }
                        }
                    }
                }
                if (bestLava != null) {
                    this.buildPos = bestLava;
                    this.buildState = bestLavaFix;
                    this.buildIsLavaFix = true;
                    this.targetPos = null;
                    golem.setReservedTargetPos(bestLava);
                    return true;
                }

                // 2. Спиральная каменная лестница на слое y (и опора под ней на y - 1) —
                // прокладывается прямо вниз в шахту, открывая спуск к нижним краям пещеры!
                if (s.isQuarryStairs()) {
                    BlockPos bestStair = null;
                    BlockState bestStairState = null;
                    double bestStairDist = Double.MAX_VALUE;

                    for (int checkY = y; checkY >= Math.max(bottomY - 1, y - 1); checkY--) {
                        for (int x = minX; x <= maxX; x++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                BlockPos p = new BlockPos(x, checkY, z);
                                if (isTempSkipped(p)) continue;
                                BlockState desired = QuarryPlanner.getDesiredStairBlock(s, p);
                                if (desired == null) continue;
                                BlockState cur = golem.level().getBlockState(p);
                                if (QuarryPlanner.isSupportUnderStair(s, p)) {
                                    if (!cur.isAir() && !cur.liquid() && !cur.getCollisionShape(golem.level(), p).isEmpty()) continue;
                                } else if (cur.is(desired.getBlock())) {
                                    if (!desired.hasProperty(StairBlock.FACING)
                                            || cur.getValue(StairBlock.FACING) == desired.getValue(StairBlock.FACING)) {
                                        continue;
                                    }
                                }
                                if (!cur.isAir() && !cur.liquid() && !isBreakableForTunnel(p)) continue;
                                if (isClaimedByCloserPeer(p, peerDiggers)) continue;

                                double d = p.distSqr(golem.blockPosition());
                                if (d < bestStairDist) {
                                    bestStairDist = d;
                                    bestStair = p;
                                    bestStairState = desired;
                                }
                            }
                        }
                    }
                    if (bestStair != null) {
                        BlockState cur = golem.level().getBlockState(bestStair);
                        if (!cur.isAir() && !cur.liquid()) {
                            this.targetPos = bestStair;
                            this.buildPos = null;
                            this.buildState = null;
                            this.buildIsLavaFix = false;
                            golem.setReservedTargetPos(bestStair);
                        } else {
                            this.buildPos = bestStair;
                            this.buildState = bestStairState;
                            this.buildIsLavaFix = false;
                            this.targetPos = null;
                            golem.setReservedTargetPos(bestStair);
                        }
                        return true;
                    }
                }

                // 3. Застройка краёв зоны выделения в пещере/шахте (на уровне y и y - 1)
                // Выбираем края рядом с доступными блоками/ступеньками (чтобы не пытаться тянуться через всю пустую шахту)
                BlockPos bestWall = null;
                double bestWallDist = Double.MAX_VALUE;
                for (int checkY = y; checkY >= Math.max(bottomY, y - 1); checkY--) {
                    for (int x = minX - 1; x <= maxX + 1; x++) {
                        for (int z = minZ - 1; z <= maxZ + 1; z++) {
                            boolean isOuterBorder = (x == minX - 1 || x == maxX + 1 || z == minZ - 1 || z == maxZ + 1);
                            if (!isOuterBorder) continue;
                            BlockPos p = new BlockPos(x, checkY, z);
                            if (isTempSkipped(p)) continue;
                            BlockState cur = golem.level().getBlockState(p);
                            boolean needsWall = cur.isAir() || cur.liquid() || cur.getCollisionShape(golem.level(), p).isEmpty();
                            if (!needsWall) continue;
                            if (!isCaveEdgeExposed(s, p, checkY)) continue;
                            if (isClaimedByCloserPeer(p, peerDiggers)) continue;

                            double d = p.distSqr(golem.blockPosition());
                            if (d < bestWallDist) {
                                bestWallDist = d;
                                bestWall = p;
                            }
                        }
                    }
                }
                if (bestWall != null) {
                    this.buildPos = bestWall;
                    this.buildState = Blocks.STONE.defaultBlockState();
                    this.buildIsLavaFix = false;
                    this.targetPos = null;
                    golem.setReservedTargetPos(bestWall);
                    return true;
                }

                // 4. Выкопка обычных блоков текущего слоя y (с защитой лестницы)
                BlockPos bestDig = null;
                double bestScore = Double.MAX_VALUE;
                BlockPos fallback = null;
                double fallbackScore = Double.MAX_VALUE;

                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos p = new BlockPos(x, y, z);
                        if (isTempSkipped(p)) continue;
                        if (!isValidQuarryDigTarget(p, s)) continue;

                        double myDist = p.distSqr(golem.blockPosition());
                        if (isClaimedByCloserPeer(p, peerDiggers)) {
                            if (myDist < fallbackScore) {
                                fallbackScore = myDist;
                                fallback = p;
                            }
                        } else if (myDist < bestScore) {
                            bestScore = myDist;
                            bestDig = p;
                        }
                    }
                }

                BlockPos chosen = bestDig != null ? bestDig : fallback;
                if (chosen != null) {
                    this.targetPos = chosen;
                    this.buildPos = null;
                    this.buildState = null;
                    this.buildIsLavaFix = false;
                    golem.setReservedTargetPos(chosen);
                    return true;
                }
            }
            return false;
        }

        private boolean isCaveEdgeExposed(PlayerGolemSettings s, BlockPos wallPos, int y) {
            BlockState st = golem.level().getBlockState(wallPos);
            if (!st.getFluidState().isEmpty()) return true;
            if (!golem.level().canSeeSky(wallPos)) return true;
            if (y < s.getQuarryTopY()) {
                int inX = Math.max(s.getQuarryMinX(), Math.min(s.getQuarryMaxX(), wallPos.getX()));
                int inZ = Math.max(s.getQuarryMinZ(), Math.min(s.getQuarryMaxZ(), wallPos.getZ()));
                BlockPos inside = new BlockPos(inX, y, inZ);
                if (!golem.level().canSeeSky(inside) || measureAirDropBelow(wallPos, 3) >= 2) {
                    return true;
                }
            }
            return false;
        }

        private boolean isClaimedByCloserPeer(BlockPos pos, List<DiggerGolemEntity> peerDiggers) {
            double myDist = pos.distSqr(golem.blockPosition());
            for (DiggerGolemEntity peer : peerDiggers) {
                BlockPos peerTarget = peer.getReservedTargetPos();
                if (peerTarget != null && peerTarget.distSqr(pos) <= 2.5D) {
                    double peerDist = pos.distSqr(peer.blockPosition());
                    if (peerDist + 0.5D < myDist || (Math.abs(peerDist - myDist) <= 0.5D && peer.getId() < golem.getId())) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isValidQuarryDigTarget(BlockPos pos, PlayerGolemSettings s) {
            if (pos.getX() < s.getQuarryMinX() || pos.getX() > s.getQuarryMaxX()) return false;
            if (pos.getZ() < s.getQuarryMinZ() || pos.getZ() > s.getQuarryMaxZ()) return false;
            if (pos.getY() > s.getQuarryTopY() || pos.getY() < s.getQuarryBottomY()) return false;

            BlockState state = golem.level().getBlockState(pos);
            if (state.isAir() || state.liquid()) return false;
            float speed = state.getDestroySpeed(golem.level(), pos);
            if (speed < 0.0F || speed > 55.0F) return false;
            if (golem.level().getBlockEntity(pos) != null) return false;
            if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL)
                    || state.is(Blocks.FARMLAND) || state.is(Blocks.TORCH)) {
                return false;
            }

            BlockState desiredStair = QuarryPlanner.getDesiredStairBlock(s, pos);
            if (desiredStair != null) {
                if (QuarryPlanner.isSupportUnderStair(s, pos)) {
                    return false;
                }
                if (state.is(desiredStair.getBlock())) {
                    if (!desiredStair.hasProperty(StairBlock.FACING)) return false;
                    return state.getValue(StairBlock.FACING) != desiredStair.getValue(StairBlock.FACING);
                }
                return true;
            }

            if (s.isQuarryStairs()) {
                for (int dy = 1; dy <= 3; dy++) {
                    if (QuarryPlanner.getDesiredStairBlock(s, pos.below(dy)) != null) {
                        return true;
                    }
                }
            }

            String extractFilter = s.getExtractFilter();
            String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            String fullId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            if (extractFilter != null && !extractFilter.isEmpty()) {
                return matchesExtractFilter(path, fullId, extractFilter);
            }
            if (s.getDigMode() == DigMode.ORES_ONLY) {
                return path.contains("_ore") || state.is(Blocks.ANCIENT_DEBRIS)
                        || state.is(Blocks.RAW_IRON_BLOCK) || state.is(Blocks.RAW_COPPER_BLOCK) || state.is(Blocks.RAW_GOLD_BLOCK);
            }
            return true;
        }

        private boolean needToClimbOutForChest(BlockPos chestPos) {
            return golem.blockPosition().getY() < chestPos.getY() - 1;
        }

        private boolean needToClimbOut() {
            if (entryPos == null) return false;
            if (golem.blockPosition().getY() >= entryPos.getY()) {
                entryPos = null;
                return false;
            }
            return true;
        }

        private void tickEscapeToSurface() {
            if (entryPos == null || golem.blockPosition().getY() >= entryPos.getY()) {
                entryPos = null;
                escaping = false;
                golem.setWorking(false);
                return;
            }

            Path path = golem.getNavigation().createPath(entryPos, 1);
            if (path != null && path.canReach()) {
                golem.setWorking(false);
                golem.getNavigation().moveTo(path, 1.12D);
                return;
            }

            BlockPos pos = golem.blockPosition();
            BlockPos headAbove = pos.above(1);
            BlockPos roofAbove = pos.above(2);
            if (isBreakableForTunnel(headAbove)) {
                mineEscapeBlock(headAbove);
                return;
            }
            if (isBreakableForTunnel(roofAbove)) {
                mineEscapeBlock(roofAbove);
                return;
            }

            int stepX = Integer.compare(entryPos.getX(), pos.getX());
            int stepZ = Integer.compare(entryPos.getZ(), pos.getZ());
            if (stepX != 0 || stepZ != 0) {
                BlockPos sideUp = pos.offset(stepX, 1, stepZ);
                BlockPos sideMid = pos.offset(stepX, 0, stepZ);
                if (isBreakableForTunnel(sideUp)) {
                    mineEscapeBlock(sideUp);
                    return;
                }
                if (isBreakableForTunnel(sideMid)) {
                    mineEscapeBlock(sideMid);
                    return;
                }
            }

            golem.setWorking(false);
            if (golem.onGround() && golem.level() instanceof ServerLevel sl) {
                BlockState scaffold = golem.takeOneScaffoldBlock();
                if (scaffold != null && sl.getBlockState(pos).isAir()) {
                    golem.setPos(pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D);
                    sl.setBlock(pos, scaffold, 3);
                    golem.playSound(ModSounds.GOLEM_STEP, 0.25F, 1.0F);
                } else {
                    golem.getJumpControl().jump();
                    golem.getNavigation().moveTo(entryPos.getX() + 0.5D, entryPos.getY(), entryPos.getZ() + 0.5D, 1.15D);
                }
            }
        }

        private void mineEscapeBlock(BlockPos pos) {
            golem.getNavigation().stop();
            golem.getLookControl().setLookAt(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            golem.setWorking(true);
            digProgress++;
            if (digProgress >= 12 && golem.level() instanceof ServerLevel sl) {
                digProgress = 0;
                BlockState st = sl.getBlockState(pos);
                List<ItemStack> drops = Block.getDrops(st, sl, pos, sl.getBlockEntity(pos), golem, ItemStack.EMPTY);
                sl.destroyBlock(pos, false, golem);
                for (ItemStack d : drops) {
                    ItemStack left = golem.addToCargo(d);
                    if (!left.isEmpty()) Block.popResource(sl, pos, left);
                }
                golem.pruneExcessBuildingBlocks();
                golem.setWorking(false);
            }
        }

        private BlockPos findBlockingStepToward(BlockPos target) {
            BlockPos me = golem.blockPosition();
            int dx = Integer.compare(target.getX(), me.getX());
            int dy = Integer.compare(target.getY(), me.getY());
            int dz = Integer.compare(target.getZ(), me.getZ());

            List<BlockPos> candidates = new ArrayList<>();
            if (dy < 0) {
                if (dx != 0 || dz != 0) {
                    candidates.add(me.offset(dx, 0, dz));
                    candidates.add(me.offset(dx, -1, dz));
                }
                candidates.add(me.below());
                if (dx != 0) candidates.add(me.offset(dx, 0, 0));
                if (dz != 0) candidates.add(me.offset(0, 0, dz));
            } else {
                if (dx != 0 || dz != 0) {
                    candidates.add(me.offset(dx, 0, dz));
                    candidates.add(me.offset(dx, 1, dz));
                }
                if (dy > 0) candidates.add(me.above());
            }
            PlayerGolemSettings s = golem.getOwnerSettings();
            for (BlockPos c : candidates) {
                if (isBreakableForTunnel(c) && !QuarryPlanner.isProtectedStairPos(s, c)) return c;
            }
            return null;
        }

        private boolean hasLineOfSightToBlock(BlockPos pos) {
            for (Direction dir : Direction.values()) {
                BlockPos adj = pos.relative(dir);
                BlockState st = golem.level().getBlockState(adj);
                if (st.isAir() || st.getCollisionShape(golem.level(), adj).isEmpty()) {
                    if (adj.distSqr(golem.blockPosition()) <= pos.distSqr(golem.blockPosition()) + 1.5D) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isBreakableForTunnel(BlockPos pos) {
            BlockState state = golem.level().getBlockState(pos);
            if (state.isAir() || state.liquid()) return false;
            float speed = state.getDestroySpeed(golem.level(), pos);
            if (speed < 0.0F || speed > 55.0F) return false;
            if (golem.level().getBlockEntity(pos) != null) return false;
            return !state.is(Blocks.CHEST) && !state.is(Blocks.TRAPPED_CHEST) && !state.is(Blocks.BARREL);
        }

        private BlockPos findTargetBlock() {
            PlayerGolemSettings settings = golem.getOwnerSettings();
            int radius = settings.getDiggerRadius();
            DigMode mode = settings.getDigMode();
            String extractFilter = settings.getExtractFilter();

            BlockPos center = golem.getHomePos();
            if (center == null) {
                LivingEntity owner = golem.getOwnerEntity();
                center = (settings.isFollowOwner() && owner != null) ? owner.blockPosition() : golem.blockPosition();
            }

            int depth = settings.getDiggerDepth();
            int minY = mode == DigMode.TUNNEL ? center.getY() : center.getY() - depth;
            int maxY = mode == DigMode.TUNNEL ? center.getY() + 1 : center.getY() + 3;

            List<DiggerGolemEntity> peerDiggers = golem.level().getEntitiesOfClass(
                    DiggerGolemEntity.class,
                    golem.getBoundingBox().inflate(24.0D),
                    other -> other != golem && other.isAlive()
            );

            BlockPos best = null;
            double bestScore = Double.MAX_VALUE;
            BlockPos fallback = null;
            double fallbackScore = Double.MAX_VALUE;

            for (int y = maxY; y >= minY; y--) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                        if (!isValidDigTarget(pos, mode, extractFilter)) continue;

                        double myDist = pos.distSqr(golem.blockPosition());
                        double score = myDist + Math.abs(golem.blockPosition().getY() - y) * 3.0D;
                        if (isClaimedByCloserPeer(pos, peerDiggers)) {
                            if (score < fallbackScore) {
                                fallbackScore = score;
                                fallback = pos;
                            }
                        } else if (score < bestScore) {
                            bestScore = score;
                            best = pos;
                        }
                    }
                }
            }
            return best != null ? best : fallback;
        }

        private boolean isValidDigTarget(BlockPos pos, DigMode mode, String extractFilter) {
            BlockState state = golem.level().getBlockState(pos);
            if (state.isAir() || state.liquid()) return false;
            float speed = state.getDestroySpeed(golem.level(), pos);
            if (speed < 0.0F || speed > 55.0F) return false;
            if (golem.level().getBlockEntity(pos) != null) return false;
            if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL)
                    || state.is(Blocks.FARMLAND) || state.is(Blocks.TORCH)) {
                return false;
            }
            String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            String fullId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            boolean isOre = path.contains("_ore") || state.is(Blocks.ANCIENT_DEBRIS)
                    || state.is(Blocks.RAW_IRON_BLOCK) || state.is(Blocks.RAW_COPPER_BLOCK) || state.is(Blocks.RAW_GOLD_BLOCK);

            if (extractFilter != null && !extractFilter.isEmpty()) {
                return matchesExtractFilter(path, fullId, extractFilter);
            }
            if (mode == DigMode.ORES_ONLY) {
                return isOre;
            }
            if (pos.equals(golem.blockPosition().below()) && !isOre) return false;
            return true;
        }

        private boolean matchesExtractFilter(String path, String fullId, String filter) {
            String f = filter.toLowerCase();
            if (fullId.equalsIgnoreCase(f) || path.equals(f)) return true;
            return switch (f) {
                case "iron" -> path.contains("iron_ore") || path.equals("raw_iron_block");
                case "gold" -> path.contains("gold_ore") || path.equals("raw_gold_block");
                case "copper" -> path.contains("copper_ore") || path.equals("raw_copper_block");
                case "coal" -> path.contains("coal_ore");
                case "diamond" -> path.contains("diamond_ore");
                case "emerald" -> path.contains("emerald_ore");
                case "lapis" -> path.contains("lapis_ore");
                case "redstone" -> path.contains("redstone_ore");
                case "quartz" -> path.contains("quartz_ore");
                case "debris", "netherite" -> path.equals("ancient_debris");
                case "ores" -> path.contains("_ore") || path.equals("ancient_debris");
                default -> path.contains(f);
            };
        }
    }
}
