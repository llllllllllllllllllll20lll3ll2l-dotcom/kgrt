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
 * Ультра-HD 3D-модель 2-блочного Голема-Воительницы (138 скульптурных элементов, 16x геометрия, текстура 4096x4096).
 */
public class ValkyrieModel extends EntityModel<GolemRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ManylMod.id("valkyrie_golem"), "main");

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

    public ValkyrieModel(ModelPart root) {
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
                        .texOffs(3052, 4).addBox(-26F, 0F, -26F, 52.0F, 42.0F, 52.0F)
                        .texOffs(2464, 142).addBox(-23F, 40F, -23F, 46.0F, 36.0F, 46.0F)
                        .texOffs(216, 314).addBox(-28F, -2F, -24F, 56.0F, 12.0F, 48.0F)
                        .texOffs(3592, 314).addBox(-20F, 66F, -32F, 40.0F, 26.0F, 12.0F)
                        .texOffs(1768, 378).addBox(-15F, 70F, -36F, 30.0F, 18.0F, 8.0F)
                        .texOffs(3058, 378).addBox(-6F, 74F, -39F, 12.0F, 10.0F, 5.0F)
                        .texOffs(1980, 238).addBox(-25F, 82F, -25F, 50.0F, 14.0F, 50.0F)
                        .texOffs(2360, 4).addBox(-23F, 94F, -23F, 46.0F, 50.0F, 46.0F)
                        .texOffs(1408, 314).addBox(-18F, 98F, -28F, 36.0F, 42.0F, 10.0F)
                        .texOffs(2752, 314).addBox(-16F, 100F, 18F, 32.0F, 36.0F, 10.0F)
                        .texOffs(1572, 238).addBox(-22F, 138F, -28F, 44.0F, 10.0F, 56.0F)
                        .texOffs(1932, 4).addBox(-24F, 146F, -38F, 48.0F, 30.0F, 70.0F)
                        .texOffs(2096, 142).addBox(-26F, 174F, -44F, 52.0F, 10.0F, 74.0F)
                        .texOffs(2964, 314).addBox(-20F, 154F, -54F, 40.0F, 24.0F, 20.0F)
                        .texOffs(960, 378).addBox(-16F, 148F, -42F, 32.0F, 12.0F, 16.0F)
                        .texOffs(1936, 314).addBox(-18F, 152F, 24F, 36.0F, 32.0F, 16.0F)
                , PartPose.offset(-28F, -160F, 0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(2840, 4).addBox(-26F, 0F, -26F, 52.0F, 42.0F, 52.0F)
                        .texOffs(2652, 142).addBox(-23F, 40F, -23F, 46.0F, 36.0F, 46.0F)
                        .texOffs(4, 314).addBox(-28F, -2F, -24F, 56.0F, 12.0F, 48.0F)
                        .texOffs(3700, 314).addBox(-20F, 66F, -32F, 40.0F, 26.0F, 12.0F)
                        .texOffs(1848, 378).addBox(-15F, 70F, -36F, 30.0F, 18.0F, 8.0F)
                        .texOffs(3020, 378).addBox(-6F, 74F, -39F, 12.0F, 10.0F, 5.0F)
                        .texOffs(1776, 238).addBox(-25F, 82F, -25F, 50.0F, 14.0F, 50.0F)
                        .texOffs(2172, 4).addBox(-23F, 94F, -23F, 46.0F, 50.0F, 46.0F)
                        .texOffs(1504, 314).addBox(-18F, 98F, -28F, 36.0F, 42.0F, 10.0F)
                        .texOffs(2664, 314).addBox(-16F, 100F, 18F, 32.0F, 36.0F, 10.0F)
                        .texOffs(1368, 238).addBox(-22F, 138F, -28F, 44.0F, 10.0F, 56.0F)
                        .texOffs(1692, 4).addBox(-24F, 146F, -38F, 48.0F, 30.0F, 70.0F)
                        .texOffs(1840, 142).addBox(-26F, 174F, -44F, 52.0F, 10.0F, 74.0F)
                        .texOffs(2840, 314).addBox(-20F, 154F, -54F, 40.0F, 24.0F, 20.0F)
                        .texOffs(1060, 378).addBox(-16F, 148F, -42F, 32.0F, 12.0F, 16.0F)
                        .texOffs(2044, 314).addBox(-18F, 152F, 24F, 36.0F, 32.0F, 16.0F)
                , PartPose.offset(28F, -160F, 0F));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(716, 238).addBox(-24F, -194F, -22F, 48.0F, 26.0F, 44.0F)
                        .texOffs(1508, 142).addBox(-46F, -176F, -36F, 92.0F, 14.0F, 72.0F)
                        .texOffs(280, 4).addBox(-54F, -166F, -34F, 108.0F, 52.0F, 68.0F)
                        .texOffs(3600, 142).addBox(-38F, -114F, -26F, 76.0F, 20.0F, 52.0F)
                        .texOffs(3388, 238).addBox(-48F, -158F, -48F, 42.0F, 44.0F, 18.0F)
                        .texOffs(3512, 238).addBox(6F, -158F, -48F, 42.0F, 44.0F, 18.0F)
                        .texOffs(3504, 314).addBox(-44F, -152F, -52F, 34.0F, 32.0F, 8.0F)
                        .texOffs(3416, 314).addBox(10F, -152F, -52F, 34.0F, 32.0F, 8.0F)
                        .texOffs(940, 314).addBox(-20F, -148F, -54F, 40.0F, 42.0F, 12.0F)
                        .texOffs(3846, 314).addBox(-12F, -140F, -58F, 24.0F, 26.0F, 8.0F)
                        .texOffs(3264, 4).addBox(-25F, -94F, -20F, 50.0F, 54.0F, 40.0F)
                        .texOffs(1208, 314).addBox(-27F, -86F, -22F, 54.0F, 8.0F, 44.0F)
                        .texOffs(1600, 314).addBox(-26F, -66F, -21F, 52.0F, 8.0F, 42.0F)
                        .texOffs(3812, 238).addBox(-30F, -44F, -23F, 60.0F, 14.0F, 46.0F)
                        .texOffs(3970, 314).addBox(-17F, -46F, -31F, 34.0F, 22.0F, 10.0F)
                        .texOffs(2496, 378).addBox(-8F, -40F, -35F, 16.0F, 12.0F, 6.0F)
                        .texOffs(424, 238).addBox(-44F, -30F, -28F, 88.0F, 14.0F, 56.0F)
                        .texOffs(3448, 4).addBox(-54F, -16F, -34F, 108.0F, 24.0F, 68.0F)
                        .texOffs(1404, 142).addBox(-20F, -22F, -40F, 40.0F, 78.0F, 10.0F)
                        .texOffs(2060, 378).addBox(-24F, 50F, -42F, 48.0F, 10.0F, 12.0F)
                        .texOffs(2352, 142).addBox(-22F, -22F, 32F, 44.0F, 74.0F, 10.0F)
                        .texOffs(1928, 378).addBox(-26F, 46F, 30F, 52.0F, 10.0F, 12.0F)
                        .texOffs(996, 4).addBox(-60F, -18F, -34F, 12.0F, 42.0F, 68.0F)
                        .texOffs(636, 4).addBox(-68F, 14F, -38F, 12.0F, 34.0F, 76.0F)
                        .texOffs(1216, 142).addBox(-70F, 42F, -40F, 12.0F, 10.0F, 80.0F)
                        .texOffs(2152, 314).addBox(-72F, 6F, -12F, 8.0F, 24.0F, 24.0F)
                        .texOffs(1160, 4).addBox(48F, -18F, -34F, 12.0F, 42.0F, 68.0F)
                        .texOffs(816, 4).addBox(56F, 14F, -38F, 12.0F, 34.0F, 76.0F)
                        .texOffs(1028, 142).addBox(58F, 42F, -40F, 12.0F, 10.0F, 80.0F)
                        .texOffs(2220, 314).addBox(62F, 6F, -12F, 8.0F, 24.0F, 24.0F)
                , PartPose.offset(0F, -160F, 0F));

        body.addOrReplaceChild("backpack",
                CubeListBuilder.create()
                        .texOffs(252, 142).addBox(-28F, -32F, -8F, 56.0F, 68.0F, 24.0F)
                        .texOffs(844, 314).addBox(-18F, -22F, 14F, 36.0F, 48.0F, 10.0F)
                        .texOffs(3914, 314).addBox(-10F, -12F, 20F, 20.0F, 28.0F, 6.0F)
                        .texOffs(2572, 314).addBox(-52F, -34F, 6F, 30.0F, 32.0F, 14.0F)
                        .texOffs(2384, 314).addBox(-76F, -58F, 10F, 34.0F, 34.0F, 12.0F)
                        .texOffs(3192, 314).addBox(-102F, -82F, 14F, 38.0F, 32.0F, 12.0F)
                        .texOffs(100, 378).addBox(-124F, -96F, 16F, 36.0F, 20.0F, 10.0F)
                        .texOffs(2404, 378).addBox(-130F, -90F, 19F, 38.0F, 12.0F, 6.0F)
                        .texOffs(632, 378).addBox(-102F, -52F, 14F, 44.0F, 18.0F, 10.0F)
                        .texOffs(2544, 378).addBox(-106F, -48F, 17F, 44.0F, 10.0F, 6.0F)
                        .texOffs(1396, 378).addBox(-84F, -24F, 12F, 40.0F, 16.0F, 10.0F)
                        .texOffs(2752, 378).addBox(-84F, -20F, 15F, 36.0F, 9.0F, 6.0F)
                        .texOffs(1680, 378).addBox(-62F, -4F, 8F, 32.0F, 16.0F, 10.0F)
                        .texOffs(2480, 314).addBox(22F, -34F, 6F, 30.0F, 32.0F, 14.0F)
                        .texOffs(2288, 314).addBox(42F, -58F, 10F, 34.0F, 34.0F, 12.0F)
                        .texOffs(3088, 314).addBox(64F, -82F, 14F, 38.0F, 32.0F, 12.0F)
                        .texOffs(196, 378).addBox(88F, -96F, 16F, 36.0F, 20.0F, 10.0F)
                        .texOffs(2312, 378).addBox(92F, -90F, 19F, 38.0F, 12.0F, 6.0F)
                        .texOffs(744, 378).addBox(58F, -52F, 14F, 44.0F, 18.0F, 10.0F)
                        .texOffs(2648, 378).addBox(62F, -48F, 17F, 44.0F, 10.0F, 6.0F)
                        .texOffs(1292, 378).addBox(44F, -24F, 12F, 40.0F, 16.0F, 10.0F)
                        .texOffs(2840, 378).addBox(48F, -20F, 15F, 36.0F, 9.0F, 6.0F)
                        .texOffs(1592, 378).addBox(30F, -4F, 8F, 32.0F, 16.0F, 10.0F)
                , PartPose.offset(0F, -124F, 36F));

        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(4, 4).addBox(-34F, -66F, -34F, 68.0F, 66.0F, 68.0F)
                        .texOffs(2928, 378).addBox(-24F, -44F, -36F, 16.0F, 10.0F, 5.0F)
                        .texOffs(2974, 378).addBox(8F, -44F, -36F, 16.0F, 10.0F, 5.0F)
                        .texOffs(3808, 314).addBox(-5F, -58F, -39F, 10.0F, 30.0F, 7.0F)
                        .texOffs(2948, 142).addBox(-40F, -40F, -36F, 12.0F, 36.0F, 40.0F)
                        .texOffs(2840, 142).addBox(28F, -40F, -36F, 12.0F, 36.0F, 40.0F)
                        .texOffs(1160, 378).addBox(-26F, -14F, -38F, 52.0F, 14.0F, 12.0F)
                        .texOffs(720, 142).addBox(-38F, -76F, -38F, 76.0F, 14.0F, 76.0F)
                        .texOffs(580, 378).addBox(-8F, -86F, -42F, 16.0F, 22.0F, 8.0F)
                        .texOffs(2548, 4).addBox(-36F, -94F, -36F, 72.0F, 22.0F, 72.0F)
                        .texOffs(4, 238).addBox(-30F, -104F, -30F, 60.0F, 12.0F, 60.0F)
                        .texOffs(572, 314).addBox(-50F, -72F, -14F, 14.0F, 28.0F, 32.0F)
                        .texOffs(3636, 238).addBox(-58F, -98F, -4F, 14.0F, 34.0F, 28.0F)
                        .texOffs(1792, 314).addBox(-60F, -120F, 8F, 12.0F, 28.0F, 22.0F)
                        .texOffs(4, 378).addBox(-58F, -112F, 12F, 8.0F, 18.0F, 14.0F)
                        .texOffs(668, 314).addBox(36F, -72F, -14F, 14.0F, 28.0F, 32.0F)
                        .texOffs(3724, 238).addBox(44F, -98F, -4F, 14.0F, 34.0F, 28.0F)
                        .texOffs(1864, 314).addBox(48F, -120F, 8F, 12.0F, 28.0F, 22.0F)
                        .texOffs(52, 378).addBox(50F, -112F, 12F, 8.0F, 18.0F, 14.0F)
                        .texOffs(428, 314).addBox(-10F, -112F, -24F, 20.0F, 10.0F, 50.0F)
                        .texOffs(1248, 238).addBox(-7F, -136F, -20F, 14.0F, 26.0F, 44.0F)
                        .texOffs(2568, 238).addBox(-18F, -68F, 32F, 36.0F, 48.0F, 16.0F)
                        .texOffs(1048, 314).addBox(-15F, -24F, 40F, 30.0F, 38.0F, 16.0F)
                        .texOffs(856, 378).addBox(-16F, 8F, 46F, 32.0F, 10.0F, 18.0F)
                        .texOffs(764, 314).addBox(-12F, 16F, 52F, 24.0F, 46.0F, 14.0F)
                        .texOffs(1500, 378).addBox(-14F, 56F, 54F, 28.0F, 10.0F, 16.0F)
                        .texOffs(1144, 314).addBox(-9F, 64F, 58F, 18.0F, 42.0F, 12.0F)
                , PartPose.offset(0F, -192F, 0F));

        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(4, 142).addBox(-44F, -24F, -30F, 62.0F, 32.0F, 60.0F)
                        .texOffs(3056, 142).addBox(-48F, -4F, -32F, 70.0F, 10.0F, 64.0F)
                        .texOffs(3232, 238).addBox(-50F, -34F, -24F, 28.0F, 14.0F, 48.0F)
                        .texOffs(2248, 378).addBox(-24F, -20F, -34F, 22.0F, 14.0F, 8.0F)
                        .texOffs(416, 142).addBox(-30F, 8F, -20F, 34.0F, 52.0F, 40.0F)
                        .texOffs(2184, 238).addBox(-36F, 52F, -24F, 46.0F, 16.0F, 48.0F)
                        .texOffs(1508, 4).addBox(-34F, 66F, -24F, 42.0F, 56.0F, 48.0F)
                        .texOffs(336, 238).addBox(-42F, 72F, -16F, 10.0F, 40.0F, 32.0F)
                        .texOffs(3356, 314).addBox(-45F, 80F, -10F, 8.0F, 24.0F, 20.0F)
                        .texOffs(2876, 238).addBox(-36F, 118F, -26F, 46.0F, 10.0F, 52.0F)
                        .texOffs(904, 238).addBox(-32F, 128F, -22F, 40.0F, 26.0F, 44.0F)
                        .texOffs(292, 378).addBox(-30F, 152F, -20F, 16.0F, 12.0F, 18.0F)
                        .texOffs(508, 378).addBox(-12F, 152F, -20F, 16.0F, 12.0F, 18.0F)
                , PartPose.offset(-66F, -164F, 0F));

        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(3804, 4).addBox(-18F, -24F, -30F, 62.0F, 32.0F, 60.0F)
                        .texOffs(3328, 142).addBox(-22F, -4F, -32F, 70.0F, 10.0F, 64.0F)
                        .texOffs(3076, 238).addBox(18F, -34F, -24F, 28.0F, 14.0F, 48.0F)
                        .texOffs(2184, 378).addBox(2F, -20F, -34F, 22.0F, 14.0F, 8.0F)
                        .texOffs(568, 142).addBox(-4F, 8F, -20F, 34.0F, 52.0F, 40.0F)
                        .texOffs(2376, 238).addBox(-10F, 52F, -24F, 46.0F, 16.0F, 48.0F)
                        .texOffs(1324, 4).addBox(-8F, 66F, -24F, 42.0F, 56.0F, 48.0F)
                        .texOffs(248, 238).addBox(32F, 72F, -16F, 10.0F, 40.0F, 32.0F)
                        .texOffs(3296, 314).addBox(36F, 80F, -10F, 8.0F, 24.0F, 20.0F)
                        .texOffs(2676, 238).addBox(-10F, 118F, -26F, 46.0F, 10.0F, 52.0F)
                        .texOffs(1076, 238).addBox(-6F, 128F, -22F, 40.0F, 26.0F, 44.0F)
                        .texOffs(436, 378).addBox(-4F, 152F, -20F, 16.0F, 12.0F, 18.0F)
                        .texOffs(364, 378).addBox(14F, 152F, -20F, 16.0F, 12.0F, 18.0F)
                , PartPose.offset(66F, -164F, 0F));

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
