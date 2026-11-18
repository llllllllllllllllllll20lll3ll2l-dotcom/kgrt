package com.manyl.mod.client.model;

import com.manyl.mod.ManylMod;
import com.manyl.mod.client.renderer.GolemRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Высокодетализированная 3D-модель 2-блочного Голема-Воина (41 скульптурный элемент, 4x разрешение геометрии, текстура 1024x1024).
 */
public class GolemModel extends EntityModel<GolemRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ManylMod.id("golem"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;

    private final KeyframeAnimation idleAnim;
    private final KeyframeAnimation walkAnim;
    private final KeyframeAnimation workAnim;
    private final KeyframeAnimation attackAnim;
    private final KeyframeAnimation carryAnim;
    private final KeyframeAnimation sitAnim;
    private final KeyframeAnimation summonAnim;
    private final KeyframeAnimation eatAnim;

    public GolemModel(ModelPart root) {
        super(root);
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.rightArm = this.body.getChild("right_arm");

        this.idleAnim = GolemAnimations.IDLE.bake(root);
        this.walkAnim = GolemAnimations.WALK.bake(root);
        this.workAnim = GolemAnimations.WORK.bake(root);
        this.attackAnim = GolemAnimations.ATTACK.bake(root);
        this.carryAnim = GolemAnimations.CARRY.bake(root);
        this.sitAnim = GolemAnimations.SIT.bake(root);
        this.summonAnim = GolemAnimations.SUMMON.bake(root);
        this.eatAnim = GolemAnimations.EAT.bake(root);
    }

    public void translateToRightHand(PoseStack matrices) {
        this.root.translateAndRotate(matrices);
        this.body.translateAndRotate(matrices);
        this.rightArm.translateAndRotate(matrices);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        PartDefinition root = rootPart.addOrReplaceChild("root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Правая нога (бедро, тассет, голень, наколенник, сапог, носок)
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(2, 2).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 18.0F, 16.0F)
                        .texOffs(68, 2).addBox(-10.5F, 1.0F, -6.0F, 3.0F, 14.0F, 12.0F)
                        .texOffs(100, 2).addBox(-8.5F, 18.0F, -8.5F, 17.0F, 17.0F, 17.0F)
                        .texOffs(170, 2).addBox(-6.0F, 14.5F, -11.5F, 12.0F, 10.0F, 4.0F)
                        .texOffs(204, 2).addBox(-9.0F, 35.0F, -11.5F, 18.0F, 9.0F, 21.0F)
                        .texOffs(284, 2).addBox(-7.0F, 39.0F, -15.0F, 14.0F, 5.0F, 5.0F),
                PartPose.offset(-10.0F, -20.0F, 0.0F));

        // Левая нога
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(324, 2).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 18.0F, 16.0F)
                        .texOffs(390, 2).addBox(7.5F, 1.0F, -6.0F, 3.0F, 14.0F, 12.0F)
                        .texOffs(422, 2).addBox(-8.5F, 18.0F, -8.5F, 17.0F, 17.0F, 17.0F)
                        .texOffs(492, 2).addBox(-6.0F, 14.5F, -11.5F, 12.0F, 10.0F, 4.0F)
                        .texOffs(526, 2).addBox(-9.0F, 35.0F, -11.5F, 18.0F, 9.0F, 21.0F)
                        .texOffs(606, 2).addBox(-7.0F, 39.0F, -15.0F, 14.0F, 5.0F, 5.0F),
                PartPose.offset(10.0F, -20.0F, 0.0F));

        // Торс (талия, грудь, кираса, рама ядра, рунное ядро, пояс, пряжка, табарда, горжет)
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(646, 2).addBox(-18.0F, -20.0F, -11.0F, 36.0F, 20.0F, 22.0F)
                        .texOffs(764, 2).addBox(-21.0F, -48.0F, -13.0F, 42.0F, 28.0F, 26.0F)
                        .texOffs(902, 2).addBox(-16.0F, -45.0F, -16.5F, 32.0F, 24.0F, 4.0F)
                        .texOffs(976, 2).addBox(-7.5F, -42.5F, -19.0F, 15.0F, 19.0F, 3.0F)
                        .texOffs(2, 58).addBox(-4.5F, -39.0F, -21.0F, 9.0F, 12.0F, 3.0F)
                        .texOffs(28, 58).addBox(-19.0F, -7.0F, -12.0F, 38.0F, 7.0F, 24.0F)
                        .texOffs(154, 58).addBox(-5.5F, -8.0F, -14.5F, 11.0F, 9.0F, 3.0F)
                        .texOffs(184, 58).addBox(-7.0F, 0.0F, -13.0F, 14.0F, 18.0F, 2.0F)
                        .texOffs(218, 58).addBox(-14.0F, -52.0F, -12.0F, 28.0F, 6.0F, 24.0F),
                PartPose.offset(0.0F, -20.0F, 0.0F));

        // Заплечный рунный генератор / щит
        body.addOrReplaceChild("backpack",
                CubeListBuilder.create()
                        .texOffs(324, 58).addBox(-13.0F, -17.0F, 0.0F, 26.0F, 34.0F, 8.0F)
                        .texOffs(394, 58).addBox(-15.5F, -13.0F, 5.0F, 5.0F, 26.0F, 5.0F)
                        .texOffs(416, 58).addBox(10.5F, -13.0F, 5.0F, 5.0F, 26.0F, 5.0F)
                        .texOffs(438, 58).addBox(-5.0F, -9.0F, 7.5F, 10.0F, 18.0F, 3.0F),
                PartPose.offset(0.0F, -32.0F, 13.0F));

        // Голова и многослойный шлем с нащёчниками, наносником и плюмажем
        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(466, 58).addBox(-15.0F, -30.0F, -15.0F, 30.0F, 30.0F, 30.0F)
                        .texOffs(588, 58).addBox(-17.0F, -34.0F, -17.0F, 34.0F, 13.0F, 34.0F)
                        .texOffs(726, 58).addBox(-18.0F, -24.0F, -19.0F, 36.0F, 7.0F, 36.0F)
                        .texOffs(872, 58).addBox(-18.0F, -18.0F, -16.0F, 5.0F, 16.0F, 20.0F)
                        .texOffs(924, 58).addBox(13.0F, -18.0F, -16.0F, 5.0F, 16.0F, 20.0F)
                        .texOffs(976, 58).addBox(-2.5F, -20.0F, -19.5F, 5.0F, 15.0F, 4.0F)
                        .texOffs(2, 120).addBox(-4.0F, -38.0F, -13.0F, 8.0F, 5.0F, 28.0F)
                        .texOffs(76, 120).addBox(-2.5F, -46.0F, -11.0F, 5.0F, 9.0F, 26.0F)
                        .texOffs(140, 120).addBox(-2.0F, -40.0F, 14.0F, 4.0F, 8.0F, 7.0F),
                PartPose.offset(0.0F, -48.0F, 0.0F));

        // Правая рука (плечо, двойной наплечник, наруч, налокотник, боевая перчатка)
        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(164, 120).addBox(-15.0F, -2.0F, -7.0F, 14.0F, 16.0F, 14.0F)
                        .texOffs(222, 120).addBox(-18.0F, -7.0F, -10.0F, 20.0F, 10.0F, 20.0F)
                        .texOffs(304, 120).addBox(-16.0F, -12.0F, -8.0F, 16.0F, 7.0F, 16.0F)
                        .texOffs(370, 120).addBox(-16.0F, 14.0F, -8.0F, 16.0F, 20.0F, 16.0F)
                        .texOffs(436, 120).addBox(-18.5F, 16.0F, -5.0F, 4.0F, 14.0F, 10.0F)
                        .texOffs(466, 120).addBox(-16.5F, 34.0F, -8.5F, 17.0F, 10.0F, 17.0F),
                PartPose.offset(-21.0F, -42.0F, 0.0F));

        // Левая рука
        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(536, 120).addBox(1.0F, -2.0F, -7.0F, 14.0F, 16.0F, 14.0F)
                        .texOffs(594, 120).addBox(-2.0F, -7.0F, -10.0F, 20.0F, 10.0F, 20.0F)
                        .texOffs(676, 120).addBox(0.0F, -12.0F, -8.0F, 16.0F, 7.0F, 16.0F)
                        .texOffs(742, 120).addBox(0.0F, 14.0F, -8.0F, 16.0F, 20.0F, 16.0F)
                        .texOffs(808, 120).addBox(14.5F, 16.0F, -5.0F, 4.0F, 14.0F, 10.0F)
                        .texOffs(838, 120).addBox(-0.5F, 34.0F, -8.5F, 17.0F, 10.0F, 17.0F),
                PartPose.offset(21.0F, -42.0F, 0.0F));

        return LayerDefinition.create(mesh, 1024, 1024);
    }

    @Override
    public void setupAnim(GolemRenderState state) {
        super.setupAnim(state);

        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

        this.idleAnim.apply(state.idleAnimationState, state.ageInTicks);
        this.walkAnim.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 2.0F, 2.2F);

        this.sitAnim.apply(state.sitAnimationState, state.ageInTicks);
        this.carryAnim.apply(state.carryAnimationState, state.ageInTicks);
        this.workAnim.apply(state.workAnimationState, state.ageInTicks);
        this.attackAnim.apply(state.attackAnimationState, state.ageInTicks);
        this.eatAnim.apply(state.eatAnimationState, state.ageInTicks);
        this.summonAnim.apply(state.summonAnimationState, state.ageInTicks);
    }
}
