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
 * Модель маленького голема (~0.8 блока высотой) для 1.21.11.
 */
public class GolemModel extends EntityModel<GolemRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ManylMod.id("golem"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    private final KeyframeAnimation idleAnim;
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
        this.leftArm = this.body.getChild("left_arm");
        this.rightLeg = this.root.getChild("right_leg");
        this.leftLeg = this.root.getChild("left_leg");

        this.idleAnim = GolemAnimations.IDLE.bake(root);
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
        PartDefinition meshRoot = mesh.getRoot();

        PartDefinition root = meshRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 14)
                        .addBox(-4.0F, -5.0F, -2.5F, 8.0F, 6.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.5F, -6.0F, -3.5F, 7.0F, 6.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -5.0F, 0.0F));

        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(26, 14)
                        .addBox(-3.0F, -0.5F, -1.5F, 3.0F, 6.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(-4.0F, -4.5F, 0.0F));

        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(26, 14).mirror()
                        .addBox(0.0F, -0.5F, -1.5F, 3.0F, 6.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(4.0F, -4.5F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 26)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(-2.0F, 21.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 26).mirror()
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(2.0F, 21.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(GolemRenderState state) {
        super.setupAnim(state);

        this.head.yRot = state.yRot * ((float) Math.PI / 180.0F);
        this.head.xRot = state.xRot * ((float) Math.PI / 180.0F);

        float limbAngle = state.walkAnimationPos;
        float limbSpeed = state.walkAnimationSpeed;
        this.rightLeg.xRot = Mth.cos(limbAngle * 0.9F) * 1.1F * limbSpeed;
        this.leftLeg.xRot = Mth.cos(limbAngle * 0.9F + (float) Math.PI) * 1.1F * limbSpeed;
        this.rightArm.xRot = Mth.cos(limbAngle * 0.9F + (float) Math.PI) * 0.75F * limbSpeed;
        this.leftArm.xRot = Mth.cos(limbAngle * 0.9F) * 0.75F * limbSpeed;

        this.idleAnim.apply(state.idleAnimationState, state.ageInTicks, 1.0F);
        this.workAnim.apply(state.workAnimationState, state.ageInTicks, 1.0F);
        this.attackAnim.apply(state.attackAnimationState, state.ageInTicks, 1.0F);
        this.carryAnim.apply(state.carryAnimationState, state.ageInTicks, 1.0F);
        this.sitAnim.apply(state.sitAnimationState, state.ageInTicks, 1.0F);
        this.summonAnim.apply(state.summonAnimationState, state.ageInTicks, 1.0F);
        this.eatAnim.apply(state.eatAnimationState, state.ageInTicks, 1.0F);
    }
}
