package com.manyl.mod.client.renderer;

import com.manyl.mod.client.model.ValkyrieModel;
import com.manyl.mod.entity.ValkyrieGolemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

/**
 * Слой плавного высокополигонального Smooth-Mesh рендера для режима !@ADMIN_OP.
 */
public class ValkyrieSmoothMeshLayer extends RenderLayer<GolemRenderState, ValkyrieModel> {
    public ValkyrieSmoothMeshLayer(RenderLayerParent<GolemRenderState, ValkyrieModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector commandQueue, int light,
                       GolemRenderState state, float limbAngle, float limbDistance) {
        if (state.isDisabled || !state.opMode || state.opOutfit == ValkyrieGolemEntity.OP_OUTFIT_NONE) {
            return;
        }
        matrices.pushPose();
        matrices.translate(0.0F, 1.501F, 0.0F);
        matrices.scale(-1.0F, -1.0F, 1.0F);
        matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
        matrices.scale(1.12F, 1.12F, 1.12F);
        ValkyrieSmoothMeshEngine.render(matrices, commandQueue, light, state, limbAngle, limbDistance);
        matrices.popPose();
    }
}
