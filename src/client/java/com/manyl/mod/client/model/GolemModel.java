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
 * Новая 3D-модель голема-воина (высота ~2.0 блока / 32+ пикселя),
 * с кованой бронёй, нагрудной пластиной, рунным заплечным щитом, наплечниками и шлемом с гребнем.
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

        // Ноги (4x11x4) с опорой на y=24 (от y=13 до y=24)
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F),
                PartPose.offset(-2.5F, 13.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F),
                PartPose.offset(2.5F, 13.0F, 0.0F));

        // Торс (10x12x6, от y=1 до y=13) + рельефная нагрудная пластина (8x7x1)
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-5.0F, -12.0F, -3.0F, 10.0F, 12.0F, 6.0F)
                        .texOffs(0, 49).addBox(-4.0F, -11.0F, -3.6F, 8.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));

        // Рунный заплечный щит/ранец (6x8x2)
        body.addOrReplaceChild("backpack",
                CubeListBuilder.create().texOffs(18, 49).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 8.0F, 2.0F),
                PartPose.offset(0.0F, -7.0F, 3.0F));

        // Голова (8x8x8, от y=-7 до y=1) + шлем (8x3x8 + 0.45) + гребень (2x2x6) -> рост ровно ~2.0 блока
        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-4.0F, -8.35F, -4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.45F))
                        .texOffs(32, 11).addBox(-1.0F, -10.4F, -2.5F, 2.0F, 2.0F, 6.0F),
                PartPose.offset(0.0F, -12.0F, 0.0F));

        // Правая рука (4x11x4) + наплечник (5x4x5)
        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(32, 19).addBox(-4.0F, -1.0F, -2.0F, 4.0F, 11.0F, 4.0F)
                        .texOffs(32, 34).addBox(-4.5F, -1.8F, -2.5F, 5.0F, 4.0F, 5.0F),
                PartPose.offset(-5.0F, -10.5F, 0.0F));

        // Левая рука (4x11x4) + наплечник (5x4x5)
        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(48, 19).addBox(0.0F, -1.0F, -2.0F, 4.0F, 11.0F, 4.0F)
                        .texOffs(32, 43).addBox(-0.5F, -1.8F, -2.5F, 5.0F, 4.0F, 5.0F),
                PartPose.offset(5.0F, -10.5F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
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
