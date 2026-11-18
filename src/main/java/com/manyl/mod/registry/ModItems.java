package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import com.manyl.mod.item.SummonStaffItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Регистрация предметов Manyl_mod.
 */
public final class ModItems {
    public static final Item SUMMON_STAFF = registerSummonStaff();

    private static Item registerSummonStaff() {
        Identifier id = ManylMod.id("summon_staff");
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item.Properties props = new Item.Properties()
                .setId(key)
                .stacksTo(1)
                .rarity(Rarity.RARE);
        return Registry.register(BuiltInRegistries.ITEM, key, new SummonStaffItem(props));
    }

    private ModItems() {}

    public static void init() {}
}
