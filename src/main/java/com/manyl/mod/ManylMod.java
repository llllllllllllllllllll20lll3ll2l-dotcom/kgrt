package com.manyl.mod;

import com.manyl.mod.network.ChatCommandHandler;
import com.manyl.mod.network.ModNetworking;
import com.manyl.mod.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главный класс инициализации мода Manyl_mod.
 */
public class ManylMod implements ModInitializer {
    public static final String MOD_ID = "manyl_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ResourceKey<CreativeModeTab> MAIN_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("main"));

    @Override
    public void onInitialize() {
        LOGGER.info("Инициализация мода Manyl_mod V3...");

        ModSounds.init();
        ModParticles.init();
        ModItems.init();
        ModEntities.init();
        ModMenus.init();
        ModWorldgen.init();

        ModNetworking.registerPayloads();
        ModNetworking.registerServerReceivers();
        ChatCommandHandler.init();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MAIN_TAB, FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.manyl_mod.main"))
                .icon(() -> new ItemStack(ModItems.WARRIOR_SPAWN_EGG))
                .displayItems((ctx, entries) -> {
                    entries.accept(ModItems.WARRIOR_SPAWN_EGG);
                    entries.accept(ModItems.VALKYRIE_SPAWN_EGG);
                    entries.accept(ModItems.VOW_RING);
                })
                .build());

        LOGGER.info("Manyl_mod V3 успешно загружен!");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
