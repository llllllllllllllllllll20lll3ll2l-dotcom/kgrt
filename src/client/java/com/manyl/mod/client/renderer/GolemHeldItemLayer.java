package com.manyl.mod.client.renderer;

import com.manyl.mod.client.model.GolemModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Слой рендера оружия/предмета в правой руке голема (особенно у Воина).
 */
public class GolemHeldItemLayer extends RenderLayer<GolemRenderState, GolemModel> {
    public GolemHeldItemLayer(RenderLayerParent<GolemRenderState, GolemModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector commandQueue, int light,
                       GolemRenderState state, float limbAngle, float limbDistance) {
        if (state.heldItem.isEmpty()) {
            return;
        }
        matrices.pushPose();
        this.getParentModel().translateToRightHand(matrices);
        // Смещение в центр кисти и поворот оружия вперёд с масштабом под 0.8-блочного голема
        matrices.translate(-0.095F, 0.30F, -0.05F);
        matrices.mulPose(Axis.XP.rotationDegrees(-90.0F));
        matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
        matrices.scale(0.65F, 0.65F, 0.65F);

        state.heldItem.submit(matrices, commandQueue, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        matrices.popPose();
    }
}
