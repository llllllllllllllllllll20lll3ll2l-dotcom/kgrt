package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import com.manyl.mod.item.SummonStaffItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * Регистрация предметов мода (посох призыва прислужников).
 */
public final class ModItems {
    public static final ResourceKey<Item> SUMMON_STAFF_KEY =
            ResourceKey.create(Registries.ITEM, ManylMod.id("summon_staff"));

    public static final Item SUMMON_STAFF = Registry.register(
            BuiltInRegistries.ITEM,
            SUMMON_STAFF_KEY,
            new SummonStaffItem(new Item.Properties().stacksTo(1).setId(SUMMON_STAFF_KEY))
    );

    public static void init() {
    }
}
