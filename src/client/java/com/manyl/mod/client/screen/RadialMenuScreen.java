package com.manyl.mod.client.screen;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.network.ModNetworking;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Оптимизированное радиальное колесо (версия 1.1):
 * использует запечённые PNG-текстуры вместо попиксельной отрисовки (~2-3 blit на кадр, 0 лагов FPS).
 */
public class RadialMenuScreen extends Screen {
    public enum Page {
        ACTIONS,
        SUMMON_CLASS
    }

    private static final int WHEEL_SIZE = 224;
    private static final int OUTER_R = 104;
    private static final int INNER_R = 32;

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

    private static final Identifier[] ROLE_ICONS = {
            ManylMod.id("textures/gui/icon_digger.png"),
            ManylMod.id("textures/gui/icon_farmer.png"),
            ManylMod.id("textures/gui/icon_sorter.png"),
            ManylMod.id("textures/gui/icon_warrior.png")
    };

    private Page page;

    public RadialMenuScreen(Page initialPage) {
        super(Component.translatable("screen.manyl_mod.radial"));
        this.page = initialPage;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int wx = cx - WHEEL_SIZE / 2;
        int wy = cy - WHEEL_SIZE / 2;

        g.fill(0, 0, this.width, this.height, 0x44000000);

        int sectorCount = (page == Page.ACTIONS) ? 3 : 4;
        int hovered = getHoveredSector(cx, cy, mouseX, mouseY, sectorCount);
        boolean hoverCenter = isCenterHovered(cx, cy, mouseX, mouseY);

        // 1. Базовая текстура кольца (1 blit)
        g.blit(RenderPipelines.GUI_TEXTURED,
                sectorCount == 3 ? RADIAL_3_BASE : RADIAL_4_BASE,
                wx, wy, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);

        // 2. Подсветка наведённого сектора или центра (макс. 1 blit)
        if (hoverCenter) {
            g.blit(RenderPipelines.GUI_TEXTURED, RADIAL_CENTER_HOVER,
                    wx, wy, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);
        } else if (hovered >= 0 && hovered < sectorCount) {
            Identifier hoverTex = (sectorCount == 3) ? RADIAL_3_HOVER[hovered] : RADIAL_4_HOVER[hovered];
            g.blit(RenderPipelines.GUI_TEXTURED, hoverTex,
                    wx, wy, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);
        }

        // 3. Иконки и подписи секторов
        double start = (sectorCount == 4) ? -Math.PI * 0.75D : -Math.PI * 0.5D - Math.PI / 3.0D;
        double step = (Math.PI * 2.0D) / sectorCount;

        for (int i = 0; i < sectorCount; i++) {
            double mid = start + (i + 0.5D) * step;
            int ix = cx + (int) (Math.cos(mid) * 68);
            int iy = cy + (int) (Math.sin(mid) * 68);

            if (page == Page.ACTIONS) {
                ItemStack icon = switch (i) {
                    case 0 -> new ItemStack(Items.NETHER_STAR);
                    case 1 -> new ItemStack(Items.ENDER_PEARL);
                    default -> new ItemStack(Items.COMPARATOR);
                };
                g.renderItem(icon, ix - 8, iy - 13);
                Component label = switch (i) {
                    case 0 -> Component.translatable("gui.manyl_mod.radial.summon");
                    case 1 -> Component.translatable("gui.manyl_mod.radial.recall");
                    default -> Component.translatable("gui.manyl_mod.radial.manage");
                };
                g.drawCenteredString(this.font, label, ix, iy + 8, hovered == i ? 0xFFFFFFFF : 0xFFE6E6E6);
            } else {
                GolemRole role = GolemRole.byOrdinal(i);
                g.blit(RenderPipelines.GUI_TEXTURED, ROLE_ICONS[i], ix - 8, iy - 15, 0, 0, 16, 16, 16, 16);
                Component label = Component.translatable(role.getTranslationKey());
                int count = GolemClientState.counts[i];
                int limit = role.getLimit();
                g.drawCenteredString(this.font, label, ix, iy + 4, hovered == i ? 0xFFFFFFFF : 0xFFE6E6E6);
                g.drawCenteredString(this.font, count + "/" + limit, ix, iy + 15,
                        count >= limit ? 0xFFFF6666 : 0xFFB8FFB8);
            }
        }

        // Центр круга
        Component centerLabel = (page == Page.ACTIONS)
                ? Component.translatable("gui.manyl_mod.radial.close")
                : Component.translatable("gui.manyl_mod.radial.back");
        g.drawCenteredString(this.font, centerLabel, cx, cy - 4, hoverCenter ? 0xFFFFD873 : 0xFFD0D5E0);
        g.drawCenteredString(this.font, Component.literal("Manyl_mod 1.1"), cx, cy - OUTER_R - 18, 0xFFDFB55C);

        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) return super.mouseClicked(event, doubleClick);

        double mouseX = event.x();
        double mouseY = event.y();
        int cx = this.width / 2;
        int cy = this.height / 2;

        if (isCenterHovered(cx, cy, mouseX, mouseY)) {
            if (page == Page.SUMMON_CLASS) page = Page.ACTIONS;
            else onClose();
            return true;
        }

        int sectorCount = (page == Page.ACTIONS) ? 3 : 4;
        int sector = getHoveredSector(cx, cy, mouseX, mouseY, sectorCount);
        if (sector < 0) return super.mouseClicked(event, doubleClick);

        if (page == Page.ACTIONS) {
            if (sector == 0) page = Page.SUMMON_CLASS;
            else if (sector == 1) {
                ModNetworking.sendRecallAll();
                onClose();
            } else {
                this.minecraft.setScreen(new GolemManagementScreen());
            }
        } else {
            ModNetworking.sendSelectAndSummon(sector);
            onClose();
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE || event.key() == InputConstants.KEY_R) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private boolean isCenterHovered(int cx, int cy, double mx, double my) {
        double dx = mx - cx;
        double dy = my - cy;
        return Math.sqrt(dx * dx + dy * dy) <= INNER_R;
    }

    private int getHoveredSector(int cx, int cy, double mx, double my, int count) {
        double dx = mx - cx;
        double dy = my - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist <= INNER_R || dist > OUTER_R) return -1;

        double angle = Math.atan2(dy, dx);
        double start = (count == 4) ? -Math.PI * 0.75D : -Math.PI * 0.5D - Math.PI / 3.0D;
        double rel = (angle - start) % (Math.PI * 2.0D);
        if (rel < 0) rel += Math.PI * 2.0D;
        int sector = (int) (rel / ((Math.PI * 2.0D) / count));
        return Math.max(0, Math.min(count - 1, sector));
    }
}
