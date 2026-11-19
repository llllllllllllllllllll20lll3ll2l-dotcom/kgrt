package com.manyl.mod.client.renderer;

import com.manyl.mod.entity.GolemRole;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.AnimationState;

/**
 * Состояние рендера стража-голема.
 */
public class GolemRenderState extends LivingEntityRenderState {
    public GolemRole role = GolemRole.WARRIOR;
    public int hunger = 100;
    public boolean isSitting = false;
    public boolean isCarrying = false;
    public boolean isWorking = false;
    public boolean isDisabled = false;
    public boolean isDaydreaming = false;
    public int daydreamTicks = 0;
    public int bathPhase = 0;
    public int bathTicks = 0;
    public int interactionAnim = 0;
    public float interactionTicks = 0.0F;
    public boolean isVowBound = false;

    public boolean hasHeadArmor = false;
    public boolean hasChestArmor = false;
    public boolean hasLegsArmor = false;
    public boolean hasFeetArmor = false;

    public final ItemStackRenderState heldItem = new ItemStackRenderState();
    public final ItemStackRenderState offhandItem = new ItemStackRenderState();

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState workAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState carryAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState summonAnimationState = new AnimationState();
    public final AnimationState eatAnimationState = new AnimationState();
}
