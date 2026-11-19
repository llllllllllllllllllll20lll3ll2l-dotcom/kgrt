package com.manyl.mod.client.screen;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.entity.ValkyrieGolemEntity;
import com.manyl.mod.network.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Диалоговый экран общения и тёплой романтической ветки с Големом-воительницей.
 */
public class ValkyrieDialogueScreen extends Screen {
    private static final Identifier PANEL_TEX = ManylMod.id("textures/gui/management_panel.png");
    private static final int PANEL_W = 280;
    private static final int PANEL_H = 212;

    private final int entityId;
    private boolean tamed;
    private boolean sitting;
    private int affection;
    private int romanceStage;
    private String dialogueLine;

    private Button recruitBtn;
    private Button talkBtn;
    private Button complimentBtn;
    private Button giftBtn;
    private Button confessBtn;
    private Button modeBtn;
    private Button gearBtn;

    public ValkyrieDialogueScreen(ModNetworking.OpenValkyrieDialoguePayload payload) {
        super(Component.translatable(GolemRole.VALKYRIE.getTranslationKey()));
        this.entityId = payload.entityId();
        applyPayload(payload);
    }

    public void updateFromPayload(ModNetworking.OpenValkyrieDialoguePayload payload) {
        if (payload.entityId() != this.entityId) return;
        applyPayload(payload);
        rebuildButtonsState();
    }

