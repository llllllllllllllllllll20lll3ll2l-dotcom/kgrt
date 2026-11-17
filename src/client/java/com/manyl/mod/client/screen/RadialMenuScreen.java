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
 * Оптимизированное круговое меню управления прислужниками (Manyl_mod 1.1):
 * использует предварительно запечённые PNG-текстуры (2-3 blit вместо 135 000 fill на кадр — без лагов FPS).
 */
public class RadialMenuScreen extends Screen {
    private static final Identifier ICON_SUMMON = ManylMod.id("textures/gui/icons/summon.png");
    private static final Identifier ICON_RECALL = ManylMod.id("textures/gui/icons/recall.png");
    private static final Identifier ICON_MANAGE = ManylMod.id("textures/gui/icons/manage.png");

    private static final Identifier RADIAL_3_BASE = ManylMod.id("textures/gui/radial/radial_3_base.png");
    private static final Identifier RADIAL_4_BASE = ManylMod.id("textures/gui/radial/radial_4_base.png");
    private static final Identifier RADIAL_CENTER_HOVER = ManylMod.id("textures/gui/radial/radial_center_hover.png");

    private static final Identifier[] RADIAL_3_HOVER = {
            ManylMod.id("textures/gui/radial/radial_3_hover_0.png"),
            ManylMod.id("textures/gui/radial/radial_3_hover_1.png"),
            ManylMod.id("textures/gui/radial/radial_3_hover_2.png")
    };
    private static final Identifier[] RADIAL_4_HOVER = {
            ManylMod.id("textures/gui/radial/radial_4_hover_0.png"),
            ManylMod.id("textures/gui/radial/radial_4_hover_1.png"),
            ManylMod.id("textures/gui/radial/radial_4_hover_2.png"),
            ManylMod.id("textures/gui/radial/radial_4_hover_3.png")
    };

    private static final int WHEEL_SIZE = 224;
    private static final int OUTER_RADIUS = 104;
    private static final int INNER_RADIUS = 30;

    private final int[] counts = new int[GolemRole.values().length];
    private boolean summonSubmenu = false;
    private int hoveredSector = -1;
    private boolean hoveredCenter = false;

    public RadialMenuScreen(ModNetworking.OpenStaffMenuPayload payload) {
        super(Component.translatable("screen.manyl_mod.radial"));
        for (GolemRole role : GolemRole.values()) {
            counts[role.ordinal()] = payload.countFor(role);
        }
    }

    public void updateCounts(ModNetworking.SyncGolemStatePayload payload) {
        for (GolemRole role : GolemRole.values()) {
            counts[role.ordinal()] = payload.countFor(role);
        }
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

        int sectorCount = summonSubmenu ? 4 : 3;

        // 1. Быстрая отрисовка базового кольца (1 blit)
        graphics.blit(RenderPipelines.GUI_TEXTURED,
                summonSubmenu ? RADIAL_4_BASE : RADIAL_3_BASE,
                wx, wy, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);

        // 2. Подсветка наведённого сектора или центра (1 blit)
        if (hoveredCenter) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, RADIAL_CENTER_HOVER,
                    wx, wy, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);
        } else if (hoveredSector >= 0 && hoveredSector < sectorCount) {
            Identifier hoverTex = summonSubmenu ? RADIAL_4_HOVER[hoveredSector] : RADIAL_3_HOVER[hoveredSector];
            graphics.blit(RenderPipelines.GUI_TEXTURED, hoverTex,
                    wx, wy, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);
        }

        // 3. Иконки и подписи секторов
        double startOffset = summonSubmenu ? -Math.PI * 0.75D : -Math.PI * 0.5D - Math.PI / 3.0D;
        double step = (Math.PI * 2.0D) / sectorCount;
        int iconR = (INNER_RADIUS + OUTER_RADIUS) / 2;

        for (int i = 0; i < sectorCount; i++) {
            double mid = startOffset + (i + 0.5D) * step;
            boolean hovered = (hoveredSector == i);

            int ix = cx + (int) Math.round(Math.cos(mid) * iconR);
            int iy = cy + (int) Math.round(Math.sin(mid) * iconR);

            if (!summonSubmenu) {
                Identifier icon = switch (i) {
                    case 0 -> ICON_SUMMON;
                    case 1 -> ICON_RECALL;
                    default -> ICON_MANAGE;
                };
                Component label = switch (i) {
                    case 0 -> Component.translatable("gui.manyl_mod.summon");
                    case 1 -> Component.translatable("gui.manyl_mod.recall");
                    default -> Component.translatable("gui.manyl_mod.manage");
                };
                graphics.blit(RenderPipelines.GUI_TEXTURED, icon, ix - 8, iy - 12, 0, 0, 16, 16, 16, 16);
                graphics.drawCenteredString(this.font, label, ix, iy + 8, hovered ? 0xFFFFE599 : 0xFFE0E5F0);
            } else {
                GolemRole role = GolemRole.byOrdinal(i);
                graphics.blit(RenderPipelines.GUI_TEXTURED, role.getIconTexture(), ix - 8, iy - 14, 0, 0, 16, 16, 16, 16);
                graphics.drawCenteredString(this.font, Component.translatable(role.getTranslationKey()), ix, iy + 5, hovered ? 0xFFFFFFFF : 0xFFD8DEE9);
                String countStr = counts[i] + "/" + role.getMaxPerPlayer();
                graphics.drawCenteredString(this.font, countStr, ix, iy + 15, role.getAccentColor());
            }
        }

        String centerText = summonSubmenu ? "←" : "✕";
        graphics.drawCenteredString(this.font, centerText, cx, cy - 4, 0xFFFFE8A3);

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
        int sectorCount = summonSubmenu ? 4 : 3;
        double startOffset = summonSubmenu ? -Math.PI * 0.75D : -Math.PI * 0.5D - Math.PI / 3.0D;
        double angle = Math.atan2(dy, dx) - startOffset;
        while (angle < 0) angle += Math.PI * 2.0D;
        while (angle >= Math.PI * 2.0D) angle -= Math.PI * 2.0D;
        hoveredSector = Mth.clamp((int) (angle / ((Math.PI * 2.0D) / sectorCount)), 0, sectorCount - 1);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int cx = this.width / 2;
            int cy = this.height / 2;
            updateHover(cx, cy, (int) event.x(), (int) event.y());

            if (hoveredCenter) {
                if (summonSubmenu) {
                    summonSubmenu = false;
                } else {
                    this.onClose();
                }
                return true;
            }

            if (hoveredSector >= 0) {
                if (!summonSubmenu) {
                    switch (hoveredSector) {
                        case 0 -> summonSubmenu = true;
                        case 1 -> {
                            ClientPlayNetworking.send(new ModNetworking.RecallGolemsPayload(-1));
                            this.onClose();
                        }
                        case 2 -> ClientPlayNetworking.send(new ModNetworking.RequestOpenManagementPayload());
                    }
                } else {
                    ClientPlayNetworking.send(new ModNetworking.SummonGolemPayload(hoveredSector));
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
