package com.manyl.mod.client.renderer;

import com.manyl.mod.client.model.GolemModel;
import com.manyl.mod.entity.BaseGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Рендерер всех 4 ролей големов-прислужников (использует ролевые текстуры, слой свечения и оружие в руке).
 */
public class GolemRenderer extends MobRenderer<BaseGolemEntity, GolemRenderState, GolemModel> {
    private final ItemModelResolver itemModelResolver;

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel(context.bakeLayer(GolemModel.LAYER_LOCATION)), 0.55F);
        this.itemModelResolver = context.getItemModelResolver();
        this.addLayer(new GolemGlowLayer(this));
        this.addLayer(new GolemHeldItemLayer(this));
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

        this.itemModelResolver.updateForLiving(
                state.heldItem,
                entity.getDisplayItem(),
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                entity
        );

        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.workAnimationState.copyFrom(entity.workAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.carryAnimationState.copyFrom(entity.carryAnimationState);
        state.sitAnimationState.copyFrom(entity.sitAnimationState);
        state.summonAnimationState.copyFrom(entity.summonAnimationState);
        state.eatAnimationState.copyFrom(entity.eatAnimationState);
    }

    @Override
    protected void scale(GolemRenderState state, PoseStack poseStack) {
        poseStack.scale(0.25F, 0.25F, 0.25F);
    }

    @Override
    public Identifier getTextureLocation(GolemRenderState state) {
        return state.role.getTexture();
    }
}
