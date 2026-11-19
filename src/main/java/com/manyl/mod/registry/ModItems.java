package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.item.GuardianEggItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Регистрация предметов Manyl_mod (крафтовые яйца призыва стражей).
 */
public final class ModItems {
    public static final Item WARRIOR_SPAWN_EGG = registerEgg("warrior_spawn_egg", GolemRole.WARRIOR);
    public static final Item VALKYRIE_SPAWN_EGG = registerEgg("valkyrie_spawn_egg", GolemRole.VALKYRIE);

    private static Item registerEgg(String name, GolemRole role) {
        Identifier id = ManylMod.id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item.Properties props = new Item.Properties()
                .setId(key)
                .stacksTo(16)
                .rarity(Rarity.RARE);
        return Registry.register(BuiltInRegistries.ITEM, key, new GuardianEggItem(role, props));
    }

    private ModItems() {}

    public static void init() {}
}
