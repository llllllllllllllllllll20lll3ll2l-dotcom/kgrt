package com.manyl.mod.client.renderer;

import com.manyl.mod.client.model.ValkyrieModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Слой рендера щита в левой руке Голема-Воительницы.
 */
public class ValkyrieLeftHandLayer extends RenderLayer<GolemRenderState, ValkyrieModel> {
    public ValkyrieLeftHandLayer(RenderLayerParent<GolemRenderState, ValkyrieModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector commandQueue, int light,
                       GolemRenderState state, float limbAngle, float limbDistance) {
        if (state.offhandItem.isEmpty() || state.bathPhase >= 2 || state.interactionAnim >= 2
                || (state.opMode && state.opOutfit != 0)) {
            return;
        }
        matrices.pushPose();
        this.getParentModel().translateToLeftHand(matrices);
        matrices.translate(0.0F, 1.98F, -2.11F);
        matrices.mulPose(Axis.XP.rotationDegrees(-90.0F));
        matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
        matrices.scale(27.72F, 27.72F, 27.72F);

        state.offhandItem.submit(matrices, commandQueue, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        matrices.popPose();
    }
}
