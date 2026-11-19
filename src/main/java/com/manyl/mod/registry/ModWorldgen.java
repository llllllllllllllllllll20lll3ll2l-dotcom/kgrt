package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import com.manyl.mod.world.GuardianRuinsFeature;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Регистрация генерации древних руин с отключёнными стражами.
 */
public final class ModWorldgen {
    public static final Feature<NoneFeatureConfiguration> GUARDIAN_RUINS_FEATURE = Registry.register(
            BuiltInRegistries.FEATURE,
            ManylMod.id("guardian_ruins"),
            new GuardianRuinsFeature(NoneFeatureConfiguration.CODEC)
    );

    public static final ResourceKey<PlacedFeature> GUARDIAN_RUINS_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ManylMod.id("guardian_ruins")
    );

    public static void init() {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                GUARDIAN_RUINS_PLACED
        );
    }
}
