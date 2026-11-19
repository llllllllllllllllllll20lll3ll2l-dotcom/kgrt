package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Регистрация звуковых эффектов Manyl_mod (включая живую женскую озвучку Воительницы).
 */
public final class ModSounds {
    public static final SoundEvent GOLEM_SUMMON = register("golem_summon");
    public static final SoundEvent GOLEM_RECALL = register("golem_recall");
    public static final SoundEvent GOLEM_STEP = register("golem_step");
    public static final SoundEvent GOLEM_ATTACK = register("golem_attack");
    public static final SoundEvent GOLEM_HURT = register("golem_hurt");
    public static final SoundEvent GOLEM_DEATH = register("golem_death");
    public static final SoundEvent GOLEM_EAT = register("golem_eat");
    public static final SoundEvent GOLEM_HUNGRY = register("golem_hungry");

    public static final SoundEvent VALKYRIE_LAUGH = register("valkyrie_laugh");
    public static final SoundEvent VALKYRIE_HURT = register("valkyrie_hurt");
    public static final SoundEvent VALKYRIE_ATTACK = register("valkyrie_attack");
    public static final SoundEvent VALKYRIE_GREET = register("valkyrie_greet");
    public static final SoundEvent VALKYRIE_JEALOUS = register("valkyrie_jealous");
    public static final SoundEvent VALKYRIE_LOVE = register("valkyrie_love");
    public static final SoundEvent VALKYRIE_SIGH = register("valkyrie_sigh");

    private static SoundEvent register(String name) {
        Identifier id = ManylMod.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {}
}
