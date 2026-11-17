package com.manyl.mod.client.renderer;

import com.manyl.mod.client.model.GolemModel;
import com.manyl.mod.entity.BaseGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/**
 * Рендерер всех 4 ролей големов-прислужников (использует общую модель и ролевую текстуру + слой свечения).
 */
public class GolemRenderer extends MobRenderer<BaseGolemEntity, GolemRenderState, GolemModel> {
    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel(context.bakeLayer(GolemModel.LAYER_LOCATION)), 0.38F);
        this.addLayer(new GolemGlowLayer(this));
    }

    @Override
    public GolemRenderState createRenderState() {
        return new GolemRenderState();
    }

    @Override
    public void extractRenderState(BaseGolemEntity entity, GolemRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        state.role = entity.getRole();
        state.hunger = entity.getHunger();
        state.isSitting = entity.isSitting();
        state.isCarrying = entity.isCarrying();
        state.isWorking = entity.isWorking();

        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.workAnimationState.copyFrom(entity.workAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.carryAnimationState.copyFrom(entity.carryAnimationState);
        state.sitAnimationState.copyFrom(entity.sitAnimationState);
        state.summonAnimationState.copyFrom(entity.summonAnimationState);
        state.eatAnimationState.copyFrom(entity.eatAnimationState);
    }

    @Override
    public Identifier getTextureLocation(GolemRenderState state) {
        return state.role.getTexture();
    }
}
