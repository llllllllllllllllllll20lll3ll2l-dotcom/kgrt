package com.manyl.mod.client.renderer;

import com.manyl.mod.ManylMod;
import com.manyl.mod.client.model.GolemModel;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.entity.GolemRole;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Рендерер всех 4 классов маленьких големов (1.21.11, со слоем свечения и слоем оружия в руке).
 */
public class GolemRenderer extends MobRenderer<BaseGolemEntity, GolemRenderState, GolemModel> {
    private static final Identifier DIGGER_TEX = ManylMod.id("textures/entity/golem_digger.png");
    private static final Identifier FARMER_TEX = ManylMod.id("textures/entity/golem_farmer.png");
    private static final Identifier SORTER_TEX = ManylMod.id("textures/entity/golem_sorter.png");
    private static final Identifier WARRIOR_TEX = ManylMod.id("textures/entity/golem_warrior.png");

    private final ItemModelResolver itemModelResolver;

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel(context.bakeLayer(GolemModel.LAYER_LOCATION)), 0.32F);
        this.itemModelResolver = context.getItemModelResolver();
        this.addLayer(new GolemGlowLayer(this));
        this.addLayer(new GolemHeldItemLayer(this));
    }

    @Override
    public GolemRenderState createRenderState() {
        return new GolemRenderState();
    }

    @Override
    public void extractRenderState(BaseGolemEntity entity, GolemRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.role = entity.getRole();
        state.hunger = entity.getHunger();
        state.working = entity.isWorking();
        state.carrying = entity.isCarrying();
        state.sitting = entity.isSitting();

        // Отображаемый предмет в руке (например, меч/топор из инвентаря Воина)
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
    public Identifier getTextureLocation(GolemRenderState state) {
        return textureFor(state.role);
    }

    public static Identifier textureFor(GolemRole role) {
        return switch (role) {
            case DIGGER -> DIGGER_TEX;
            case FARMER -> FARMER_TEX;
            case SORTER -> SORTER_TEX;
            case WARRIOR -> WARRIOR_TEX;
        };
    }
}
