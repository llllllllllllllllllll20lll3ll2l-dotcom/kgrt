package com.manyl.mod.client;

import com.manyl.mod.client.model.GolemModel;
import com.manyl.mod.client.particle.ManylParticles;
import com.manyl.mod.client.renderer.GolemRenderer;
import com.manyl.mod.client.screen.GolemInventoryScreen;
import com.manyl.mod.client.screen.GolemManagementScreen;
import com.manyl.mod.client.screen.RadialMenuScreen;
import com.manyl.mod.network.ModNetworking;
import com.manyl.mod.registry.ModEntities;
import com.manyl.mod.registry.ModMenus;
import com.manyl.mod.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * Клиентская инициализация Manyl_mod:
 * регистрация модели големов, рендереров, частиц, экранов инвентаря, кругового меню и панели управления.
 */
public class ManylModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Модель и рендерер всех 4 ролей големов
        EntityModelLayerRegistry.registerModelLayer(GolemModel.LAYER_LOCATION, GolemModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntities.DIGGER_GOLEM, GolemRenderer::new);
        EntityRendererRegistry.register(ModEntities.FARMER_GOLEM, GolemRenderer::new);
        EntityRendererRegistry.register(ModEntities.SORTER_GOLEM, GolemRenderer::new);
        EntityRendererRegistry.register(ModEntities.WARRIOR_GOLEM, GolemRenderer::new);

        // Фабрики кастомных частиц
        ParticleFactoryRegistry.getInstance().register(ModParticles.SUMMON_RUNE, ManylParticles.RuneFactory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.GOLEM_SPARK, ManylParticles.SparkFactory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.CORE_HEART, ManylParticles.SparkFactory::new);

        // Экран встроенного инвентаря прислужника
        MenuScreens.register(ModMenus.GOLEM_INVENTORY, GolemInventoryScreen::new);

        // Приём пакетов для кругового меню и окна управления
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.OpenStaffMenuPayload.TYPE, (payload, ctx) ->
                ctx.client().execute(() -> {
                    if (payload.openManagement()) {
                        ctx.client().setScreen(new GolemManagementScreen(payload));
                    } else {
                        ctx.client().setScreen(new RadialMenuScreen(payload));
                    }
                }));

        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.SyncGolemStatePayload.TYPE, (payload, ctx) ->
                ctx.client().execute(() -> {
                    if (ctx.client().screen instanceof RadialMenuScreen radial) {
                        radial.updateCounts(payload);
                    } else if (ctx.client().screen instanceof GolemManagementScreen mgmt) {
                        mgmt.updateState(payload);
                    }
                }));
    }
}
