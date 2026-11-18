package com.manyl.mod.client.screen;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.network.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Круговое меню управления Воинами (Призвать / Отозвать / Управление).
 */
public class RadialMenuScreen extends Screen {
    private static final Identifier ICON_RECALL = ManylMod.id("textures/gui/icons/recall.png");
    private static final Identifier ICON_MANAGE = ManylMod.id("textures/gui/icons/manage.png");

    private static final Identifier RADIAL_3_BASE = ManylMod.id("textures/gui/radial/radial_3_base.png");
    private static final Identifier RADIAL_CENTER_HOVER = ManylMod.id("textures/gui/radial/radial_center_hover.png");
    private static final Identifier[] RADIAL_3_HOVER = {
            ManylMod.id("textures/gui/radial/radial_3_hover_0.png"),
            ManylMod.id("textures/gui/radial/radial_3_hover_1.png"),
            ManylMod.id("textures/gui/radial/radial_3_hover_2.png")
    };

    private static final int WHEEL_SIZE = 224;
    private static final int OUTER_RADIUS = 104;
    private static final int INNER_RADIUS = 30;

    private int warriorCount;
    private int hoveredSector = -1;
    private boolean hoveredCenter = false;

    public RadialMenuScreen(ModNetworking.OpenStaffMenuPayload payload) {
        super(Component.translatable("screen.manyl_mod.radial"));
        this.warriorCount = payload.warriorCount();
    }

    public void updateState(ModNetworking.SyncGolemStatePayload payload) {
        this.warriorCount = payload.warriorCount();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int wx = cx - WHEEL_SIZE / 2;
        int wy = cy - WHEEL_SIZE / 2;

        updateHover(cx, cy, mouseX, mouseY);

        graphics.blit(RenderPipelines.GUI_TEXTURED, RADIAL_3_BASE, wx, wy, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);

        if (hoveredCenter) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, RADIAL_CENTER_HOVER, wx, wy, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);
        } else if (hoveredSector >= 0 && hoveredSector < 3) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, RADIAL_3_HOVER[hoveredSector], wx, wy, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);
        }

        double startOffset = -Math.PI * 0.5D - Math.PI / 3.0D;
        double step = (Math.PI * 2.0D) / 3.0D;
        int iconR = (INNER_RADIUS + OUTER_RADIUS) / 2;

        for (int i = 0; i < 3; i++) {
            double mid = startOffset + (i + 0.5D) * step;
            boolean hovered = (hoveredSector == i);

            int ix = cx + (int) Math.round(Math.cos(mid) * iconR);
            int iy = cy + (int) Math.round(Math.sin(mid) * iconR);

            if (i == 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, GolemRole.WARRIOR.getIconTexture(), ix - 8, iy - 14, 0, 0, 16, 16, 16, 16);
                graphics.drawCenteredString(this.font, Component.translatable("gui.manyl_mod.summon"), ix, iy + 5, hovered ? 0xFFFFE599 : 0xFFE0E5F0);
                String countStr = warriorCount + "/" + GolemRole.WARRIOR.getMaxPerPlayer();
                graphics.drawCenteredString(this.font, countStr, ix, iy + 15, GolemRole.WARRIOR.getAccentColor());
            } else {
                Identifier icon = (i == 1) ? ICON_RECALL : ICON_MANAGE;
                Component label = Component.translatable(i == 1 ? "gui.manyl_mod.recall" : "gui.manyl_mod.manage");
                graphics.blit(RenderPipelines.GUI_TEXTURED, icon, ix - 8, iy - 12, 0, 0, 16, 16, 16, 16);
                graphics.drawCenteredString(this.font, label, ix, iy + 8, hovered ? 0xFFFFE599 : 0xFFE0E5F0);
            }
        }

        graphics.drawCenteredString(this.font, "✕", cx, cy - 4, 0xFFFFE8A3);
        super.render(graphics, mouseX, mouseY, delta);
    }

    private void updateHover(int cx, int cy, int mouseX, int mouseY) {
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        hoveredCenter = dist <= INNER_RADIUS;
        if (hoveredCenter || dist > OUTER_RADIUS + 6) {
            hoveredSector = -1;
            return;
        }
        double startOffset = -Math.PI * 0.5D - Math.PI / 3.0D;
        double angle = Math.atan2(dy, dx) - startOffset;
        while (angle < 0) angle += Math.PI * 2.0D;
        while (angle >= Math.PI * 2.0D) angle -= Math.PI * 2.0D;
        hoveredSector = Mth.clamp((int) (angle / ((Math.PI * 2.0D) / 3.0D)), 0, 2);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int cx = this.width / 2;
            int cy = this.height / 2;
            updateHover(cx, cy, (int) event.x(), (int) event.y());

            if (hoveredCenter) {
                this.onClose();
                return true;
            }
            if (hoveredSector >= 0) {
                switch (hoveredSector) {
                    case 0 -> ClientPlayNetworking.send(new ModNetworking.SummonGolemPayload(0));
                    case 1 -> {
                        ClientPlayNetworking.send(new ModNetworking.RecallGolemsPayload(-1));
                        this.onClose();
                    }
                    case 2 -> ClientPlayNetworking.send(new ModNetworking.RequestOpenManagementPayload());
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
