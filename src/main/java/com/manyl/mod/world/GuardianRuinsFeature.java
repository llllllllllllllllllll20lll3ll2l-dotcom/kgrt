package com.manyl.mod.world;

import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.registry.ModEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Древние руины со спящим (сломанным) стражем.
 * Игрок может починить стража железом, редстоуном или алмазом и принять его в отряд.
 */
public class GuardianRuinsFeature extends Feature<NoneFeatureConfiguration> {
    public GuardianRuinsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        if (surfaceY <= level.getMinY() + 4 || surfaceY >= level.getMaxY() - 10) {
            return false;
        }
        BlockPos center = new BlockPos(origin.getX(), surfaceY, origin.getZ());
        BlockState ground = level.getBlockState(center.below());
        if (!ground.isSolid() || ground.is(Blocks.WATER) || ground.is(Blocks.LAVA) || !level.isEmptyBlock(center)) {
            return false;
        }

        // Каменная площадка 5x5 с фундаментом и разрушенными колоннами
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos floorPos = center.offset(dx, -1, dz);
                if (random.nextFloat() < 0.85F || (Math.abs(dx) <= 1 && Math.abs(dz) <= 1)) {
                    BlockState brick = randomBrick(random);
                    level.setBlock(floorPos, brick, 2);
                    if (!level.getBlockState(floorPos.below()).isSolid()) {
                        level.setBlock(floorPos.below(), Blocks.COBBLESTONE.defaultBlockState(), 2);
                    }
                }
                // Расчищаем воздух над площадкой
                for (int dy = 0; dy <= 3; dy++) {
                    BlockPos clearPos = center.offset(dx, dy, dz);
                    if (level.getBlockState(clearPos).canBeReplaced()) {
                        level.setBlock(clearPos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Постамент в центре
        level.setBlock(center, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 2);

        // Угловые разрушенные колонны
        int[][] corners = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}};
        for (int i = 0; i < corners.length; i++) {
            int cx = corners[i][0];
            int cz = corners[i][1];
            int h = 1 + random.nextInt(3);
            for (int dy = 0; dy < h; dy++) {
                level.setBlock(center.offset(cx, dy, cz), randomBrick(random), 2);
            }
            if (i == 0 && h >= 2) {
                level.setBlock(center.offset(cx, h, cz), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
            }
        }

        // Древний сундук с деталями для починки
        BlockPos chestPos = center.offset(1, 0, -1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.IRON_INGOT, 2 + random.nextInt(4)));
            chest.setItem(1, new ItemStack(Items.REDSTONE, 2 + random.nextInt(5)));
            chest.setItem(2, new ItemStack(Items.BREAD, 2 + random.nextInt(3)));
            if (random.nextFloat() < 0.45F) {
                chest.setItem(3, new ItemStack(Items.AMETHYST_SHARD, 1 + random.nextInt(2)));
            }
            if (random.nextFloat() < 0.25F) {
                chest.setItem(4, new ItemStack(Items.GOLD_INGOT, 1 + random.nextInt(2)));
            }
            if (random.nextFloat() < 0.15F) {
                chest.setItem(5, new ItemStack(Items.DIAMOND, 1));
            }
        }

        // Сломанный (отключённый) страж на постаменте
        boolean spawnValkyrie = random.nextBoolean();
        BaseGolemEntity golem = (spawnValkyrie ? ModEntities.VALKYRIE_GOLEM : ModEntities.WARRIOR_GOLEM)
                .create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (golem != null) {
            golem.setPos(center.getX() + 0.5D, center.getY() + 1.02D, center.getZ() + 0.5D);
            golem.setYRot(random.nextFloat() * 360.0F);
            golem.setDisabled(true);
            golem.setHealth(4.0F);
            golem.setPersistenceRequired();
            level.addFreshEntity(golem);
        }

        return true;
    }

    private static BlockState randomBrick(RandomSource random) {
        float f = random.nextFloat();
        if (f < 0.35F) return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        if (f < 0.65F) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        return Blocks.STONE_BRICKS.defaultBlockState();
    }
}
