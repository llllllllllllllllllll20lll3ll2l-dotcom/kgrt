package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Регистрация всех кастомных звуковых эффектов Manyl_mod.
 */
public final class ModSounds {
    public static final SoundEvent GOLEM_SUMMON = register("golem_summon");
    public static final SoundEvent GOLEM_RECALL = register("golem_recall");
    public static final SoundEvent GOLEM_STEP = register("golem_step");
    public static final SoundEvent GOLEM_WORK_DIG = register("golem_work_dig");
    public static final SoundEvent GOLEM_WORK_FARM = register("golem_work_farm");
    public static final SoundEvent GOLEM_WORK_SORT = register("golem_work_sort");
    public static final SoundEvent GOLEM_ATTACK = register("golem_attack");
    public static final SoundEvent GOLEM_HURT = register("golem_hurt");
    public static final SoundEvent GOLEM_DEATH = register("golem_death");
    public static final SoundEvent GOLEM_EAT = register("golem_eat");
    public static final SoundEvent GOLEM_HUNGRY = register("golem_hungry");

    private static SoundEvent register(String name) {
        Identifier id = ManylMod.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {
        // Триггерит статическую инициализацию полей
    }
}
