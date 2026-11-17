package com.manyl.mod.client.screen;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.DigMode;
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
 * Полноценное окно управления прислужниками (открывается через «Управление» в круговом меню):
 * - Индивидуальный призыв / отзыв для каждого типа (Копатель, Фермер, Сортировщик, Воин);
 * - Счётчики активных прислужников против лимита (4 / 2 / 6 / 5);
 * - Настройки копателей: радиус копки (2..8), режим (Карьер / Туннель / Только руды) и следование за игроком.
 */
public class GolemManagementScreen extends Screen {
    private static final Identifier PANEL_TEX = ManylMod.id("textures/gui/management_panel.png");
    private static final int PANEL_W = 228;
    private static final int PANEL_H = 198;

    private final int[] counts = new int[GolemRole.values().length];
    private int diggerRadius;
    private DigMode digMode;
    private boolean followOwner;

    private Button modeButton;
    private Button followButton;

    public GolemManagementScreen(ModNetworking.OpenStaffMenuPayload payload) {
        super(Component.translatable("screen.manyl_mod.management"));
        for (GolemRole role : GolemRole.values()) {
            counts[role.ordinal()] = payload.countFor(role);
        }
        this.diggerRadius = payload.diggerRadius();
        this.digMode = DigMode.byOrdinal(payload.digModeOrdinal());
        this.followOwner = payload.followOwner();
    }

    public void updateState(ModNetworking.SyncGolemStatePayload payload) {
        for (GolemRole role : GolemRole.values()) {
            counts[role.ordinal()] = payload.countFor(role);
        }
        this.diggerRadius = payload.diggerRadius();
        this.digMode = DigMode.byOrdinal(payload.digModeOrdinal());
        this.followOwner = payload.followOwner();
        if (modeButton != null) {
            modeButton.setMessage(Component.translatable(digMode.getTranslationKey()));
        }
        if (followButton != null) {
            followButton.setMessage(Component.translatable(followOwner ? "gui.manyl_mod.follow" : "gui.manyl_mod.stay"));
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

        // 4 строки для ролей големов: кнопки [+ Призвать] и [- Отозвать]
        for (int i = 0; i < GolemRole.values().length; i++) {
            final int roleIdx = i;
            int ry = top + 34 + i * 27;
            this.addRenderableWidget(Button.builder(Component.literal("+"), b ->
                    ClientPlayNetworking.send(new ModNetworking.SummonGolemPayload(roleIdx)))
                    .pos(left + 162, ry)
                    .size(24, 18)
                    .build());

            this.addRenderableWidget(Button.builder(Component.literal("-"), b ->
                    ClientPlayNetworking.send(new ModNetworking.RecallGolemsPayload(roleIdx)))
                    .pos(left + 190, ry)
                    .size(24, 18)
                    .build());
        }

        // Настройки копателей (внизу панели)
        int cfgY = top + 148;
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            diggerRadius = Math.max(2, diggerRadius - 1);
            sendSettings();
        }).pos(left + 12, cfgY).size(18, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            diggerRadius = Math.min(8, diggerRadius + 1);
            sendSettings();
        }).pos(left + 64, cfgY).size(18, 18).build());

        this.modeButton = this.addRenderableWidget(Button.builder(Component.translatable(digMode.getTranslationKey()), b -> {
            digMode = digMode.next();
            b.setMessage(Component.translatable(digMode.getTranslationKey()));
            sendSettings();
        }).pos(left + 88, cfgY).size(68, 18).build());

        this.followButton = this.addRenderableWidget(Button.builder(
                Component.translatable(followOwner ? "gui.manyl_mod.follow" : "gui.manyl_mod.stay"), b -> {
                    followOwner = !followOwner;
                    b.setMessage(Component.translatable(followOwner ? "gui.manyl_mod.follow" : "gui.manyl_mod.stay"));
                    sendSettings();
                }).pos(left + 160, cfgY).size(56, 18).build());

        // Кнопка закрытия
        this.addRenderableWidget(Button.builder(Component.literal("✕"), b -> this.onClose())
                .pos(left + PANEL_W - 24, top + 6)
                .size(16, 16)
                .build());
    }

    private void sendSettings() {
        ClientPlayNetworking.send(new ModNetworking.UpdateDiggerSettingsPayload(diggerRadius, digMode.ordinal(), followOwner));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int left = (this.width - PANEL_W) / 2;
        int top = (this.height - PANEL_H) / 2;

        // Фон панели (верхняя часть и нижняя рамка из management_panel.png + тёмная подложка под настройки)
        graphics.blit(RenderPipelines.GUI_TEXTURED, PANEL_TEX, left, top, 0, 0, PANEL_W, 172, 256, 256);
        graphics.fill(left + 1, top + 170, left + PANEL_W - 1, top + PANEL_H - 1, 0xFF363A44);
        graphics.fill(left, top + PANEL_H - 1, left + PANEL_W, top + PANEL_H, 0xFF12141A);

        graphics.drawString(this.font, this.title, left + 10, top + 10, 0xFFE8C87A, false);

        // Иконки, названия и счётчики для каждого из 4 типов големов
        for (int i = 0; i < GolemRole.values().length; i++) {
            GolemRole role = GolemRole.byOrdinal(i);
            int ry = top + 34 + i * 27;
            graphics.blit(RenderPipelines.GUI_TEXTURED, role.getIconTexture(), left + 12, ry + 1, 0, 0, 16, 16, 16, 16);
            graphics.drawString(this.font, Component.translatable(role.getTranslationKey()), left + 34, ry + 5, 0xFFE5E9F0, false);

            String countLabel = counts[i] + " / " + role.getMaxPerPlayer();
            graphics.drawString(this.font, countLabel, left + 124, ry + 5, role.getAccentColor(), false);
        }

        // Подписи настроек копателей
        graphics.drawString(this.font, Component.translatable("gui.manyl_mod.digger_settings"), left + 12, top + 137, 0xFFE59D3B, false);
        graphics.drawCenteredString(this.font, String.valueOf(diggerRadius), left + 47, top + 153, 0xFFFFFFFF);

        super.render(graphics, mouseX, mouseY, delta);
    }
}
