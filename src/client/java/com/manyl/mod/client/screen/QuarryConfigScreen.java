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
 * Меню настройки размеченного карьера (открывается автоматически после выделения 2-й точки Посохом карьера).
 */
public class QuarryConfigScreen extends Screen {
    private static final int W = 264;
    private static final int H = 210;

    private final int minX, maxX, minZ, maxZ, topY;
    private int quarryDepth;
    private boolean quarryStairs;
    private DigMode digMode;
    private String extractFilter;
    private boolean dropTrash;

    private Button stairsBtn;
    private Button modeBtn;
    private Button trashBtn;
    private EditBox filterBox;

    public QuarryConfigScreen(ModNetworking.OpenQuarryConfigPayload p) {
        super(Component.translatable("screen.manyl_mod.quarry_config"));
        this.minX = p.minX();
        this.maxX = p.maxX();
        this.minZ = p.minZ();
        this.maxZ = p.maxZ();
        this.topY = p.topY();
        this.quarryDepth = p.quarryDepth();
        this.quarryStairs = p.quarryStairs();
        this.digMode = DigMode.byOrdinal(p.digModeOrdinal());
        this.extractFilter = p.extractFilter() == null ? "" : p.extractFilter();
        this.dropTrash = p.dropTrash();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        // Глубина карьера (-5, -1, +1, +5)
        this.addRenderableWidget(Button.builder(Component.literal("-5"), b -> quarryDepth = Math.max(2, quarryDepth - 5))
                .pos(left + 12, top + 58).size(26, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> quarryDepth = Math.max(2, quarryDepth - 1))
                .pos(left + 40, top + 58).size(22, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> quarryDepth = Math.min(64, quarryDepth + 1))
                .pos(left + 114, top + 58).size(22, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("+5"), b -> quarryDepth = Math.min(64, quarryDepth + 5))
                .pos(left + 138, top + 58).size(26, 18).build());

        // Режим копки (Карьер / Туннель / Только руды)
        this.modeBtn = this.addRenderableWidget(Button.builder(Component.translatable(digMode.getTranslationKey()), b -> {
            digMode = digMode.next();
            b.setMessage(Component.translatable(digMode.getTranslationKey()));
        }).pos(left + 170, top + 58).size(82, 18).build());

        // Фильтр цели (!extract)
        this.filterBox = new EditBox(this.font, left + 12, top + 94, 240, 18, Component.translatable("gui.manyl_mod.extract_filter"));
        this.filterBox.setMaxLength(48);
        this.filterBox.setValue(this.extractFilter);
        this.filterBox.setHint(Component.translatable("gui.manyl_mod.extract_hint"));
        this.addRenderableWidget(this.filterBox);

        // Каменная лестница по кругу
        this.stairsBtn = this.addRenderableWidget(Button.builder(onOff(quarryStairs), b -> {
            quarryStairs = !quarryStairs;
            b.setMessage(onOff(quarryStairs));
        }).pos(left + 194, top + 122).size(58, 16).build());

        // Оставлять 1 стак стройблоков
        this.trashBtn = this.addRenderableWidget(Button.builder(onOff(dropTrash), b -> {
            dropTrash = !dropTrash;
            b.setMessage(onOff(dropTrash));
        }).pos(left + 194, top + 144).size(58, 16).build());

        // Кнопки запуска, сброса и отмены
        this.addRenderableWidget(Button.builder(Component.translatable("gui.manyl_mod.quarry_start"), b -> {
            String f = filterBox != null ? filterBox.getValue().trim() : extractFilter;
            ClientPlayNetworking.send(new ModNetworking.SaveQuarryConfigPayload(
                    quarryDepth, quarryStairs, digMode.ordinal(), f, dropTrash, true, false
            ));
            this.onClose();
        }).pos(left + 12, top + 176).size(116, 22).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.manyl_mod.quarry_clear"), b -> {
            ClientPlayNetworking.send(new ModNetworking.SaveQuarryConfigPayload(
                    quarryDepth, quarryStairs, digMode.ordinal(), "", dropTrash, false, true
            ));
            this.onClose();
        }).pos(left + 134, top + 176).size(62, 22).build());

        this.addRenderableWidget(Button.builder(Component.literal("✕"), b -> this.onClose())
                .pos(left + 202, top + 176).size(50, 22).build());
    }

    private Component onOff(boolean v) {
        return Component.translatable(v ? "gui.manyl_mod.on" : "gui.manyl_mod.off");
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        g.fill(0, 0, this.width, this.height, 0x66000000);
        g.fill(left, top, left + W, top + H, 0xFF282C36);
        g.fill(left, top, left + W, top + 1, 0xFFE59D3B);
        g.fill(left, top + H - 1, left + W, top + H, 0xFFE59D3B);
        g.fill(left, top, left + 1, top + H, 0xFFE59D3B);
        g.fill(left + W - 1, top, left + W, top + H, 0xFFE59D3B);

        g.drawString(this.font, this.title, left + 12, top + 10, 0xFFFFCA62, false);

        int w = maxX - minX + 1;
        int l = maxZ - minZ + 1;
        String areaInfo = String.format("Зона: %d×%d бл. (верх Y=%d)", w, l, topY);
        g.drawString(this.font, areaInfo, left + 12, top + 28, 0xFFB8E994, false);
        if (w < 3 || l < 3) {
            g.drawString(this.font, Component.translatable("gui.manyl_mod.quarry_min_stair_warn"), left + 12, top + 40, 0xFFFF8866, false);
        }

        g.drawString(this.font, Component.translatable("gui.manyl_mod.depth"), left + 12, top + 47, 0xFFD0D5E0, false);
        g.drawCenteredString(this.font, quarryDepth + " бл.", left + 88, top + 63, 0xFFFFFFFF);

        g.drawString(this.font, Component.translatable("gui.manyl_mod.extract_filter"), left + 12, top + 82, 0xFFD0D5E0, false);
        g.drawString(this.font, Component.translatable("gui.manyl_mod.quarry_stairs"), left + 12, top + 126, 0xFFE0E5F0, false);
        g.drawString(this.font, Component.translatable("gui.manyl_mod.drop_trash"), left + 12, top + 148, 0xFFE0E5F0, false);

        super.render(g, mouseX, mouseY, delta);
    }
}
