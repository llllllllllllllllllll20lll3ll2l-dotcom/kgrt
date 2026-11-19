package com.manyl.mod.client.screen;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.menu.GolemInventoryMenu;
import com.manyl.mod.network.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

/**
 * Экран инвентаря стража: броня (4 слота), щит, еда, груз, ранг и переименование.
 */
public class GolemInventoryScreen extends AbstractContainerScreen<GolemInventoryMenu> {
    private static final Identifier TEXTURE = ManylMod.id("textures/gui/golem_inventory.png");
    private static final Identifier HUNGER_FULL = ManylMod.id("textures/gui/icons/hunger_full.png");
    private static final Identifier HUNGER_HALF = ManylMod.id("textures/gui/icons/hunger_half.png");
    private static final Identifier HUNGER_EMPTY = ManylMod.id("textures/gui/icons/hunger_empty.png");

    private static final int SIDE_W = 94;
    private static final int SIDE_H = 152;

    private Button sitToggleBtn;
    private EditBox nameField;

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

            String initialName = golem.hasCustomName() && golem.getCustomName() != null
                    ? golem.getCustomName().getString() : "";
            this.nameField = new EditBox(this.font, sx + 6, sy + 44, SIDE_W - 12, 16,
                    Component.translatable("gui.manyl_mod.rename_placeholder"));
            this.nameField.setMaxLength(32);
            this.nameField.setValue(initialName);
            this.nameField.setHint(Component.translatable("gui.manyl_mod.rename_placeholder"));
            this.addRenderableWidget(this.nameField);

            this.addRenderableWidget(Button.builder(Component.translatable("gui.manyl_mod.rename_apply"), b -> {
                if (this.nameField != null) {
                    ClientPlayNetworking.send(new ModNetworking.RenameGolemPayload(golem.getId(), this.nameField.getValue()));
                    this.nameField.setFocused(false);
                }
            }).pos(sx + 6, sy + 63).size(SIDE_W - 12, 16).build());

            this.sitToggleBtn = this.addRenderableWidget(Button.builder(sitButtonLabel(golem), b ->
                    ClientPlayNetworking.send(new ModNetworking.ToggleGolemSitPayload(golem.getId())))
                    .pos(sx + 6, sy + 85)
                    .size(SIDE_W - 12, 18)
                    .build());

            this.addRenderableWidget(Button.builder(Component.translatable("gui.manyl_mod.dismiss_one"), b -> {
                ClientPlayNetworking.send(new ModNetworking.DismissGolemPayload(golem.getId()));
                this.onClose();
            }).pos(sx + 6, sy + 106).size(SIDE_W - 12, 18).build());

            this.addRenderableWidget(Button.builder(Component.translatable("gui.manyl_mod.squad_manage"), b ->
                    ClientPlayNetworking.send(new ModNetworking.RequestOpenManagementPayload()))
                    .pos(sx + 6, sy + 127)
                    .size(SIDE_W - 12, 18)
                    .build());
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.nameField != null && this.nameField.isFocused()) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                this.nameField.setFocused(false);
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                BaseGolemEntity golem = this.menu.getGolem();
                if (golem != null) {
                    ClientPlayNetworking.send(new ModNetworking.RenameGolemPayload(golem.getId(), this.nameField.getValue()));
                }
                this.nameField.setFocused(false);
                return true;
            }
            return this.nameField.keyPressed(event);
        }
        return super.keyPressed(event);
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

        // Отрисовка 4 слотов брони + слот щита в верхнем ряду (y=18)
        int[] armorXs = {64, 82, 100, 118, 148};
        for (int ax : armorXs) {
            drawSlotBox(graphics, x + ax - 1, y + 17);
        }

        // Боковая панель управления справа
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

            int hpWidth = Math.round(48.0F * (golem.getHealth() / golem.getMaxHealth()));
            if (hpWidth > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 8, y + 74, 176, 10, hpWidth, 3, 256, 256);
            }

            // Ранг и сытость на правой панели
            Component rankComp = Component.translatable(golem.getRankTranslationKey())
                    .append(Component.literal(" (" + golem.getKills() + ")"));
            graphics.drawString(this.font, rankComp, sx + 6, sy + 19, 0xFFF2D27A, false);

            int hunger = golem.getHunger();
            Identifier hungerIcon = hunger > 50 ? HUNGER_FULL : (hunger > 15 ? HUNGER_HALF : HUNGER_EMPTY);
            graphics.blit(RenderPipelines.GUI_TEXTURED, hungerIcon, sx + 6, sy + 30, 0, 0, 9, 9, 9, 9);
            graphics.drawString(this.font, Component.translatable("gui.manyl_mod.hunger")
                    .append(Component.literal(": " + hunger + "%")), sx + 18, sy + 31, 0xFFDADEE8, false);
        }
    }

    private void drawSlotBox(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        g.fill(x, y, x + 17, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + 17, 0xFF373737);
        g.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        g.fill(x + 17, y + 1, x + 18, y + 18, 0xFFFFFFFF);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFE8C87A, false);
        graphics.drawString(this.font, Component.translatable("gui.manyl_mod.armor_label"), 64, 7, 0xFFB8C0D0, false);
        graphics.drawString(this.font, Component.translatable("gui.manyl_mod.shield_label"), 144, 7, 0xFFB8C0D0, false);

        BaseGolemEntity golem = this.menu.getGolem();
        if (golem != null) {
            String[] hints = {"H", "C", "L", "B"};
            int[] xs = {69, 87, 105, 123};
            for (int i = 0; i < 4; i++) {
                if (golem.getGolemInventory().getItem(BaseGolemEntity.ARMOR_HEAD_SLOT + i).isEmpty()) {
                    graphics.drawString(this.font, hints[i], xs[i], 22, 0xFF555B6A, false);
                }
            }
            if (golem.getGolemInventory().getItem(BaseGolemEntity.SHIELD_SLOT).isEmpty()) {
                graphics.drawString(this.font, "S", 153, 22, 0xFF555B6A, false);
            }
        }

        graphics.drawString(this.font, Component.translatable("gui.manyl_mod.food_slot"), 63, 58, 0xFFD4A85C, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFB8C0D0, false);
    }
}
