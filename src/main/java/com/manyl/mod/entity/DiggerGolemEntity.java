package com.manyl.mod.entity;

import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.entity.ai.FollowOwnerGoal;
import com.manyl.mod.registry.ModParticles;
import com.manyl.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Голем-копатель (макс. 4 на игрока).
 * В версии 1.1:
 * - распределяется с другими копателями: к руде под землёй идёт ближайший копатель;
 * - если руда под землёй закрыта блоками — прокладывает туннель/лестницу вниз к ней, а после добычи выбирается обратно наверх;
 * - если инвентарь полностью заполняется второстепенными стройблоками, оставляет ровно 1 стак стройблоков, а остальные выкидывает (руду НИКОГДА не выкидывает).
 */
public class DiggerGolemEntity extends BaseGolemEntity {
    private BlockPos reservedTargetPos;

    public DiggerGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public GolemRole getRole() {
        return GolemRole.DIGGER;
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

    /**
     * Если инвентарь полон или перегружен второстепенными стройблоками,
     * оставляет только 1 самый полный стак стройблоков, а остальные стройблоки выбрасывает.
     * Руды, самородки, слитки, еда и ценные ресурсы НИКОГДА не выбрасываются.
     */
    public boolean pruneExcessBuildingBlocks() {
        if (level().isClientSide()) return false;
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

        if (trashSlots.size() <= 1) {
            return false; // уже остался максимум 1 стак стройблоков
        }

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
        if (droppedAny) {
            inventory.setChanged();
        }
        return droppedAny;
    }

    /**
     * Определяет, является ли предмет второстепенным строительным блоком (земля, камень, сланец, гравий и т.п.).
     * Руды и ценные ресурсы всегда возвращают false.
     */
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

    /**
     * Пытается взять 1 строительный блок из инвентаря для подъёма по ступенькам из шахты.
     */
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

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new DiggerWorkGoal(this));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.05D, 7.0F, 3.0F));
    }

    private static class DiggerWorkGoal extends Goal {
        private final DiggerGolemEntity golem;
        private BlockPos targetPos;
        private BlockPos tunnelBlockPos; // промежуточный блок при прокопке к руде или выходе наверх
        private BlockPos entryPos;       // позиция на поверхности перед спуском под землю
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

        @Override
        public boolean canUse() {
            if (golem.isSitting() || golem.getHunger() <= 5) return false;
            if (scanCooldown > 0) {
                scanCooldown--;
                return false;
            }
            scanCooldown = 12;

            // Если застрял под землёй ниже входа — сначала выбираемся или продолжаем копать рядом
            if (golem.isCargoFull()) {
                golem.pruneExcessBuildingBlocks();
            }
            if (golem.isCargoFull()) {
                if (needToClimbOut()) {
                    escaping = true;
                    return true;
                }
                depositChestPos = golem.findNearestContainerPos(10);
                return depositChestPos != null;
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
                depositChestPos = golem.findNearestContainerPos(10);
                return depositChestPos != null;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (golem.isSitting() || golem.getHunger() <= 5) return false;
            if (escaping) {
                return needToClimbOut();
            }
            if (depositChestPos != null) {
                return golem.hasCargo() && golem.level().getBlockEntity(depositChestPos) instanceof Container;
            }
            PlayerGolemSettings s = golem.getOwnerSettings();
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
                if (needToClimbOut()) {
                    escaping = true;
                    return;
                }
                golem.setWorking(false);
                double dist = golem.position().distanceToSqr(
                        depositChestPos.getX() + 0.5D, depositChestPos.getY() + 0.5D, depositChestPos.getZ() + 0.5D);
                if (dist > 4.5D) {
                    golem.getNavigation().moveTo(depositChestPos.getX() + 0.5D, depositChestPos.getY(), depositChestPos.getZ() + 0.5D, 1.05D);
                } else {
                    golem.getNavigation().stop();
                    BlockEntity be = golem.level().getBlockEntity(depositChestPos);
                    if (be instanceof Container container && golem.depositCargoIntoContainer(container)) {
                        golem.playSound(ModSounds.GOLEM_WORK_SORT, 0.7F, 1.0F);
                    }
                    depositChestPos = null;
                }
                return;
            }

            if (targetPos == null) return;

            // Если копатель передумал или другой копатель оказался заметно ближе — не мешаем, но обычно дорабатывает свою бронь
            BlockPos activeDigPos = (tunnelBlockPos != null && isBreakableForTunnel(tunnelBlockPos)) ? tunnelBlockPos : targetPos;

            double distToTarget = golem.position().distanceToSqr(
                    targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D);

            // Проверяем, не закрыта ли руда землёй (под нами или за стенкой)
            if (tunnelBlockPos == null && (distToTarget > 6.25D || !hasLineOfSightToBlock(targetPos))) {
                if (distToTarget <= 6.25D && !hasLineOfSightToBlock(targetPos)) {
                    // Руда рядом под землёй, но перекрыта блоком — пробиваем верхний/передний слой!
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

                    // Если путь заблокирован (руда под землёй +-2 блока или за стеной) — начинаем копаться к ней!
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
                golem.getNavigation().moveTo(activeDigPos.getX() + 0.5D, activeDigPos.getY(), activeDigPos.getZ() + 0.5D, 1.08D);
                return;
            }

            // Копаем активный блок (либо препятствие на пути, либо саму руду)
            golem.getNavigation().stop();
            golem.getLookControl().setLookAt(activeDigPos.getX() + 0.5D, activeDigPos.getY() + 0.5D, activeDigPos.getZ() + 0.5D);
            golem.setWorking(true);

            digProgress++;
            if (digProgress % 8 == 1) {
                golem.playSound(ModSounds.GOLEM_WORK_DIG, 0.7F, 0.9F + golem.getRandom().nextFloat() * 0.2F);
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

                if (activeDigPos.equals(tunnelBlockPos)) {
                    tunnelBlockPos = null;
                    // Подталкиваем голема шагнуть в прорытый проход к руде
                    golem.getNavigation().moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, 1.05D);
                } else {
                    // Руда добыта! Проверяем, есть ли ещё руды под землёй рядом, иначе выбираемся на поверхность
                    targetPos = null;
                    golem.setReservedTargetPos(null);
                    if (golem.isCargoFull() || findTargetBlock() == null) {
                        if (needToClimbOut()) {
                            escaping = true;
                        }
                    }
                }
            }
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

            // Пробуем сначала пройти обычной навигацией наверх
            Path path = golem.getNavigation().createPath(entryPos, 1);
            if (path != null && path.canReach()) {
                golem.setWorking(false);
                golem.getNavigation().moveTo(path, 1.12D);
                return;
            }

            // Иначе прорубаем себе выход наверх и при необходимости ставим строительный блок под ноги!
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

            // Смотрим шаг по горизонтали к entryPos на уровне y+1 и y
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

            // Если над головой открыто — прыгаем и ставим 1 строительный блок под ноги, чтобы выбраться из ямы!
            golem.setWorking(false);
            if (golem.onGround() && golem.level() instanceof ServerLevel sl) {
                BlockState scaffold = golem.takeOneScaffoldBlock();
                if (scaffold != null && sl.getBlockState(pos).isAir()) {
                    golem.setPos(pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D);
                    sl.setBlock(pos, scaffold, 3);
                    golem.playSound(ModSounds.GOLEM_STEP, 0.7F, 1.0F);
                } else {
                    // Запасной мягкий прыжок-подъём к краю ямы
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
            for (BlockPos c : candidates) {
                if (isBreakableForTunnel(c)) return c;
            }
            return null;
        }

        private boolean hasLineOfSightToBlock(BlockPos pos) {
            // Считаем, что блок открыт, если хотя бы одна из 6 соседних граней воздух/проходима и ближе к голему
            for (var dir : net.minecraft.core.Direction.values()) {
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

            int minY = mode == DigMode.TUNNEL ? center.getY() : center.getY() - 4;
            int maxY = mode == DigMode.TUNNEL ? center.getY() + 1 : center.getY() + 3;

            // Собираем всех союзных копателей рядом для распределения целей
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
                        boolean claimedByCloserPeer = false;
                        for (DiggerGolemEntity peer : peerDiggers) {
                            BlockPos peerTarget = peer.getReservedTargetPos();
                            if (peerTarget != null && peerTarget.distSqr(pos) <= 2.5D) {
                                double peerDist = pos.distSqr(peer.blockPosition());
                                if (peerDist + 0.5D < myDist || (Math.abs(peerDist - myDist) <= 0.5D && peer.getId() < golem.getId())) {
                                    claimedByCloserPeer = true;
                                    break;
                                }
                            }
                        }

                        double score = myDist + Math.abs(golem.blockPosition().getY() - y) * 3.0D;
                        if (claimedByCloserPeer) {
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
            // В режиме обычной копки не выкапываем блок ровно под собой, если это не целевая руда / фильтр
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
