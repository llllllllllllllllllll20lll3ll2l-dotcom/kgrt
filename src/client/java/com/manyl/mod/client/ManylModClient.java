package com.manyl.mod.client;

import com.manyl.mod.ManylMod;
import com.manyl.mod.client.model.GolemModel;
import com.manyl.mod.client.model.ValkyrieModel;
import com.manyl.mod.client.particle.ManylParticles;
import com.manyl.mod.client.renderer.GolemRenderer;
import com.manyl.mod.client.renderer.ValkyrieRenderer;
import com.manyl.mod.client.screen.GolemInventoryScreen;
import com.manyl.mod.client.screen.GolemManagementScreen;
import com.manyl.mod.client.screen.RadialMenuScreen;
import com.manyl.mod.client.screen.ValkyrieDialogueScreen;
import com.manyl.mod.network.ModNetworking;
import com.manyl.mod.registry.ModEntities;
import com.manyl.mod.registry.ModMenus;
import com.manyl.mod.registry.ModParticles;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * Клиентская инициализация Manyl_mod (Воин, Воительница и тактические клавиши отряда).
 */
public class ManylModClient implements ClientModInitializer {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(ManylMod.id("main"));
    private static KeyMapping keySquadOrder;
    private static KeyMapping keySquadMenu;
    private static boolean holdToggleState = false;

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

        keySquadOrder = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.manyl_mod.squad_order",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KEY_CATEGORY
        ));
        keySquadMenu = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.manyl_mod.squad_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::handleHotkeys);

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
                    holdToggleState = "stay".equals(payload.guardMode());
                    if (ctx.client().screen instanceof RadialMenuScreen radial) {
                        radial.updateState(payload);
                    } else if (ctx.client().screen instanceof GolemManagementScreen mgmt) {
                        mgmt.updateState(payload);
                    }
                }));

        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.OpenValkyrieDialoguePayload.TYPE, (payload, ctx) ->
                ctx.client().execute(() -> {
                    if (ctx.client().screen instanceof ValkyrieDialogueScreen screen) {
                        screen.updateFromPayload(payload);
                    } else {
                        ctx.client().setScreen(new ValkyrieDialogueScreen(payload));
                    }
                }));
    }

    private void handleHotkeys(Minecraft client) {
        if (client.player == null || client.level == null) return;

        while (keySquadOrder.consumeClick()) {
            Entity aimed = findAimedLivingEntity(client, 24.0D);
            if (aimed instanceof LivingEntity) {
                ClientPlayNetworking.send(new ModNetworking.SquadOrderPayload(
                        ModNetworking.SquadOrderPayload.ORDER_ATTACK, aimed.getId()));
            } else {
                holdToggleState = !holdToggleState;
                ClientPlayNetworking.send(new ModNetworking.SquadOrderPayload(
                        holdToggleState ? ModNetworking.SquadOrderPayload.ORDER_HOLD : ModNetworking.SquadOrderPayload.ORDER_FOLLOW,
                        -1));
            }
        }

        while (keySquadMenu.consumeClick()) {
            ClientPlayNetworking.send(new ModNetworking.RequestOpenManagementPayload());
        }
    }

    private static Entity findAimedLivingEntity(Minecraft client, double range) {
        Entity camera = client.getCameraEntity();
        if (camera == null) return null;
        Vec3 eye = camera.getEyePosition(1.0F);
        Vec3 look = camera.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));
        AABB box = camera.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                camera, eye, end, box,
                e -> e instanceof LivingEntity && !e.isSpectator() && e.isPickable(),
                range * range
        );
        return hit != null ? hit.getEntity() : null;
    }
}
