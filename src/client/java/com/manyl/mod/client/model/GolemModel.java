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
 * Ультра-HD 3D-модель 2-блочного Голема-Воина (135 скульптурных элементов, 16x геометрия, текстура 4096x4096).
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
        PartDefinition root = rootPart.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(788, 4).addBox(-32F, 0F, -32F, 64.0F, 72.0F, 64.0F)
                        .texOffs(2856, 416).addBox(-36F, -4F, -28F, 72.0F, 16.0F, 56.0F)
                        .texOffs(1660, 196).addBox(-44F, 4F, -26F, 16.0F, 58.0F, 52.0F)
                        .texOffs(3948, 314).addBox(-48F, 10F, -20F, 14.0F, 46.0F, 40.0F)
                        .texOffs(2068, 506).addBox(-50F, 18F, -10F, 12.0F, 26.0F, 20.0F)
                        .texOffs(1580, 506).addBox(-26F, 62F, -40F, 52.0F, 34.0F, 14.0F)
                        .texOffs(104, 570).addBox(-22F, 66F, -46F, 44.0F, 26.0F, 10.0F)
                        .texOffs(2720, 570).addBox(-8F, 73F, -50F, 16.0F, 12.0F, 6.0F)
                        .texOffs(736, 196).addBox(-30F, 88F, -30F, 60.0F, 52.0F, 60.0F)
                        .texOffs(1144, 506).addBox(-24F, 92F, -35F, 48.0F, 42.0F, 10.0F)
                        .texOffs(1852, 506).addBox(-20F, 96F, 24F, 40.0F, 34.0F, 12.0F)
                        .texOffs(116, 416).addBox(-28F, 128F, -36F, 56.0F, 12.0F, 72.0F)
                        .texOffs(3084, 4).addBox(-34F, 136F, -44F, 68.0F, 40.0F, 84.0F)
                        .texOffs(2916, 196).addBox(-38F, 162F, -48F, 76.0F, 14.0F, 92.0F)
                        .texOffs(988, 506).addBox(-28F, 144F, -60F, 56.0F, 32.0F, 20.0F)
                        .texOffs(920, 570).addBox(-22F, 138F, -50F, 44.0F, 14.0F, 16.0F)
                        .texOffs(624, 570).addBox(-24F, 148F, 38F, 48.0F, 24.0F, 10.0F)
                , PartPose.offset(-40F, -152F, 0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(1048, 4).addBox(-32F, 0F, -32F, 64.0F, 72.0F, 64.0F)
                        .texOffs(3116, 416).addBox(-36F, -4F, -28F, 72.0F, 16.0F, 56.0F)
                        .texOffs(1800, 196).addBox(28F, 4F, -26F, 16.0F, 58.0F, 52.0F)
                        .texOffs(4, 416).addBox(34F, 10F, -20F, 14.0F, 46.0F, 40.0F)
                        .texOffs(2136, 506).addBox(38F, 18F, -10F, 12.0F, 26.0F, 20.0F)
                        .texOffs(1716, 506).addBox(-26F, 62F, -40F, 52.0F, 34.0F, 14.0F)
                        .texOffs(216, 570).addBox(-22F, 66F, -46F, 44.0F, 26.0F, 10.0F)
                        .texOffs(2768, 570).addBox(-8F, 73F, -50F, 16.0F, 12.0F, 6.0F)
                        .texOffs(980, 196).addBox(-30F, 88F, -30F, 60.0F, 52.0F, 60.0F)
                        .texOffs(1264, 506).addBox(-24F, 92F, -35F, 48.0F, 42.0F, 10.0F)
                        .texOffs(1960, 506).addBox(-20F, 96F, 24F, 40.0F, 34.0F, 12.0F)
                        .texOffs(376, 416).addBox(-28F, 128F, -36F, 56.0F, 12.0F, 72.0F)
                        .texOffs(2776, 4).addBox(-34F, 136F, -44F, 68.0F, 40.0F, 84.0F)
                        .texOffs(3256, 196).addBox(-38F, 162F, -48F, 76.0F, 14.0F, 92.0F)
                        .texOffs(832, 506).addBox(-28F, 144F, -60F, 56.0F, 32.0F, 20.0F)
                        .texOffs(1044, 570).addBox(-22F, 138F, -50F, 44.0F, 14.0F, 16.0F)
                        .texOffs(744, 570).addBox(-24F, 148F, 38F, 48.0F, 24.0F, 10.0F)
                , PartPose.offset(40F, -152F, 0F));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(2084, 314).addBox(-36F, -196F, -32F, 72.0F, 24.0F, 64.0F)
                        .texOffs(1940, 196).addBox(-58F, -182F, -46F, 116.0F, 16.0F, 92.0F)
                        .texOffs(1756, 570).addBox(-48F, -172F, -50F, 96.0F, 12.0F, 14.0F)
                        .texOffs(4, 4).addBox(-68F, -168F, -46F, 136.0F, 96.0F, 92.0F)
                        .texOffs(3844, 416).addBox(-62F, -162F, -54F, 56.0F, 48.0F, 12.0F)
                        .texOffs(3704, 416).addBox(6F, -162F, -54F, 56.0F, 48.0F, 12.0F)
                        .texOffs(732, 314).addBox(-66F, -166F, 38F, 132.0F, 84.0F, 14.0F)
                        .texOffs(2556, 416).addBox(-34F, -148F, -58F, 68.0F, 64.0F, 12.0F)
                        .texOffs(2156, 570).addBox(-40F, -152F, -60F, 16.0F, 16.0F, 10.0F)
                        .texOffs(2324, 570).addBox(24F, -152F, -60F, 16.0F, 16.0F, 10.0F)
                        .texOffs(2268, 570).addBox(-40F, -100F, -60F, 16.0F, 16.0F, 10.0F)
                        .texOffs(2212, 570).addBox(24F, -100F, -60F, 16.0F, 16.0F, 10.0F)
                        .texOffs(332, 506).addBox(-24F, -138F, -62F, 48.0F, 44.0F, 10.0F)
                        .texOffs(1252, 570).addBox(-14F, -128F, -66F, 28.0F, 24.0F, 6.0F)
                        .texOffs(2404, 4).addBox(-54F, -72F, -38F, 108.0F, 48.0F, 76.0F)
                        .texOffs(1432, 314).addBox(-58F, -68F, -42F, 116.0F, 12.0F, 84.0F)
                        .texOffs(1028, 314).addBox(-58F, -50F, -42F, 116.0F, 12.0F, 84.0F)
                        .texOffs(1472, 4).addBox(-74F, -136F, -34F, 12.0F, 64.0F, 68.0F)
                        .texOffs(1308, 4).addBox(62F, -136F, -34F, 12.0F, 64.0F, 68.0F)
                        .texOffs(1224, 196).addBox(-64F, -30F, -44F, 128.0F, 22.0F, 88.0F)
                        .texOffs(3212, 506).addBox(-24F, -34F, -52F, 48.0F, 30.0F, 12.0F)
                        .texOffs(2656, 570).addBox(-12F, -26F, -55F, 24.0F, 14.0F, 6.0F)
                        .texOffs(3376, 416).addBox(-24F, -8F, -48F, 48.0F, 56.0F, 10.0F)
                        .texOffs(2380, 570).addBox(-28F, 38F, -50F, 56.0F, 12.0F, 12.0F)
                        .texOffs(88, 506).addBox(-24F, -8F, 38F, 48.0F, 48.0F, 10.0F)
                        .texOffs(176, 196).addBox(-68F, -12F, -36F, 12.0F, 42.0F, 72.0F)
                        .texOffs(4, 196).addBox(56F, -12F, -36F, 12.0F, 42.0F, 72.0F)
                , PartPose.offset(0F, -152F, 0F));

        body.addOrReplaceChild("backpack",
                CubeListBuilder.create()
                        .texOffs(2156, 4).addBox(-44F, -40F, -10F, 88.0F, 92.0F, 34.0F)
                        .texOffs(3012, 506).addBox(-36F, -48F, -4F, 72.0F, 16.0F, 26.0F)
                        .texOffs(3336, 506).addBox(-36F, 46F, -4F, 72.0F, 14.0F, 26.0F)
                        .texOffs(2720, 416).addBox(-28F, -28F, 20F, 56.0F, 66.0F, 10.0F)
                        .texOffs(668, 506).addBox(-18F, -18F, 26F, 36.0F, 46.0F, 8.0F)
                        .texOffs(864, 570).addBox(-10F, -8F, 30F, 20.0F, 26.0F, 6.0F)
                        .texOffs(2808, 196).addBox(-58F, -36F, -6F, 24.0F, 80.0F, 28.0F)
                        .texOffs(2888, 506).addBox(-60F, -42F, -8F, 28.0F, 12.0F, 32.0F)
                        .texOffs(2764, 506).addBox(-60F, 36F, -8F, 28.0F, 12.0F, 32.0F)
                        .texOffs(560, 506).addBox(-56F, -10F, -8F, 20.0F, 22.0F, 32.0F)
                        .texOffs(3628, 506).addBox(-40F, -62F, 0F, 22.0F, 18.0F, 22.0F)
                        .texOffs(2588, 570).addBox(-37F, -68F, 3F, 16.0F, 8.0F, 16.0F)
                        .texOffs(2700, 196).addBox(34F, -36F, -6F, 24.0F, 80.0F, 28.0F)
                        .texOffs(2640, 506).addBox(32F, -42F, -8F, 28.0F, 12.0F, 32.0F)
                        .texOffs(2516, 506).addBox(32F, 36F, -8F, 28.0F, 12.0F, 32.0F)
                        .texOffs(452, 506).addBox(36F, -10F, -8F, 20.0F, 22.0F, 32.0F)
                        .texOffs(3536, 506).addBox(18F, -62F, 0F, 22.0F, 18.0F, 22.0F)
                        .texOffs(2520, 570).addBox(21F, -68F, 3F, 16.0F, 8.0F, 16.0F)
                , PartPose.offset(0F, -128F, 52F));

        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(464, 4).addBox(-40F, -68F, -40F, 80.0F, 68.0F, 80.0F)
                        .texOffs(2816, 570).addBox(-28F, -50F, -42F, 18.0F, 10.0F, 6.0F)
                        .texOffs(2868, 570).addBox(10F, -50F, -42F, 18.0F, 10.0F, 6.0F)
                        .texOffs(2496, 314).addBox(-46F, -42F, -44F, 18.0F, 40.0F, 48.0F)
                        .texOffs(2360, 314).addBox(28F, -42F, -44F, 18.0F, 40.0F, 48.0F)
                        .texOffs(464, 570).addBox(-32F, -18F, -46F, 64.0F, 20.0F, 14.0F)
                        .texOffs(1168, 570).addBox(-16F, -22F, -48F, 32.0F, 22.0F, 8.0F)
                        .texOffs(1384, 506).addBox(-50F, -54F, -14F, 8.0F, 26.0F, 26.0F)
                        .texOffs(1456, 506).addBox(42F, -54F, -14F, 8.0F, 26.0F, 26.0F)
                        .texOffs(1528, 506).addBox(-7F, -64F, -48F, 14.0F, 42.0F, 10.0F)
                        .texOffs(348, 196).addBox(-48F, -74F, -48F, 96.0F, 16.0F, 96.0F)
                        .texOffs(2360, 196).addBox(-42F, -94F, -42F, 84.0F, 24.0F, 84.0F)
                        .texOffs(636, 416).addBox(-44F, -92F, 36F, 88.0F, 72.0F, 12.0F)
                        .texOffs(2632, 314).addBox(-36F, -104F, -36F, 72.0F, 14.0F, 72.0F)
                        .texOffs(2376, 416).addBox(-12F, -114F, -30F, 24.0F, 12.0F, 64.0F)
                        .texOffs(1668, 570).addBox(-14F, -118F, 26F, 28.0F, 14.0F, 14.0F)
                        .texOffs(840, 416).addBox(-8F, -138F, -28F, 16.0F, 26.0F, 58.0F)
                        .texOffs(208, 506).addBox(-7F, -144F, -22F, 14.0F, 10.0F, 46.0F)
                        .texOffs(760, 506).addBox(-8F, -132F, 26F, 16.0F, 36.0F, 18.0F)
                        .texOffs(3720, 506).addBox(-7F, -110F, 36F, 14.0F, 28.0F, 12.0F)
                , PartPose.offset(0F, -192F, 0F));

        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(2112, 416).addBox(-12F, -16F, -24F, 16.0F, 32.0F, 48.0F)
                        .texOffs(3392, 4).addBox(-60F, -32F, -40F, 88.0F, 42.0F, 80.0F)
                        .texOffs(368, 314).addBox(-64F, -8F, -42F, 96.0F, 14.0F, 84.0F)
                        .texOffs(1836, 416).addBox(-52F, -44F, -32F, 72.0F, 16.0F, 64.0F)
                        .texOffs(2360, 506).addBox(-36F, -48F, -18F, 40.0F, 8.0F, 36.0F)
                        .texOffs(1980, 570).addBox(-32F, -26F, -44F, 32.0F, 16.0F, 10.0F)
                        .texOffs(3596, 196).addBox(-44F, 8F, -28F, 56.0F, 48.0F, 56.0F)
                        .texOffs(3192, 314).addBox(-52F, 48F, -32F, 68.0F, 22.0F, 64.0F)
                        .texOffs(3496, 416).addBox(-58F, 46F, -18F, 14.0F, 26.0F, 36.0F)
                        .texOffs(1896, 4).addBox(-48F, 66F, -32F, 64.0F, 62.0F, 64.0F)
                        .texOffs(1836, 314).addBox(-58F, 72F, -22F, 16.0F, 48.0F, 44.0F)
                        .texOffs(3984, 416).addBox(-62F, 80F, -14F, 12.0F, 32.0F, 28.0F)
                        .texOffs(992, 416).addBox(-52F, 122F, -34F, 72.0F, 14.0F, 68.0F)
                        .texOffs(3704, 314).addBox(-46F, 134F, -30F, 60.0F, 26.0F, 60.0F)
                        .texOffs(1496, 570).addBox(-50F, 140F, -34F, 68.0F, 12.0F, 16.0F)
                        .texOffs(4, 570).addBox(-44F, 158F, -28F, 24.0F, 14.0F, 24.0F)
                        .texOffs(3976, 506).addBox(-16F, 158F, -28F, 24.0F, 14.0F, 24.0F)
                        .texOffs(396, 570).addBox(2F, 144F, -28F, 16.0F, 20.0F, 16.0F)
                , PartPose.offset(-84F, -168F, 0F));

        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(2244, 416).addBox(-4F, -16F, -24F, 16.0F, 32.0F, 48.0F)
                        .texOffs(3732, 4).addBox(-28F, -32F, -40F, 88.0F, 42.0F, 80.0F)
                        .texOffs(4, 314).addBox(-32F, -8F, -42F, 96.0F, 14.0F, 84.0F)
                        .texOffs(1560, 416).addBox(-20F, -44F, -32F, 72.0F, 16.0F, 64.0F)
                        .texOffs(2204, 506).addBox(-4F, -48F, -18F, 40.0F, 8.0F, 36.0F)
                        .texOffs(2068, 570).addBox(0F, -26F, -44F, 32.0F, 16.0F, 10.0F)
                        .texOffs(3824, 196).addBox(-12F, 8F, -28F, 56.0F, 48.0F, 56.0F)
                        .texOffs(2924, 314).addBox(-20F, 48F, -32F, 68.0F, 22.0F, 64.0F)
                        .texOffs(3600, 416).addBox(40F, 46F, -18F, 14.0F, 26.0F, 36.0F)
                        .texOffs(1636, 4).addBox(-16F, 66F, -32F, 64.0F, 62.0F, 64.0F)
                        .texOffs(1960, 314).addBox(42F, 72F, -22F, 16.0F, 48.0F, 44.0F)
                        .texOffs(4, 506).addBox(48F, 80F, -14F, 12.0F, 32.0F, 28.0F)
                        .texOffs(1276, 416).addBox(-20F, 122F, -34F, 72.0F, 14.0F, 68.0F)
                        .texOffs(3460, 314).addBox(-14F, 134F, -30F, 60.0F, 26.0F, 60.0F)
                        .texOffs(1324, 570).addBox(-18F, 140F, -34F, 68.0F, 12.0F, 16.0F)
                        .texOffs(3876, 506).addBox(-12F, 158F, -28F, 24.0F, 14.0F, 24.0F)
                        .texOffs(3776, 506).addBox(16F, 158F, -28F, 24.0F, 14.0F, 24.0F)
                        .texOffs(328, 570).addBox(-24F, 144F, -28F, 16.0F, 20.0F, 16.0F)
                , PartPose.offset(84F, -168F, 0F));

        return LayerDefinition.create(mesh, 4096, 4096);
    }

    @Override
    public void setupAnim(GolemRenderState state) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

        this.idleAnim.apply(state.idleAnimationState, state.ageInTicks, 1.0F);
        this.walkAnim.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 2.2F, 2.4F);
        this.workAnim.apply(state.workAnimationState, state.ageInTicks, 1.0F);
        this.attackAnim.apply(state.attackAnimationState, state.ageInTicks, 1.0F);
        this.carryAnim.apply(state.carryAnimationState, state.ageInTicks, 1.0F);
        this.sitAnim.apply(state.sitAnimationState, state.ageInTicks, 1.0F);
        this.summonAnim.apply(state.summonAnimationState, state.ageInTicks, 1.0F);
        this.eatAnim.apply(state.eatAnimationState, state.ageInTicks, 1.0F);
    }
}