    private void applyPayload(ModNetworking.OpenValkyrieDialoguePayload payload) {
        this.tamed = payload.tamed();
        this.sitting = payload.sitting();
        this.affection = payload.affection();
        this.romanceStage = payload.romanceStage();
        this.dialogueLine = payload.dialogueLine();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        clearWidgets();
        int left = (this.width - PANEL_W) / 2;
        int top = (this.height - PANEL_H) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("✕"), b -> this.onClose())
                .pos(left + PANEL_W - 24, top + 8)
                .size(16, 16)
                .build());

        this.recruitBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.recruit"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_RECRUIT))
                .pos(left + 14, top + 124)
                .size(PANEL_W - 28, 22)
                .build());

        int halfW = (PANEL_W - 34) / 2;
        this.talkBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.talk"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_TALK))
                .pos(left + 14, top + 114)
                .size(halfW, 20)
                .build());

        this.complimentBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.compliment"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_COMPLIMENT))
                .pos(left + 20 + halfW, top + 114)
                .size(halfW, 20)
                .build());

        this.giftBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.gift"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_GIFT))
                .pos(left + 14, top + 138)
                .size(halfW, 20)
                .build());

        this.confessBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.confess"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_CONFESS))
                .pos(left + 20 + halfW, top + 138)
                .size(halfW, 20)
                .build());

        this.modeBtn = this.addRenderableWidget(Button.builder(
                        modeLabel(),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_TOGGLE_SIT))
                .pos(left + 14, top + 162)
                .size(halfW, 20)
                .build());

        this.gearBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.gear"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_OPEN_INVENTORY))
                .pos(left + 20 + halfW, top + 162)
                .size(halfW, 20)
                .build());

        rebuildButtonsState();
    }

    private void rebuildButtonsState() {
        if (recruitBtn != null) recruitBtn.visible = !tamed;
        if (talkBtn != null) talkBtn.visible = tamed;
        if (complimentBtn != null) complimentBtn.visible = tamed;
        if (giftBtn != null) giftBtn.visible = tamed;
        if (confessBtn != null) {
            confessBtn.visible = tamed;
            confessBtn.active = romanceStage < ValkyrieGolemEntity.STAGE_BELOVED
                    && affection >= ValkyrieGolemEntity.CONFESS_THRESHOLD;
            confessBtn.setMessage(Component.translatable(
                    romanceStage >= ValkyrieGolemEntity.STAGE_BELOVED
                            ? "gui.manyl_mod.valkyrie.beloved_badge"
                            : "gui.manyl_mod.valkyrie.confess"
            ));
        }
        if (modeBtn != null) {
            modeBtn.visible = tamed;
            modeBtn.setMessage(modeLabel());
        }
        if (gearBtn != null) gearBtn.visible = tamed;
    }

    private Component modeLabel() {
        return Component.translatable(sitting ? "gui.manyl_mod.stay" : "gui.manyl_mod.follow");
    }

    private void sendAction(int actionId) {
        ClientPlayNetworking.send(new ModNetworking.ValkyrieRomanceActionPayload(entityId, actionId));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int left = (this.width - PANEL_W) / 2;
        int top = (this.height - PANEL_H) / 2;

        // Тёмно-индиговая подложка с золотой рамкой
        graphics.fill(left, top, left + PANEL_W, top + PANEL_H, 0xEE141724);
        graphics.fill(left, top, left + PANEL_W, top + 2, 0xFFD9AE5F);
        graphics.fill(left, top + PANEL_H - 2, left + PANEL_W, top + PANEL_H, 0xFFD9AE5F);
        graphics.fill(left, top, left + 2, top + PANEL_H, 0xFFD9AE5F);
        graphics.fill(left + PANEL_W - 2, top, left + PANEL_W, top + PANEL_H, 0xFFD9AE5F);

        // Иконка и заголовок
        graphics.blit(RenderPipelines.GUI_TEXTURED, GolemRole.VALKYRIE.getIconTexture(),
                left + 14, top + 10, 0, 0, 18, 18, 18, 18);
        graphics.drawString(this.font, this.title, left + 38, top + 10, 0xFFF3C6E2, false);

        // Статус отношений
        Component stageText = stageComponent();
        int stageColor = switch (romanceStage) {
            case ValkyrieGolemEntity.STAGE_BELOVED -> 0xFFFF6CA8;
            case ValkyrieGolemEntity.STAGE_CRUSH -> 0xFFF59FD0;
            case ValkyrieGolemEntity.STAGE_FRIEND -> 0xFFE8C87A;
            default -> 0xFFB8C4D8;
        };
        graphics.drawString(this.font, stageText, left + 38, top + 21, stageColor, false);

        // Полоска близости (♥ 0..100)
        int barX = left + 14;
        int barY = top + 36;
        int barW = PANEL_W - 28;
        graphics.fill(barX, barY, barX + barW, barY + 8, 0xFF23283D);
        int fillW = Math.round(barW * (affection / (float) ValkyrieGolemEntity.MAX_AFFECTION));
        if (fillW > 0) {
            int barColor = romanceStage >= ValkyrieGolemEntity.STAGE_BELOVED ? 0xFFFF4D94 : 0xFFE866B8;
            graphics.fill(barX, barY, barX + fillW, barY + 8, barColor);
        }
        String affLabel = "♥ " + affection + " / " + ValkyrieGolemEntity.MAX_AFFECTION;
        graphics.drawCenteredString(this.font, affLabel, left + PANEL_W / 2, barY, 0xFFFFFFFF);

        // Окошко с репликой
        int boxX = left + 14;
        int boxY = top + 50;
        int boxW = PANEL_W - 28;
        int boxH = 56;
        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xAA0D101B);
        graphics.fill(boxX, boxY, boxX + boxW, boxY + 1, 0x88E866B8);
        graphics.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, 0x88E866B8);

        List<FormattedCharSequence> lines = this.font.split(Component.literal(dialogueLine), boxW - 14);
        int lineY = boxY + 7;
        for (int i = 0; i < Math.min(lines.size(), 5); i++) {
            graphics.drawString(this.font, lines.get(i), boxX + 7, lineY, 0xFFECEFF4, false);
            lineY += 10;
        }

        // Подсказка в самом низу
        if (tamed && romanceStage < ValkyrieGolemEntity.STAGE_BELOVED && affection < ValkyrieGolemEntity.CONFESS_THRESHOLD) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("gui.manyl_mod.valkyrie.confess_hint", ValkyrieGolemEntity.CONFESS_THRESHOLD),
                    left + PANEL_W / 2, top + PANEL_H - 20, 0xFF9AA7C0);
        } else if (tamed) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("gui.manyl_mod.valkyrie.gift_bar_hint"),
                    left + PANEL_W / 2, top + PANEL_H - 20, 0xFF9AA7C0);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    private Component stageComponent() {
        return switch (romanceStage) {
            case ValkyrieGolemEntity.STAGE_BELOVED -> Component.translatable("gui.manyl_mod.valkyrie.stage.beloved");
            case ValkyrieGolemEntity.STAGE_CRUSH -> Component.translatable("gui.manyl_mod.valkyrie.stage.crush");
            case ValkyrieGolemEntity.STAGE_FRIEND -> Component.translatable("gui.manyl_mod.valkyrie.stage.friend");
            default -> Component.translatable(tamed ? "gui.manyl_mod.valkyrie.stage.ally" : "gui.manyl_mod.valkyrie.stage.wild");
        };
    }
}
