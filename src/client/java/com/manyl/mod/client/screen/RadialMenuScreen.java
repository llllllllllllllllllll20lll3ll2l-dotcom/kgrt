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
 * Круговое меню управления прислужниками (открывается при ПКМ с посохом в руке):
 * - Главное кольцо: Призвать / Отозвать / Управление;
 * - Подменю «Призвать»: выбор конкретного голема (Копатель, Фермер, Сортировщик, Воин) с отображением лимита.
 */
public class RadialMenuScreen extends Screen {
    private static final Identifier ICON_SUMMON = ManylMod.id("textures/gui/icons/summon.png");
    private static final Identifier ICON_RECALL = ManylMod.id("textures/gui/icons/recall.png");
    private static final Identifier ICON_MANAGE = ManylMod.id("textures/gui/icons/manage.png");

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

        updateHover(cx, cy, mouseX, mouseY);

        // Полупрозрачный фон кольца
        drawCircle(graphics, cx, cy, OUTER_RADIUS + 4, 0xCC14171F);
        drawCircle(graphics, cx, cy, OUTER_RADIUS, 0xDD222733);

        int sectorCount = summonSubmenu ? 4 : 3;
        double startOffset = summonSubmenu ? -Math.PI * 0.75D : -Math.PI * 0.5D - Math.PI / 3.0D;
        double step = (Math.PI * 2.0D) / sectorCount;

        for (int i = 0; i < sectorCount; i++) {
            double a0 = startOffset + i * step;
            double a1 = a0 + step;
            double mid = (a0 + a1) * 0.5D;
            boolean hovered = (hoveredSector == i);

            int fillColor = hovered ? getSectorHighlightColor(i) : 0xAA2D3444;
            drawRingSector(graphics, cx, cy, INNER_RADIUS + 3, OUTER_RADIUS - 3, a0 + 0.03D, a1 - 0.03D, fillColor);

            int iconR = (INNER_RADIUS + OUTER_RADIUS) / 2;
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

        // Центральная кнопка (закрыть или назад)
        drawCircle(graphics, cx, cy, INNER_RADIUS, hoveredCenter ? 0xFFBA8E3E : 0xFF363C4A);
        drawCircle(graphics, cx, cy, INNER_RADIUS - 2, hoveredCenter ? 0xFF474E5E : 0xFF1D212B);
        String centerText = summonSubmenu ? "←" : "✕";
        graphics.drawCenteredString(this.font, centerText, cx, cy - 4, 0xFFFFE8A3);

        super.render(graphics, mouseX, mouseY, delta);
    }

    private int getSectorHighlightColor(int sector) {
        if (!summonSubmenu) {
            return switch (sector) {
                case 0 -> 0xCC2C6E7E;
                case 1 -> 0xCC6D3A82;
                default -> 0xCC8A682C;
            };
        }
        return (GolemRole.byOrdinal(sector).getAccentColor() & 0x00FFFFFF) | 0x99000000;
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

    private static void drawCircle(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private static void drawRingSector(GuiGraphics g, int cx, int cy, int rIn, int rOut, double a0, double a1, int color) {
        for (int dy = -rOut; dy <= rOut; dy++) {
            for (int dx = -rOut; dx <= rOut; dx++) {
                double d2 = dx * dx + dy * dy;
                if (d2 < rIn * rIn || d2 > rOut * rOut) continue;
                double ang = Math.atan2(dy, dx);
                if (angleInRange(ang, a0, a1)) {
                    g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                }
            }
        }
    }

    private static boolean angleInRange(double ang, double a0, double a1) {
        double twoPi = Math.PI * 2.0D;
        double normAng = ((ang - a0) % twoPi + twoPi) % twoPi;
        double span = ((a1 - a0) % twoPi + twoPi) % twoPi;
        return normAng <= span;
    }
}
