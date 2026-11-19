package com.manyl.mod.client.screen;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.network.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Окно управления отрядом Воинов и Воительниц с кнопками роспуска отдельных типов и всего отряда.
 */
public class GolemManagementScreen extends Screen {
    private static final Identifier PANEL_TEX = ManylMod.id("textures/gui/management_panel.png");
    private static final int PANEL_W = 248;
    private static final int PANEL_H = 186;

    private int warriorCount;
    private int valkyrieCount;
    private boolean followOwner;
    private String guardMode;
    private String targetFilter;

    private Button guardButton;

    public GolemManagementScreen(ModNetworking.OpenStaffMenuPayload payload) {
        super(Component.translatable("screen.manyl_mod.management"));
        this.warriorCount = payload.warriorCount();
        this.valkyrieCount = payload.valkyrieCount();
        this.followOwner = payload.followOwner();
        this.guardMode = payload.guardMode();
        this.targetFilter = payload.targetFilter();
    }

    public void updateState(ModNetworking.SyncGolemStatePayload payload) {
        this.warriorCount = payload.warriorCount();
        this.valkyrieCount = payload.valkyrieCount();
        this.followOwner = payload.followOwner();
        this.guardMode = payload.guardMode();
        this.targetFilter = payload.targetFilter();
        if (guardButton != null) {
            guardButton.setMessage(guardLabel());
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        int left = (this.width - PANEL_W) / 2;
        int top = (this.height - PANEL_H) / 2;

        GolemRole[] roles = GolemRole.values();
        for (int i = 0; i < roles.length; i++) {
            GolemRole role = roles[i];
            int ry = top + 34 + i * 26;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.manyl_mod.dismiss_role"), b ->
                    ClientPlayNetworking.send(new ModNetworking.RecallGolemsPayload(role.ordinal())))
                    .pos(left + 168, ry)
                    .size(68, 18)
                    .build());
        }

        this.guardButton = this.addRenderableWidget(Button.builder(guardLabel(), b -> {
            guardMode = switch (guardMode) {
                case "follow" -> "stay";
                case "stay" -> "patrol";
                default -> "follow";
            };
            followOwner = "follow".equals(guardMode);
            b.setMessage(guardLabel());
            ClientPlayNetworking.send(new ModNetworking.UpdateWarriorSettingsPayload(followOwner, guardMode, targetFilter));
        }).pos(left + 12, top + 106).size(PANEL_W - 24, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.manyl_mod.disband_squad"), b -> {
            ClientPlayNetworking.send(new ModNetworking.RecallGolemsPayload(-1));
            this.onClose();
        }).pos(left + 12, top + 134).size(PANEL_W - 24, 22).build());

        this.addRenderableWidget(Button.builder(Component.literal("✕"), b -> this.onClose())
                .pos(left + PANEL_W - 24, top + 6)
                .size(16, 16)
                .build());
    }

    private Component guardLabel() {
        return switch (guardMode) {
            case "stay" -> Component.translatable("gui.manyl_mod.guard.stay");
            case "patrol" -> Component.translatable("gui.manyl_mod.guard.patrol");
            default -> Component.translatable("gui.manyl_mod.guard.follow");
        };
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int left = (this.width - PANEL_W) / 2;
        int top = (this.height - PANEL_H) / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, PANEL_TEX, left, top, 0, 0, PANEL_W, PANEL_H, 256, 256);
        graphics.fill(left, top + PANEL_H - 1, left + PANEL_W, top + PANEL_H, 0xFF12141A);

        graphics.drawString(this.font, this.title, left + 10, top + 10, 0xFFE8C87A, false);

        GolemRole[] roles = GolemRole.values();
        for (int i = 0; i < roles.length; i++) {
            GolemRole role = roles[i];
            int count = (role == GolemRole.WARRIOR) ? warriorCount : valkyrieCount;
            int ry = top + 34 + i * 26;
            graphics.blit(RenderPipelines.GUI_TEXTURED, role.getIconTexture(), left + 12, ry + 1, 0, 0, 16, 16, 16, 16);
            graphics.drawString(this.font, Component.translatable(role.getTranslationKey()), left + 34, ry + 5, 0xFFE5E9F0, false);

            String countLabel = String.valueOf(count);
            graphics.drawString(this.font, countLabel, left + 148, ry + 5, role.getAccentColor(), false);
        }

        graphics.drawString(this.font, Component.translatable("gui.manyl_mod.guard_mode_label"), left + 12, top + 92, 0xFFD8DEE9, false);
        graphics.drawCenteredString(this.font, Component.translatable("gui.manyl_mod.disband_hint"),
                left + PANEL_W / 2, top + 162, 0xFF9AA7C0);

        super.render(graphics, mouseX, mouseY, delta);
    }
}
