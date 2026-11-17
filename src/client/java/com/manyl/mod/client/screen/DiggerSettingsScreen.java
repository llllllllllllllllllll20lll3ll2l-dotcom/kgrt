package com.manyl.mod.client.screen;

import com.manyl.mod.entity.DigMode;
import com.manyl.mod.network.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Отдельное окно настройки копания для големов-копателей:
 * - Режим копки (Карьер / Туннель / Только руды)
 * - Радиус копки (2..10) и глубина копания (2..12)
 * - Целевой блок/руда (!extract) с быстрыми кнопками и полем ввода
 * - Умный спуск к подземным рудам и выброс лишних стройблоков (оставляет 1 стак)
 * - Следование за игроком / на месте
 */
public class DiggerSettingsScreen extends Screen {
    private static final int W = 264;
    private static final int H = 224;

    private final Screen parent;
    private int diggerRadius;
    private int diggerDepth;
    private DigMode digMode;
    private boolean followOwner;
    private String extractFilter;
    private boolean diggerDropTrash;
    private boolean diggerTunnelToOre;

    private Button modeBtn;
    private Button followBtn;
    private Button tunnelBtn;
    private Button trashBtn;
    private EditBox filterBox;

    public DiggerSettingsScreen(Screen parent, int diggerRadius, int diggerDepth, DigMode digMode,
                                boolean followOwner, String extractFilter, boolean diggerDropTrash, boolean diggerTunnelToOre) {
        super(Component.translatable("screen.manyl_mod.digger_settings"));
        this.parent = parent;
        this.diggerRadius = diggerRadius;
        this.diggerDepth = diggerDepth;
        this.digMode = digMode;
        this.followOwner = followOwner;
        this.extractFilter = extractFilter == null ? "" : extractFilter;
        this.diggerDropTrash = diggerDropTrash;
        this.diggerTunnelToOre = diggerTunnelToOre;
    }

