package com.manyl.mod.client.renderer;

import com.manyl.mod.client.model.GolemModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Слой свечения (_glow.png) для глаз, ядра на груди, фонарика копателя и антенны сортировщика.
 */
public class GolemGlowLayer extends RenderLayer<GolemRenderState, GolemModel> {
    private static final int FULL_BRIGHT = 15728880;

    public GolemGlowLayer(RenderLayerParent<GolemRenderState, GolemModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector commandQueue, int light,
                       GolemRenderState state, float limbAngle, float limbDistance) {
        if (state.isDisabled) {
            return;
        }
        commandQueue.order(1).submitModel(
                this.getParentModel(),
                state,
                matrices,
                RenderTypes.eyes(state.role.getGlowTexture()),
                FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null
        );
    }
}
