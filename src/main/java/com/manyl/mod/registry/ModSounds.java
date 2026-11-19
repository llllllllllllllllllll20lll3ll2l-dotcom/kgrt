package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Регистрация звуковых эффектов Manyl_mod (включая озвучку Воинов и Воительниц).
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

    public static final SoundEvent WARRIOR_GREET = register("warrior_greet");
    public static final SoundEvent WARRIOR_ATTACK = register("warrior_attack");
    public static final SoundEvent WARRIOR_ORDER = register("warrior_order");
    public static final SoundEvent WARRIOR_HURT = register("warrior_hurt");

    public static final SoundEvent VALKYRIE_LAUGH = register("valkyrie_laugh");
    public static final SoundEvent VALKYRIE_HURT = register("valkyrie_hurt");
    public static final SoundEvent VALKYRIE_ATTACK = register("valkyrie_attack");
    public static final SoundEvent VALKYRIE_GREET = register("valkyrie_greet");
    public static final SoundEvent VALKYRIE_JEALOUS = register("valkyrie_jealous");
    public static final SoundEvent VALKYRIE_LOVE = register("valkyrie_love");
    public static final SoundEvent VALKYRIE_SIGH = register("valkyrie_sigh");

    public static final SoundEvent VALKYRIE_WILD_GREET = register("valkyrie_wild_greet");
    public static final SoundEvent VALKYRIE_RECRUITED = register("valkyrie_recruited");
    public static final SoundEvent VALKYRIE_TALK_ALLY = register("valkyrie_talk_ally");
    public static final SoundEvent VALKYRIE_TALK_FRIEND = register("valkyrie_talk_friend");
    public static final SoundEvent VALKYRIE_TALK_CRUSH = register("valkyrie_talk_crush");
    public static final SoundEvent VALKYRIE_TALK_BELOVED = register("valkyrie_talk_beloved");
    public static final SoundEvent VALKYRIE_GIFT_LOVE = register("valkyrie_gift_love");
    public static final SoundEvent VALKYRIE_GIFT_SWEET = register("valkyrie_gift_sweet");
    public static final SoundEvent VALKYRIE_CONFESS = register("valkyrie_confess");
    public static final SoundEvent VALKYRIE_RESCUE = register("valkyrie_rescue");
    public static final SoundEvent VALKYRIE_DAYDREAM = register("valkyrie_daydream");
    public static final SoundEvent VALKYRIE_VOW = register("valkyrie_vow");
    public static final SoundEvent VALKYRIE_CAMPFIRE = register("valkyrie_campfire");
    public static final SoundEvent VALKYRIE_REPAIRED = register("valkyrie_repaired");
    public static final SoundEvent VALKYRIE_BARK = register("valkyrie_bark");
    public static final SoundEvent VALKYRIE_MOAN = register("valkyrie_moan");

    private static SoundEvent register(String name) {
        Identifier id = ManylMod.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {}
}
