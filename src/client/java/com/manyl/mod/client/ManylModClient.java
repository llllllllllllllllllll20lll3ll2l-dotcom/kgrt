package com.manyl.mod.client;

import com.manyl.mod.client.model.GolemModel;
import com.manyl.mod.client.model.ValkyrieModel;
import com.manyl.mod.client.particle.ManylParticles;
import com.manyl.mod.client.renderer.GolemRenderer;
import com.manyl.mod.client.renderer.ValkyrieRenderer;
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
 * Клиентская инициализация Manyl_mod (Воин и Воительница).
 */
public class ManylModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(GolemModel.LAYER_LOCATION, GolemModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ValkyrieModel.LAYER_LOCATION, ValkyrieModel::createBodyLayer);

        EntityRendererRegistry.register(ModEntities.WARRIOR_GOLEM, GolemRenderer::new);
        EntityRendererRegistry.register(ModEntities.VALKYRIE_GOLEM, ValkyrieRenderer::new);

        ParticleFactoryRegistry.getInstance().register(ModParticles.SUMMON_RUNE, ManylParticles.RuneFactory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.GOLEM_SPARK, ManylParticles.SparkFactory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.CORE_HEART, ManylParticles.SparkFactory::new);

        MenuScreens.register(ModMenus.GOLEM_INVENTORY, GolemInventoryScreen::new);

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
                        radial.updateState(payload);
                    } else if (ctx.client().screen instanceof GolemManagementScreen mgmt) {
                        mgmt.updateState(payload);
                    }
                }));
    }
}
