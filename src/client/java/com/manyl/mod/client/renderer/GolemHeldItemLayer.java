package com.manyl.mod.client.renderer;

import com.manyl.mod.client.model.GolemModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Слой рендера оружия в руке ультра-HD 2-блочного голема-воина (учитывает 0.0625x масштаб модели).
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
        // Позиция кисти в 16x координатах модели + масштаб 14.4F (14.4 * 0.0625 = 0.90)
        matrices.translate(-2.00F, 9.40F, -1.40F);
        matrices.mulPose(Axis.XP.rotationDegrees(-90.0F));
        matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
        matrices.scale(14.4F, 14.4F, 14.4F);

        state.heldItem.submit(matrices, commandQueue, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        matrices.popPose();
    }
}
