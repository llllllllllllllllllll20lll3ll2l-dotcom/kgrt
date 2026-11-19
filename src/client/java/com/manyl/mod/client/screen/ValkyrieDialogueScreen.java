package com.manyl.mod.client.screen;

import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.entity.ValkyrieGolemEntity;
import com.manyl.mod.network.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Компактный диалоговый экран с вкладками «Общение» и «Анимации» для Голема-воительницы.
 */
public class ValkyrieDialogueScreen extends Screen {
    private static final int PANEL_W = 316;
    private static final int PANEL_H = 218;

    private final int entityId;
    private boolean tamed;
    private boolean sitting;
    private int affection;
    private int romanceStage;
    private int jealousy;
    private boolean vowBound;
    private String dialogueLine;
    private int activeTab = 0; // 0 = Общение и забота, 1 = Анимации ♥

    private Button tabChatBtn;
    private Button tabAnimsBtn;
    private Button recruitBtn;
    private Button talkBtn;
    private Button complimentBtn;
    private Button giftBtn;
    private Button confessBtn;
    private Button patBtn;
    private Button hugBtn;
    private Button kissBtn;
    private Button blowKissBtn;
    private Button holdHandsBtn;
    private Button danceBtn;
    private Button saluteBtn;
    private Button shyBtn;
    private Button bowBtn;
    private Button weddingBtn;
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
        this.jealousy = payload.jealousy();
        this.vowBound = payload.vowBound();
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
        int top = Math.max(4, (this.height - PANEL_H) / 2);

