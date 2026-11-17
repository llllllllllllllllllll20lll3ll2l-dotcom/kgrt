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
 * Планировщик спиральной каменной лестницы по периметру размеченного карьера.
 * Позволяет игрокам и големам спокойно спускаться на дно карьера и подниматься наверх пешком.
 */
public final class QuarryPlanner {
    private QuarryPlanner() {}

    public record PerimeterCell(int x, int z, boolean corner, Direction climbDirection) {}

    public static List<PerimeterCell> buildPerimeter(int minX, int maxX, int minZ, int maxZ) {
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
        List<PerimeterCell> cells = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int[] cur = raw.get(i);
            int[] prev = raw.get((i - 1 + n) % n);
            int[] next = raw.get((i + 1) % n);

            int pdx = prev[0] - cur[0];
            int pdz = prev[1] - cur[1];
            int ndx = cur[0] - next[0];
            int ndz = cur[1] - next[1];
            boolean corner = (pdx != ndx || pdz != ndz);

            Direction climb = Direction.NORTH;
            if (pdx > 0) climb = Direction.EAST;
            else if (pdx < 0) climb = Direction.WEST;
            else if (pdz > 0) climb = Direction.SOUTH;
            else if (pdz < 0) climb = Direction.NORTH;

            cells.add(new PerimeterCell(cur[0], cur[1], corner, climb));
        }
        return cells;
    }

    public static int findPerimeterIndex(List<PerimeterCell> perimeter, int x, int z) {
        for (int i = 0; i < perimeter.size(); i++) {
            PerimeterCell c = perimeter.get(i);
            if (c.x() == x && c.z() == z) return i;
        }
        return -1;
    }

    /**
     * Возвращает желаемый блок спиральной каменной лестницы в точке pos,
     * либо null, если в этой позиции должен быть свободный проход/выкопанный блок.
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

        List<PerimeterCell> perimeter = buildPerimeter(minX, maxX, minZ, maxZ);
        int idx = findPerimeterIndex(perimeter, pos.getX(), pos.getZ());
        if (idx < 0) return null;

        int p = perimeter.size();
        // На глубине (topY - y) % p == idx лежит сама ступенька / угловая площадка
        if (y <= topY && y >= bottomY && Math.floorMod(topY - y, p) == idx) {
            PerimeterCell cell = perimeter.get(idx);
            return Blocks.STONE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, cell.climbDirection())
                    .setValue(StairBlock.HALF, Half.BOTTOM)
                    .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
        }

        // Ровно на 1 блок ниже каждой ступеньки — опорный каменный блок (чтобы лестница была сплошной и прочной)
        int yAbove = y + 1;
        if (yAbove <= topY && yAbove >= bottomY && Math.floorMod(topY - yAbove, p) == idx) {
            return Blocks.COBBLESTONE.defaultBlockState();
        }

        return null;
    }

    /**
     * Защита блоков построенной спиральной лестницы от выкапывания големами.
     */
    public static boolean isProtectedStairPos(PlayerGolemSettings settings, BlockPos pos) {
        return getDesiredStairBlock(settings, pos) != null;
    }
}
