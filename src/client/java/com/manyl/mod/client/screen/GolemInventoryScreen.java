package com.manyl.mod.client.screen;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.menu.GolemInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Экран встроенного инвентаря прислужника:
 * - Превью живой 3D-модели голема;
 * - Полосы сытости (голода) и здоровья;
 * - Слот под еду (слева) и 6 слотов груза (3x2 справа).
 */
public class GolemInventoryScreen extends AbstractContainerScreen<GolemInventoryMenu> {
    private static final Identifier TEXTURE = ManylMod.id("textures/gui/golem_inventory.png");
    private static final Identifier HUNGER_FULL = ManylMod.id("textures/gui/icons/hunger_full.png");
    private static final Identifier HUNGER_HALF = ManylMod.id("textures/gui/icons/hunger_half.png");
    private static final Identifier HUNGER_EMPTY = ManylMod.id("textures/gui/icons/hunger_empty.png");

    public GolemInventoryScreen(GolemInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        BaseGolemEntity golem = this.menu.getGolem();
        if (golem != null) {
            // Превью 3D-модели голема в левом окне
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics, x + 8, y + 17, x + 56, y + 73,
                    28, 0.08F, (float) mouseX, (float) mouseY, golem
            );

            // Полоса сытости (голод 0..100)
            int hunger = golem.getHunger();
            int fillWidth = Math.round(100.0F * hunger / (float) BaseGolemEntity.MAX_HUNGER);
            if (fillWidth > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 65, y + 20, 176, 0, fillWidth, 7, 256, 256);
            }

            // Иконка состояния голода рядом с полосой
            Identifier hungerIcon = hunger > 50 ? HUNGER_FULL : (hunger > 15 ? HUNGER_HALF : HUNGER_EMPTY);
            graphics.blit(RenderPipelines.GUI_TEXTURED, hungerIcon, x + 157, y + 19, 0, 0, 9, 9, 9, 9);

            // Тонкая полоска здоровья под окном превью
            int hpWidth = Math.round(48.0F * (golem.getHealth() / golem.getMaxHealth()));
            if (hpWidth > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 8, y + 74, 176, 10, hpWidth, 3, 256, 256);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFE8C87A, false);
        BaseGolemEntity golem = this.menu.getGolem();
        if (golem != null) {
            String hungerText = golem.getHunger() + "%";
            graphics.drawString(this.font, hungerText, 66, 30, 0xFFDADEE8, false);
        }
        graphics.drawString(this.font, Component.translatable("gui.manyl_mod.food_slot"), 63, 58, 0xFFD4A85C, false);
        graphics.drawString(this.font, Component.translatable("gui.manyl_mod.cargo"), 92, 30, 0xFFB8C0D0, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xFFB8C0D0, false);
    }
}