        this.addRenderableWidget(Button.builder(Component.literal("✕"), b -> this.onClose())
                .pos(left + PANEL_W - 24, top + 7)
                .size(16, 15)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.manyl_mod.squad_short"), b ->
                        ClientPlayNetworking.send(new ModNetworking.RequestOpenManagementPayload()))
                .pos(left + PANEL_W - 76, top + 7)
                .size(48, 15)
                .build());

        this.recruitBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.recruit"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_RECRUIT))
                .pos(left + 14, top + 124)
                .size(PANEL_W - 28, 22)
                .build());

        int halfW = (PANEL_W - 34) / 2;
        int thirdW = (PANEL_W - 36) / 3;

        // Переключатель вкладок
        this.tabChatBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.tab.chat"),
                        b -> {
                            this.activeTab = 0;
                            rebuildButtonsState();
                        })
                .pos(left + 14, top + 102)
                .size(halfW, 16)
                .build());

        this.tabAnimsBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.tab.anims"),
                        b -> {
                            this.activeTab = 1;
                            rebuildButtonsState();
                        })
                .pos(left + 20 + halfW, top + 102)
                .size(halfW, 16)
                .build());

        // Вкладка 0: Общение и забота
        this.talkBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.talk"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_TALK))
                .pos(left + 14, top + 122)
                .size(halfW, 17)
                .build());

        this.complimentBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.compliment"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_COMPLIMENT))
                .pos(left + 20 + halfW, top + 122)
                .size(halfW, 17)
                .build());

        this.giftBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.gift"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_GIFT))
                .pos(left + 14, top + 142)
                .size(halfW, 17)
                .build());

        this.confessBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.confess"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_CONFESS))
                .pos(left + 20 + halfW, top + 142)
                .size(halfW, 17)
                .build());

        this.weddingBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.wedding"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_WEDDING))
                .pos(left + 14, top + 162)
                .size(halfW, 17)
                .build());

        this.modeBtn = this.addRenderableWidget(Button.builder(
                        modeLabel(),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_TOGGLE_SIT))
                .pos(left + 20 + halfW, top + 162)
                .size(halfW, 17)
                .build());

        this.gearBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.gear"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_OPEN_INVENTORY))
                .pos(left + 14, top + 182)
                .size(PANEL_W - 28, 17)
                .build());

        // Вкладка 1: Интерактивные анимации
        this.patBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.pat"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_PAT))
                .pos(left + 14, top + 122)
                .size(thirdW, 18)
                .build());

        this.hugBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.hug"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_HUG))
                .pos(left + 18 + thirdW, top + 122)
                .size(thirdW, 18)
                .build());

        this.kissBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.kiss"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_KISS))
                .pos(left + 22 + thirdW * 2, top + 122)
                .size(PANEL_W - 36 - thirdW * 2, 18)
                .build());

        this.blowKissBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.blow_kiss"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_BLOW_KISS))
                .pos(left + 14, top + 144)
                .size(thirdW, 18)
                .build());

        this.holdHandsBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.hold_hands"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_HOLD_HANDS))
                .pos(left + 18 + thirdW, top + 144)
                .size(thirdW, 18)
                .build());

        this.danceBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.dance"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_DANCE))
                .pos(left + 22 + thirdW * 2, top + 144)
                .size(PANEL_W - 36 - thirdW * 2, 18)
                .build());

        this.saluteBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.salute"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_SALUTE))
                .pos(left + 14, top + 166)
                .size(thirdW, 18)
                .build());

        this.shyBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.shy"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_SHY))
                .pos(left + 18 + thirdW, top + 166)
                .size(thirdW, 18)
                .build());

        this.bowBtn = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.manyl_mod.valkyrie.bow"),
                        b -> sendAction(ValkyrieGolemEntity.ACTION_BOW))
                .pos(left + 22 + thirdW * 2, top + 166)
                .size(PANEL_W - 36 - thirdW * 2, 18)
                .build());

        rebuildButtonsState();
    }

    private void rebuildButtonsState() {
        if (recruitBtn != null) recruitBtn.visible = !tamed;

        boolean chatVisible = tamed && activeTab == 0;
        boolean animsVisible = tamed && activeTab == 1;

        if (tabChatBtn != null) {
            tabChatBtn.visible = tamed;
            tabChatBtn.active = activeTab != 0;
        }
        if (tabAnimsBtn != null) {
            tabAnimsBtn.visible = tamed;
            tabAnimsBtn.active = activeTab != 1;
        }

        if (talkBtn != null) talkBtn.visible = chatVisible;
        if (complimentBtn != null) complimentBtn.visible = chatVisible;
        if (giftBtn != null) giftBtn.visible = chatVisible;
        if (confessBtn != null) {
            confessBtn.visible = chatVisible;
            confessBtn.active = romanceStage < ValkyrieGolemEntity.STAGE_BELOVED
                    && affection >= ValkyrieGolemEntity.CONFESS_THRESHOLD;
            confessBtn.setMessage(Component.translatable(
                    romanceStage >= ValkyrieGolemEntity.STAGE_BELOVED
                            ? "gui.manyl_mod.valkyrie.beloved_badge"
                            : "gui.manyl_mod.valkyrie.confess"
            ));
        }
        if (weddingBtn != null) {
            weddingBtn.visible = chatVisible;
            weddingBtn.setMessage(Component.translatable(
                    vowBound ? "gui.manyl_mod.valkyrie.wedding_renew" : "gui.manyl_mod.valkyrie.wedding"
            ));
        }
        if (modeBtn != null) {
            modeBtn.visible = chatVisible;
            modeBtn.setMessage(modeLabel());
        }
        if (gearBtn != null) gearBtn.visible = chatVisible;

        if (patBtn != null) patBtn.visible = animsVisible;
        if (hugBtn != null) hugBtn.visible = animsVisible;
        if (kissBtn != null) kissBtn.visible = animsVisible;
        if (blowKissBtn != null) blowKissBtn.visible = animsVisible;
        if (holdHandsBtn != null) holdHandsBtn.visible = animsVisible;
        if (danceBtn != null) danceBtn.visible = animsVisible;
        if (saluteBtn != null) saluteBtn.visible = animsVisible;
        if (shyBtn != null) shyBtn.visible = animsVisible;
        if (bowBtn != null) bowBtn.visible = animsVisible;
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
        int top = Math.max(4, (this.height - PANEL_H) / 2);

        // Тёмно-индиговая подложка с золотой рамкой
        graphics.fill(left, top, left + PANEL_W, top + PANEL_H, 0xEE141724);
        graphics.fill(left, top, left + PANEL_W, top + 2, 0xFFD9AE5F);
        graphics.fill(left, top + PANEL_H - 2, left + PANEL_W, top + PANEL_H, 0xFFD9AE5F);
        graphics.fill(left, top, left + 2, top + PANEL_H, 0xFFD9AE5F);
        graphics.fill(left + PANEL_W - 2, top, left + PANEL_W, top + PANEL_H, 0xFFD9AE5F);

        // Иконка и заголовок
        graphics.blit(RenderPipelines.GUI_TEXTURED, GolemRole.VALKYRIE.getIconTexture(),
                left + 14, top + 8, 0, 0, 18, 18, 18, 18);
        graphics.drawString(this.font, this.title, left + 38, top + 7, 0xFFF3C6E2, false);

        // Статус отношений
        Component stageText = stageComponent();
        int stageColor = vowBound ? 0xFFFFD86E : switch (romanceStage) {
            case ValkyrieGolemEntity.STAGE_BELOVED -> 0xFFFF6CA8;
            case ValkyrieGolemEntity.STAGE_CRUSH -> 0xFFF59FD0;
            case ValkyrieGolemEntity.STAGE_FRIEND -> 0xFFE8C87A;
            default -> 0xFFB8C4D8;
        };
        graphics.drawString(this.font, stageText, left + 38, top + 17, stageColor, false);

        // Полоска близости (♥ 0..100) и Полоска ревности (⚡ 0..100)
        int barX = left + 14;
        int barY = top + 30;
        int totalW = PANEL_W - 28;
        boolean showJealousy = tamed && (jealousy > 0 || romanceStage >= ValkyrieGolemEntity.STAGE_FRIEND);
        int affW = showJealousy ? (totalW - 6) / 2 : totalW;

        graphics.fill(barX, barY, barX + affW, barY + 9, 0xFF23283D);
        int fillW = Math.round(affW * (affection / (float) ValkyrieGolemEntity.MAX_AFFECTION));
        if (fillW > 0) {
            int barColor = romanceStage >= ValkyrieGolemEntity.STAGE_BELOVED ? 0xFFFF4D94 : 0xFFE866B8;
            graphics.fill(barX, barY, barX + fillW, barY + 9, barColor);
        }
        String affLabel = "♥ " + affection + "%";
        graphics.drawCenteredString(this.font, affLabel, barX + affW / 2, barY + 1, 0xFFFFFFFF);

        if (showJealousy) {
            int jX = barX + affW + 6;
            int jW = totalW - affW - 6;
            graphics.fill(jX, barY, jX + jW, barY + 9, 0xFF2A1D24);
            int jFill = Math.round(jW * (jealousy / (float) ValkyrieGolemEntity.MAX_JEALOUSY));
            if (jFill > 0) {
                int jColor = jealousy >= 75 ? 0xFFE83E4A : 0xFFF09040;
                graphics.fill(jX, barY, jX + jFill, barY + 9, jColor);
            }
            Component jLabel = Component.translatable("gui.manyl_mod.valkyrie.jealousy_bar", jealousy);
            graphics.drawCenteredString(this.font, jLabel, jX + jW / 2, barY + 1, 0xFFFFFFFF);
        }

        // Активная плюшка уровня влюблённости (гарантированно помещается по ширине)
        Component perkText = perkComponent();
        List<FormattedCharSequence> perkLines = this.font.split(perkText, totalW);
        if (!perkLines.isEmpty()) {
            graphics.drawString(this.font, perkLines.get(0), left + 14, top + 42, 0xFFAEE2B8, false);
        }

        // Окошко с репликой
        int boxX = left + 14;
        int boxY = top + 54;
        int boxW = totalW;
        int boxH = 44;
        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xAA0D101B);
        graphics.fill(boxX, boxY, boxX + boxW, boxY + 1, 0x88E866B8);
        graphics.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, 0x88E866B8);

        List<FormattedCharSequence> lines = this.font.split(Component.literal(dialogueLine), boxW - 14);
        int lineY = boxY + 6;
        for (int i = 0; i < Math.min(lines.size(), 3); i++) {
            graphics.drawString(this.font, lines.get(i), boxX + 7, lineY, 0xFFECEFF4, false);
            lineY += 11;
        }

        // Подсказка в самом низу (внутри рамки, без наезда на кнопки)
        if (tamed && romanceStage < ValkyrieGolemEntity.STAGE_BELOVED && affection < ValkyrieGolemEntity.CONFESS_THRESHOLD) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("gui.manyl_mod.valkyrie.confess_hint", ValkyrieGolemEntity.CONFESS_THRESHOLD),
                    left + PANEL_W / 2, top + PANEL_H - 13, 0xFF9AA7C0);
        } else if (tamed) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("gui.manyl_mod.valkyrie.gift_bar_hint"),
                    left + PANEL_W / 2, top + PANEL_H - 13, 0xFF9AA7C0);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    private Component stageComponent() {
        if (vowBound) {
            return Component.translatable("gui.manyl_mod.valkyrie.stage.spouse");
        }
        return switch (romanceStage) {
            case ValkyrieGolemEntity.STAGE_BELOVED -> Component.translatable("gui.manyl_mod.valkyrie.stage.beloved");
            case ValkyrieGolemEntity.STAGE_CRUSH -> Component.translatable("gui.manyl_mod.valkyrie.stage.crush");
            case ValkyrieGolemEntity.STAGE_FRIEND -> Component.translatable("gui.manyl_mod.valkyrie.stage.friend");
            default -> Component.translatable(tamed ? "gui.manyl_mod.valkyrie.stage.ally" : "gui.manyl_mod.valkyrie.stage.wild");
        };
    }

    private Component perkComponent() {
        if (!tamed) {
            return Component.translatable("gui.manyl_mod.valkyrie.perk.none");
        }
        return switch (romanceStage) {
            case ValkyrieGolemEntity.STAGE_BELOVED -> Component.translatable("gui.manyl_mod.valkyrie.perk.beloved");
            case ValkyrieGolemEntity.STAGE_CRUSH -> Component.translatable("gui.manyl_mod.valkyrie.perk.crush");
            case ValkyrieGolemEntity.STAGE_FRIEND -> Component.translatable("gui.manyl_mod.valkyrie.perk.friend");
            default -> Component.translatable("gui.manyl_mod.valkyrie.perk.ally");
        };
    }
}
