package com.manyl.mod.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Планировщик ровной спиральной каменной лестницы по периметру карьера:
 * - на прямых стенах ставятся каменные ступеньки (STONE_STAIRS) строго в сторону подъёма;
 * - на углах ставятся ровные каменные площадки (STONE) вровень с выходящей ступенькой — без перекосов и прыжков;
 * - опора под ступенькой/площадкой — камень (STONE), без булыжниковых заплаток.
 */
public final class QuarryPlanner {
    private QuarryPlanner() {}

    public record PerimeterCell(int x, int z, boolean corner, Direction climbDirection, int dropOffset) {}

    public record PerimeterLayout(List<PerimeterCell> cells, int loopDrop) {}

    public static PerimeterLayout buildPerimeter(int minX, int maxX, int minZ, int maxZ) {
        List<int[]> raw = new ArrayList<>();
        // Верх (minZ): слева направо
        for (int x = minX; x <= maxX; x++) raw.add(new int[]{x, minZ});
        // Право (maxX): сверху вниз
        for (int z = minZ + 1; z <= maxZ; z++) raw.add(new int[]{maxX, z});
        // Низ (maxZ): справа налево
        if (maxZ > minZ) {
            for (int x = maxX - 1; x >= minX; x--) raw.add(new int[]{x, maxZ});
        }
        // Лево (minX): снизу вверх
        if (maxX > minX) {
            for (int z = maxZ - 1; z >= minZ + 1; z--) raw.add(new int[]{minX, z});
        }

        int n = raw.size();
        boolean[] corners = new boolean[n];
        Direction[] climbs = new Direction[n];

        for (int i = 0; i < n; i++) {
            int[] cur = raw.get(i);
            int[] prev = raw.get((i - 1 + n) % n);
            int[] next = raw.get((i + 1) % n);

            int pdx = prev[0] - cur[0];
            int pdz = prev[1] - cur[1];
            int ndx = cur[0] - next[0];
            int ndz = cur[1] - next[1];
            corners[i] = (pdx != ndx || pdz != ndz);

            Direction climb = Direction.NORTH;
            if (pdx > 0) climb = Direction.EAST;
            else if (pdx < 0) climb = Direction.WEST;
            else if (pdz > 0) climb = Direction.SOUTH;
            else if (pdz < 0) climb = Direction.NORTH;
            climbs[i] = climb;
        }

        // Высота спускается на 1 после каждой прямой ступеньки и сохраняется при выходе с угловой площадки
        List<PerimeterCell> cells = new ArrayList<>(n);
        int drop = 0;
        for (int i = 0; i < n; i++) {
            if (i > 0 && !corners[i - 1]) {
                drop++;
            }
            cells.add(new PerimeterCell(raw.get(i)[0], raw.get(i)[1], corners[i], climbs[i], drop));
        }
        int loopDrop = drop + (n > 0 && !corners[n - 1] ? 1 : 0);
        return new PerimeterLayout(cells, Math.max(1, loopDrop));
    }

    public static PerimeterCell findCell(PerimeterLayout layout, int x, int z) {
        for (PerimeterCell c : layout.cells()) {
            if (c.x() == x && c.z() == z) return c;
        }
        return null;
    }

    /**
     * Возвращает желаемый блок лестницы в точке pos (ступенька или угловая площадка),
     * либо опорный камень ровно под ступенькой (y - 1), либо null (проход / выкапываемый блок).
     */
    public static BlockState getDesiredStairBlock(PlayerGolemSettings settings, BlockPos pos) {
        if (!settings.isQuarryActive() || !settings.hasQuarryBounds() || !settings.isQuarryStairs()) {
            return null;
        }
        int minX = settings.getQuarryMinX();
        int maxX = settings.getQuarryMaxX();
        int minZ = settings.getQuarryMinZ();
        int maxZ = settings.getQuarryMaxZ();
        if (maxX - minX + 1 < 3 || maxZ - minZ + 1 < 3) {
            return null;
        }
        if (pos.getX() < minX || pos.getX() > maxX || pos.getZ() < minZ || pos.getZ() > maxZ) {
            return null;
        }
        int topY = settings.getQuarryTopY();
        int bottomY = settings.getQuarryBottomY();
        int y = pos.getY();
        if (y > topY || y < bottomY - 1) {
            return null;
        }

        PerimeterLayout layout = buildPerimeter(minX, maxX, minZ, maxZ);
        PerimeterCell cell = findCell(layout, pos.getX(), pos.getZ());
        if (cell == null) return null;

        int loopDrop = layout.loopDrop();
        int targetMod = Math.floorMod(cell.dropOffset(), loopDrop);

        // 1. Сама ступенька или ровная угловая площадка на данном уровне
        if (y <= topY && y >= bottomY && Math.floorMod(topY - y, loopDrop) == targetMod) {
            if (cell.corner()) {
                return Blocks.STONE.defaultBlockState();
            }
            return Blocks.STONE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, cell.climbDirection())
                    .setValue(StairBlock.HALF, Half.BOTTOM)
                    .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
        }

        // 2. Опорный камень строго под ступенькой/площадкой (на y + 1 лежит ступенька)
        int yAbove = y + 1;
        if (yAbove <= topY && yAbove >= bottomY && Math.floorMod(topY - yAbove, loopDrop) == targetMod) {
            return Blocks.STONE.defaultBlockState();
        }

        return null;
    }

    /**
     * Проверяет, является ли позиция опорным блоком под ступенькой (а не самой ступенькой).
     * Для опоры достаточно любого твёрдого камня (не обязательно перестраивать стену).
     */
    public static boolean isSupportUnderStair(PlayerGolemSettings settings, BlockPos pos) {
        if (!settings.isQuarryActive() || !settings.hasQuarryBounds() || !settings.isQuarryStairs()) {
            return false;
        }
        BlockState aboveDesired = getDesiredStairBlock(settings, pos.above());
        if (aboveDesired == null) return false;
        // Если блок выше — ступенька или угловая площадка, то pos — опора под ним
        return !isSupportUnderStair(settings, pos.above());
    }

    public static boolean isProtectedStairPos(PlayerGolemSettings settings, BlockPos pos) {
        return getDesiredStairBlock(settings, pos) != null;
    }
}
