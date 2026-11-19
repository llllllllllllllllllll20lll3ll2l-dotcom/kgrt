package com.manyl.mod.client.screen;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.menu.GolemInventoryMenu;
import com.manyl.mod.network.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Экран встроенного инвентаря прислужника с боковой панелью управления и роспуска отряда.
 */
public class GolemInventoryScreen extends AbstractContainerScreen<GolemInventoryMenu> {
    private static final Identifier TEXTURE = ManylMod.id("textures/gui/golem_inventory.png");
    private static final Identifier HUNGER_FULL = ManylMod.id("textures/gui/icons/hunger_full.png");
    private static final Identifier HUNGER_HALF = ManylMod.id("textures/gui/icons/hunger_half.png");
    private static final Identifier HUNGER_EMPTY = ManylMod.id("textures/gui/icons/hunger_empty.png");

    private static final int SIDE_W = 82;
    private static final int SIDE_H = 84;

    private Button sitToggleBtn;

    public GolemInventoryScreen(GolemInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelX = 63;
        this.inventoryLabelY = 75;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        BaseGolemEntity golem = this.menu.getGolem();
        if (golem != null) {
            int sx = this.leftPos + this.imageWidth + 4;
            int sy = this.topPos;
            this.sitToggleBtn = this.addRenderableWidget(Button.builder(sitButtonLabel(golem), b ->
                    ClientPlayNetworking.send(new ModNetworking.ToggleGolemSitPayload(golem.getId())))
                    .pos(sx + 6, sy + 18)
                    .size(SIDE_W - 12, 18)
                    .build());

            this.addRenderableWidget(Button.builder(Component.translatable("gui.manyl_mod.dismiss_one"), b -> {
                ClientPlayNetworking.send(new ModNetworking.DismissGolemPayload(golem.getId()));
                this.onClose();
            }).pos(sx + 6, sy + 39).size(SIDE_W - 12, 18).build());

            this.addRenderableWidget(Button.builder(Component.translatable("gui.manyl_mod.squad_manage"), b ->
                    ClientPlayNetworking.send(new ModNetworking.RequestOpenManagementPayload()))
                    .pos(sx + 6, sy + 60)
                    .size(SIDE_W - 12, 18)
                    .build());
        }
    }

    private Component sitButtonLabel(BaseGolemEntity golem) {
        return Component.translatable(golem.isSitting() ? "gui.manyl_mod.stay" : "gui.manyl_mod.follow");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        BaseGolemEntity golem = this.menu.getGolem();
        if (golem != null && sitToggleBtn != null) {
            sitToggleBtn.setMessage(sitButtonLabel(golem));
        }
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        // Боковая панель управления справа без перекрытия текстов и слотов
        int sx = x + this.imageWidth + 4;
        int sy = y;
        graphics.fill(sx, sy, sx + SIDE_W, sy + SIDE_H, 0xEE181C2B);
        graphics.fill(sx, sy, sx + SIDE_W, sy + 1, 0xFFD9AE5F);
        graphics.fill(sx, sy + SIDE_H - 1, sx + SIDE_W, sy + SIDE_H, 0xFFD9AE5F);
        graphics.fill(sx, sy, sx + 1, sy + SIDE_H, 0xFFD9AE5F);
        graphics.fill(sx + SIDE_W - 1, sy, sx + SIDE_W, sy + SIDE_H, 0xFFD9AE5F);
        graphics.drawCenteredString(this.font, Component.translatable("gui.manyl_mod.controls"),
                sx + SIDE_W / 2, sy + 6, 0xFFE8C87A);

        BaseGolemEntity golem = this.menu.getGolem();
        if (golem != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics, x + 8, y + 17, x + 56, y + 73,
                    19, 0.06F, (float) mouseX, (float) mouseY, golem
            );

            int hunger = golem.getHunger();
            int fillWidth = Math.round(90.0F * hunger / (float) BaseGolemEntity.MAX_HUNGER);
            if (fillWidth > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 65, y + 20, 176, 0, fillWidth, 7, 256, 256);
            }

            Identifier hungerIcon = hunger > 50 ? HUNGER_FULL : (hunger > 15 ? HUNGER_HALF : HUNGER_EMPTY);
            graphics.blit(RenderPipelines.GUI_TEXTURED, hungerIcon, x + 157, y + 19, 0, 0, 9, 9, 9, 9);

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
            graphics.drawString(this.font, hungerText, 62, 29, 0xFFDADEE8, false);
        }
        graphics.drawString(this.font, Component.translatable("gui.manyl_mod.food_slot"), 63, 58, 0xFFD4A85C, false);
        graphics.drawString(this.font, Component.translatable("gui.manyl_mod.cargo"), 94, 29, 0xFFB8C0D0, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFB8C0D0, false);
    }
}
