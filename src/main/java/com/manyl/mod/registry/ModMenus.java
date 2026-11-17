package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import com.manyl.mod.menu.GolemInventoryMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;

/**
 * Регистрация расширенного меню встроенного инвентаря прислужника.
 */
public final class ModMenus {
    public static final MenuType<GolemInventoryMenu> GOLEM_INVENTORY = Registry.register(
            BuiltInRegistries.MENU,
            ManylMod.id("golem_inventory"),
            new ExtendedScreenHandlerType<>(GolemInventoryMenu::new, ByteBufCodecs.INT.cast())
    );

    public static void init() {
    }
}
