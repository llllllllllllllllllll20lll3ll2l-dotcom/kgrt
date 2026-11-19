package com.manyl.mod.client.renderer;

import com.manyl.mod.client.model.GolemModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Слой рендера щита в левой руке 2-блочного голема-воина.
 */
public class GolemLeftHandLayer extends RenderLayer<GolemRenderState, GolemModel> {
    public GolemLeftHandLayer(RenderLayerParent<GolemRenderState, GolemModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector commandQueue, int light,
                       GolemRenderState state, float limbAngle, float limbDistance) {
        if (state.offhandItem.isEmpty()) {
            return;
        }
        matrices.pushPose();
        this.getParentModel().translateToLeftHand(matrices);
        matrices.translate(3.00F, 14.10F, -2.10F);
        matrices.mulPose(Axis.XP.rotationDegrees(-90.0F));
        matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
        matrices.scale(21.6F, 21.6F, 21.6F);

        state.offhandItem.submit(matrices, commandQueue, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        matrices.popPose();
    }
}
