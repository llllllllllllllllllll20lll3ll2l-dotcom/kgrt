package com.manyl.mod.client.renderer;

import com.manyl.mod.client.model.ValkyrieModel;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.entity.ValkyrieGolemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Рендерер Голема-Воительницы (16x ультра-HD модель с крылатой бронёй и косой).
 */
public class ValkyrieRenderer extends MobRenderer<BaseGolemEntity, GolemRenderState, ValkyrieModel> {
    private final ItemModelResolver itemModelResolver;

    public ValkyrieRenderer(EntityRendererProvider.Context context) {
        super(context, new ValkyrieModel(context.bakeLayer(ValkyrieModel.LAYER_LOCATION)), 0.50F);
        this.itemModelResolver = context.getItemModelResolver();
        this.addLayer(new ValkyrieGlowLayer(this));
        this.addLayer(new ValkyrieHeldItemLayer(this));
        this.addLayer(new ValkyrieLeftHandLayer(this));
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
        state.isDisabled = entity.isDisabled();
        if (entity instanceof ValkyrieGolemEntity valkyrie) {
            state.bathPhase = valkyrie.getBathPhase();
            state.bathTicks = valkyrie.getBathTicks();
            state.isDaydreaming = valkyrie.isInSwimsuit();
            state.daydreamTicks = valkyrie.getBathTicks();
            state.interactionAnim = valkyrie.getInteractionAnim();
            state.interactionTicks = net.minecraft.util.Mth.lerp(tickProgress, valkyrie.getPrevInteractionTicks(), valkyrie.getInteractionTicks());
            state.isVowBound = valkyrie.isVowBound();
        } else {
            state.bathPhase = 0;
            state.bathTicks = 0;
            state.isDaydreaming = false;
            state.daydreamTicks = 0;
            state.interactionAnim = 0;
            state.interactionTicks = 0.0F;
            state.isVowBound = false;
        }

        state.hasHeadArmor = !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        state.hasChestArmor = !entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        state.hasLegsArmor = !entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty();
        state.hasFeetArmor = !entity.getItemBySlot(EquipmentSlot.FEET).isEmpty();

        this.itemModelResolver.updateForLiving(
                state.heldItem,
                entity.getDisplayItem(),
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                entity
        );
        this.itemModelResolver.updateForLiving(
                state.offhandItem,
                entity.getOffhandItem(),
                ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
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
        float s = 1.0F / 34.08F;
        poseStack.scale(s, s, s);
    }

    @Override
    public Identifier getTextureLocation(GolemRenderState state) {
        return state.role.getTexture();
    }
}
