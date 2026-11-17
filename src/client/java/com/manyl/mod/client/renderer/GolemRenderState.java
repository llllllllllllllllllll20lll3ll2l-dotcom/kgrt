package com.manyl.mod.client.renderer;

import com.manyl.mod.entity.GolemRole;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.AnimationState;

/**
 * Состояние рендера маленького голема для 1.21.11 (включая предмет в руке).
 */
public class GolemRenderState extends LivingEntityRenderState {
    public GolemRole role = GolemRole.DIGGER;
    public int hunger = 100;
    public boolean working;
    public boolean carrying;
    public boolean sitting;

    public final ItemStackRenderState heldItem = new ItemStackRenderState();

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState workAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    public AnimationState carryAnimationState = new AnimationState();
    public AnimationState sitAnimationState = new AnimationState();
    public AnimationState summonAnimationState = new AnimationState();
    public AnimationState eatAnimationState = new AnimationState();
}
