package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Регистрация кастомных частиц: руны призыва, рабочие искры, сердце ядра при кормлении.
 */
public final class ModParticles {
    public static final SimpleParticleType SUMMON_RUNE = register("summon_rune");
    public static final SimpleParticleType GOLEM_SPARK = register("golem_spark");
    public static final SimpleParticleType CORE_HEART = register("core_heart");

    private static SimpleParticleType register(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ManylMod.id(name), FabricParticleTypes.simple());
    }

    public static void init() {
    }
}