    public void syncFromServer(ModNetworking.SyncGolemStatePayload p) {
        this.diggerRadius = p.diggerRadius();
        this.diggerDepth = p.diggerDepth();
        this.digMode = DigMode.byOrdinal(p.digModeOrdinal());
        this.followOwner = p.followOwner();
        this.extractFilter = p.extractFilter() == null ? "" : p.extractFilter();
        this.diggerDropTrash = p.diggerDropTrash();
        this.diggerTunnelToOre = p.diggerTunnelToOre();
        if (modeBtn != null) modeBtn.setMessage(Component.translatable(digMode.getTranslationKey()));
        if (followBtn != null) followBtn.setMessage(Component.translatable(followOwner ? "gui.manyl_mod.follow" : "gui.manyl_mod.stay"));
        if (tunnelBtn != null) tunnelBtn.setMessage(onOff(diggerTunnelToOre));
        if (trashBtn != null) trashBtn.setMessage(onOff(diggerDropTrash));
        if (filterBox != null && !filterBox.getValue().equals(this.extractFilter)) {
            filterBox.setValue(this.extractFilter);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        // Строка 1: Режим и следование
        this.modeBtn = this.addRenderableWidget(Button.builder(Component.translatable(digMode.getTranslationKey()), b -> {
            digMode = digMode.next();
            b.setMessage(Component.translatable(digMode.getTranslationKey()));
            sendSettings();
        }).pos(left + 12, top + 34).size(120, 18).build());

        this.followBtn = this.addRenderableWidget(Button.builder(
                Component.translatable(followOwner ? "gui.manyl_mod.follow" : "gui.manyl_mod.stay"), b -> {
                    followOwner = !followOwner;
                    b.setMessage(Component.translatable(followOwner ? "gui.manyl_mod.follow" : "gui.manyl_mod.stay"));
                    sendSettings();
                }).pos(left + 138, top + 34).size(114, 18).build());

        // Строка 2: Радиус (2..10) и Глубина (2..12)
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            diggerRadius = Math.max(2, diggerRadius - 1);
            sendSettings();
        }).pos(left + 12, top + 68).size(20, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            diggerRadius = Math.min(10, diggerRadius + 1);
            sendSettings();
        }).pos(left + 104, top + 68).size(20, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            diggerDepth = Math.max(2, diggerDepth - 1);
            sendSettings();
        }).pos(left + 138, top + 68).size(20, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            diggerDepth = Math.min(12, diggerDepth + 1);
            sendSettings();
        }).pos(left + 232, top + 68).size(20, 18).build());

        // Строка 3: Целевая руда/блок (!extract)
        this.filterBox = new EditBox(this.font, left + 12, top + 104, 166, 18, Component.translatable("gui.manyl_mod.extract_filter"));
        this.filterBox.setMaxLength(48);
        this.filterBox.setValue(this.extractFilter);
        this.filterBox.setHint(Component.translatable("gui.manyl_mod.extract_hint"));
        this.addRenderableWidget(this.filterBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.manyl_mod.apply"), b -> {
            this.extractFilter = this.filterBox.getValue().trim();
            sendSettings();
        }).pos(left + 184, top + 104).size(68, 18).build());

        // Быстрые пресеты руд
        String[] presets = {"", "iron", "diamond", "gold", "coal", "debris"};
        String[] keys = {
                "gui.manyl_mod.preset.all",
                "gui.manyl_mod.preset.iron",
                "gui.manyl_mod.preset.diamond",
                "gui.manyl_mod.preset.gold",
                "gui.manyl_mod.preset.coal",
                "gui.manyl_mod.preset.debris"
        };
        for (int i = 0; i < presets.length; i++) {
            final String preset = presets[i];
            int bx = left + 12 + (i % 3) * 81;
            int by = top + 126 + (i / 3) * 20;
            this.addRenderableWidget(Button.builder(Component.translatable(keys[i]), b -> {
                this.extractFilter = preset;
                this.filterBox.setValue(preset);
                sendSettings();
            }).pos(bx, by).size(78, 18).build());
        }

        // Строка 4: Умный спуск к рудам и сброс лишних стройблоков
        this.tunnelBtn = this.addRenderableWidget(Button.builder(onOff(diggerTunnelToOre), b -> {
            diggerTunnelToOre = !diggerTunnelToOre;
            b.setMessage(onOff(diggerTunnelToOre));
            sendSettings();
        }).pos(left + 194, top + 170).size(58, 16).build());

        this.trashBtn = this.addRenderableWidget(Button.builder(onOff(diggerDropTrash), b -> {
            diggerDropTrash = !diggerDropTrash;
            b.setMessage(onOff(diggerDropTrash));
            sendSettings();
        }).pos(left + 194, top + 190).size(58, 16).build());

        // Назад / Готово
        this.addRenderableWidget(Button.builder(Component.literal("←"), b -> goBack())
                .pos(left + W - 26, top + 6)
                .size(18, 16)
                .build());
    }

    private Component onOff(boolean v) {
        return Component.translatable(v ? "gui.manyl_mod.on" : "gui.manyl_mod.off");
    }

    private void goBack() {
        this.extractFilter = this.filterBox != null ? this.filterBox.getValue().trim() : this.extractFilter;
        sendSettings();
        if (parent != null) {
            this.minecraft.setScreen(parent);
        } else {
            this.onClose();
        }
    }

    private void sendSettings() {
        ClientPlayNetworking.send(new ModNetworking.UpdateDiggerSettingsPayload(
                diggerRadius, diggerDepth, digMode.ordinal(), followOwner,
                extractFilter, diggerDropTrash, diggerTunnelToOre
        ));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        g.fill(0, 0, this.width, this.height, 0x66000000);
        g.fill(left, top, left + W, top + H, 0xFF282C36);
        g.fill(left, top, left + W, top + 1, 0xFFBA8E3E);
        g.fill(left, top + H - 1, left + W, top + H, 0xFFBA8E3E);
        g.fill(left, top, left + 1, top + H, 0xFFBA8E3E);
        g.fill(left + W - 1, top, left + W, top + H, 0xFFBA8E3E);

        g.drawString(this.font, this.title, left + 12, top + 10, 0xFFE8C87A, false);

        g.drawString(this.font, Component.translatable("gui.manyl_mod.radius"), left + 12, top + 57, 0xFFD0D5E0, false);
        g.drawCenteredString(this.font, diggerRadius + " бл.", left + 68, top + 73, 0xFFFFFFFF);

        g.drawString(this.font, Component.translatable("gui.manyl_mod.depth"), left + 138, top + 57, 0xFFD0D5E0, false);
        g.drawCenteredString(this.font, diggerDepth + " бл.", left + 195, top + 73, 0xFFFFFFFF);

        g.drawString(this.font, Component.translatable("gui.manyl_mod.extract_filter"), left + 12, top + 93, 0xFFD0D5E0, false);

        g.drawString(this.font, Component.translatable("gui.manyl_mod.tunnel_to_ore"), left + 12, top + 174, 0xFFE0E5F0, false);
        g.drawString(this.font, Component.translatable("gui.manyl_mod.drop_trash"), left + 12, top + 194, 0xFFE0E5F0, false);

        super.render(g, mouseX, mouseY, delta);
    }
}
