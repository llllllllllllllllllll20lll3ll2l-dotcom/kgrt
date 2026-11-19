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
 * Ультра-детализированная 3D-модель Голема-Воина (141 скульптурный элемент, 24x геометрия, текстура 4096x4096).
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
    private final ModelPart extraHead;
    private final ModelPart extraChest;
    private final ModelPart extraLegsR;
    private final ModelPart extraLegsL;
    private final ModelPart extraFeetR;
    private final ModelPart extraFeetL;

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
        this.leftArm = this.body.getChild("left_arm");
        this.rightLeg = this.root.getChild("right_leg");
        this.leftLeg = this.root.getChild("left_leg");
        this.extraHead = this.head.getChild("extra_head");
        this.extraChest = this.body.getChild("extra_chest");
        this.extraLegsR = this.rightLeg.getChild("extra_legs_right_leg");
        this.extraLegsL = this.leftLeg.getChild("extra_legs_left_leg");
        this.extraFeetR = this.rightLeg.getChild("extra_feet_right_leg");
        this.extraFeetL = this.leftLeg.getChild("extra_feet_left_leg");

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

    public void translateToLeftHand(PoseStack matrices) {
        this.root.translateAndRotate(matrices);
        this.body.translateAndRotate(matrices);
        this.leftArm.translateAndRotate(matrices);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();
        PartDefinition part_root = rootPart.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);
        CubeListBuilder b_0 = CubeListBuilder.create();
        b_0.texOffs(3420, 4).addBox(-42.0F, -12.0F, -42.0F, 84.0F, 108.0F, 84.0F);
        b_0.texOffs(916, 1072).addBox(-36.0F, 8.0F, -50.0F, 72.0F, 74.0F, 12.0F);
        b_0.texOffs(1616, 1072).addBox(-32.0F, 10.0F, 40.0F, 64.0F, 70.0F, 12.0F);
        b_0.texOffs(3548, 954).addBox(-48.0F, -6.0F, -38.0F, 96.0F, 18.0F, 76.0F);
        b_0.texOffs(2216, 672).addBox(-58.0F, 8.0F, -34.0F, 20.0F, 76.0F, 68.0F);
        b_0.texOffs(2436, 954).addBox(-62.0F, 18.0F, -24.0F, 14.0F, 56.0F, 48.0F);
        b_0.texOffs(1072, 1170).addBox(-66.0F, 28.0F, -12.0F, 8.0F, 34.0F, 24.0F);
        b_0.texOffs(4, 1170).addBox(-38.0F, 86.0F, -56.0F, 76.0F, 46.0F, 18.0F);
        b_0.texOffs(2404, 1170).addBox(-30.0F, 94.0F, -64.0F, 60.0F, 32.0F, 12.0F);
        b_0.texOffs(1500, 1238).addBox(-14.0F, 102.0F, -70.0F, 28.0F, 18.0F, 8.0F);
        b_0.texOffs(1860, 496).addBox(-40.0F, 128.0F, -40.0F, 80.0F, 76.0F, 80.0F);
        b_0.texOffs(2616, 1072).addBox(-30.0F, 134.0F, -48.0F, 60.0F, 62.0F, 12.0F);
        b_0.texOffs(788, 1170).addBox(-10.0F, 138.0F, -52.0F, 20.0F, 54.0F, 8.0F);
        b_0.texOffs(2692, 954).addBox(-44.0F, 162.0F, -42.0F, 88.0F, 12.0F, 84.0F);
        b_0.texOffs(464, 496).addBox(-46.0F, 196.0F, -62.0F, 92.0F, 48.0F, 118.0F);
        b_0.texOffs(616, 672).addBox(-50.0F, 228.0F, -68.0F, 100.0F, 16.0F, 128.0F);
        b_0.texOffs(388, 1170).addBox(-36.0F, 206.0F, -80.0F, 72.0F, 36.0F, 26.0F);
        b_0.texOffs(2700, 1170).addBox(-26.0F, 212.0F, -88.0F, 52.0F, 26.0F, 12.0F);
        PartDefinition part_right_leg = part_root.addOrReplaceChild("right_leg", b_0, PartPose.offset(-56.0F, -228.0F, 0.0F));
        CubeListBuilder b_1 = CubeListBuilder.create();
        b_1.texOffs(344, 300).addBox(-48.0F, 4.0F, -52.0F, 96.0F, 84.0F, 104.0F);
        PartDefinition part_extra_legs_right_leg = part_right_leg.addOrReplaceChild("extra_legs_right_leg", b_1, PartPose.ZERO);
        CubeListBuilder b_2 = CubeListBuilder.create();
        b_2.texOffs(2396, 4).addBox(-54.0F, 198.0F, -86.0F, 108.0F, 48.0F, 146.0F);
        PartDefinition part_extra_feet_right_leg = part_right_leg.addOrReplaceChild("extra_feet_right_leg", b_2, PartPose.ZERO);
        CubeListBuilder b_3 = CubeListBuilder.create();
        b_3.texOffs(4, 300).addBox(-42.0F, -12.0F, -42.0F, 84.0F, 108.0F, 84.0F);
        b_3.texOffs(1088, 1072).addBox(-36.0F, 8.0F, -50.0F, 72.0F, 74.0F, 12.0F);
        b_3.texOffs(1772, 1072).addBox(-32.0F, 10.0F, 40.0F, 64.0F, 70.0F, 12.0F);
        b_3.texOffs(4, 1072).addBox(-48.0F, -6.0F, -38.0F, 96.0F, 18.0F, 76.0F);
        b_3.texOffs(2396, 672).addBox(38.0F, 8.0F, -34.0F, 20.0F, 76.0F, 68.0F);
        b_3.texOffs(2564, 954).addBox(42.0F, 18.0F, -24.0F, 14.0F, 56.0F, 48.0F);
        b_3.texOffs(1140, 1170).addBox(46.0F, 28.0F, -12.0F, 8.0F, 34.0F, 24.0F);
        b_3.texOffs(196, 1170).addBox(-38.0F, 86.0F, -56.0F, 76.0F, 46.0F, 18.0F);
        b_3.texOffs(2552, 1170).addBox(-30.0F, 94.0F, -64.0F, 60.0F, 32.0F, 12.0F);
        b_3.texOffs(1576, 1238).addBox(-14.0F, 102.0F, -70.0F, 28.0F, 18.0F, 8.0F);
        b_3.texOffs(2184, 496).addBox(-40.0F, 128.0F, -40.0F, 80.0F, 76.0F, 80.0F);
        b_3.texOffs(2764, 1072).addBox(-30.0F, 134.0F, -48.0F, 60.0F, 62.0F, 12.0F);
        b_3.texOffs(848, 1170).addBox(-10.0F, 138.0F, -52.0F, 20.0F, 54.0F, 8.0F);
        b_3.texOffs(3040, 954).addBox(-44.0F, 162.0F, -42.0F, 88.0F, 12.0F, 84.0F);
        b_3.texOffs(888, 496).addBox(-46.0F, 196.0F, -62.0F, 92.0F, 48.0F, 118.0F);
        b_3.texOffs(1076, 672).addBox(-50.0F, 228.0F, -68.0F, 100.0F, 16.0F, 128.0F);
        b_3.texOffs(588, 1170).addBox(-36.0F, 206.0F, -80.0F, 72.0F, 36.0F, 26.0F);
        b_3.texOffs(2832, 1170).addBox(-26.0F, 212.0F, -88.0F, 52.0F, 26.0F, 12.0F);
        PartDefinition part_left_leg = part_root.addOrReplaceChild("left_leg", b_3, PartPose.offset(56.0F, -228.0F, 0.0F));
        CubeListBuilder b_4 = CubeListBuilder.create();
        b_4.texOffs(748, 300).addBox(-48.0F, 4.0F, -52.0F, 96.0F, 84.0F, 104.0F);
        PartDefinition part_extra_legs_left_leg = part_left_leg.addOrReplaceChild("extra_legs_left_leg", b_4, PartPose.ZERO);
        CubeListBuilder b_5 = CubeListBuilder.create();
        b_5.texOffs(2908, 4).addBox(-54.0F, 198.0F, -86.0F, 108.0F, 48.0F, 146.0F);
        PartDefinition part_extra_feet_left_leg = part_left_leg.addOrReplaceChild("extra_feet_left_leg", b_5, PartPose.ZERO);
        CubeListBuilder b_6 = CubeListBuilder.create();
        b_6.texOffs(1428, 822).addBox(-52.0F, -290.0F, -44.0F, 104.0F, 34.0F, 88.0F);
        b_6.texOffs(3288, 496).addBox(-82.0F, -270.0F, -64.0F, 164.0F, 22.0F, 128.0F);
        b_6.texOffs(720, 4).addBox(-96.0F, -250.0F, -64.0F, 192.0F, 136.0F, 128.0F);
        b_6.texOffs(532, 1072).addBox(-86.0F, -242.0F, -76.0F, 78.0F, 70.0F, 16.0F);
        b_6.texOffs(724, 1072).addBox(8.0F, -242.0F, -76.0F, 78.0F, 70.0F, 16.0F);
        b_6.texOffs(3576, 672).addBox(-90.0F, -246.0F, 54.0F, 180.0F, 118.0F, 18.0F);
        b_6.texOffs(2020, 954).addBox(-48.0F, -220.0F, -82.0F, 96.0F, 90.0F, 18.0F);
        b_6.texOffs(1928, 1072).addBox(-36.0F, -208.0F, -88.0F, 72.0F, 66.0F, 14.0F);
        b_6.texOffs(1760, 1170).addBox(-24.0F, -196.0F, -94.0F, 48.0F, 42.0F, 10.0F);
        b_6.texOffs(1336, 1238).addBox(-14.0F, -186.0F, -100.0F, 28.0F, 24.0F, 8.0F);
        b_6.texOffs(1896, 300).addBox(-74.0F, -112.0F, -52.0F, 148.0F, 70.0F, 104.0F);
        b_6.texOffs(4, 822).addBox(-80.0F, -104.0F, -56.0F, 160.0F, 16.0F, 112.0F);
        b_6.texOffs(552, 822).addBox(-80.0F, -78.0F, -56.0F, 160.0F, 16.0F, 112.0F);
        b_6.texOffs(2744, 300).addBox(-90.0F, -46.0F, -62.0F, 180.0F, 48.0F, 124.0F);
        b_6.texOffs(4, 672).addBox(-86.0F, -38.0F, -66.0F, 172.0F, 14.0F, 132.0F);
        b_6.texOffs(908, 1170).addBox(-32.0F, -52.0F, -74.0F, 64.0F, 42.0F, 16.0F);
        b_6.texOffs(1412, 1238).addBox(-16.0F, -40.0F, -80.0F, 32.0F, 20.0F, 10.0F);
        b_6.texOffs(3388, 954).addBox(-32.0F, -14.0F, -68.0F, 64.0F, 82.0F, 14.0F);
        b_6.texOffs(1148, 1238).addBox(-38.0F, 54.0F, -70.0F, 76.0F, 16.0F, 16.0F);
        b_6.texOffs(1260, 1072).addBox(-32.0F, -14.0F, 54.0F, 64.0F, 70.0F, 14.0F);
        PartDefinition part_body = part_root.addOrReplaceChild("body", b_6, PartPose.offset(0.0F, -228.0F, 0.0F));
        CubeListBuilder b_7 = CubeListBuilder.create();
        b_7.texOffs(4, 4).addBox(-104.0F, -254.0F, -74.0F, 208.0F, 144.0F, 148.0F);
        PartDefinition part_extra_chest = part_body.addOrReplaceChild("extra_chest", b_7, PartPose.ZERO);
        CubeListBuilder b_8 = CubeListBuilder.create();
        b_8.texOffs(2404, 300).addBox(-62.0F, -56.0F, -12.0F, 124.0F, 130.0F, 44.0F);
        b_8.texOffs(1208, 1170).addBox(-50.0F, -68.0F, -4.0F, 100.0F, 20.0F, 36.0F);
        b_8.texOffs(1484, 1170).addBox(-50.0F, 66.0F, -4.0F, 100.0F, 18.0F, 36.0F);
        b_8.texOffs(2252, 954).addBox(-38.0F, -38.0F, 28.0F, 76.0F, 90.0F, 14.0F);
        b_8.texOffs(2912, 1072).addBox(-24.0F, -24.0F, 38.0F, 48.0F, 62.0F, 10.0F);
        b_8.texOffs(2112, 1170).addBox(-14.0F, -14.0F, 44.0F, 28.0F, 42.0F, 8.0F);
        b_8.texOffs(2992, 496).addBox(-82.0F, -50.0F, -8.0F, 32.0F, 114.0F, 40.0F);
        b_8.texOffs(3200, 1072).addBox(-86.0F, -60.0F, -12.0F, 40.0F, 18.0F, 48.0F);
        b_8.texOffs(3380, 1072).addBox(-86.0F, 50.0F, -12.0F, 40.0F, 18.0F, 48.0F);
        b_8.texOffs(2104, 1072).addBox(-80.0F, -16.0F, -12.0F, 28.0F, 32.0F, 48.0F);
        b_8.texOffs(1880, 1170).addBox(-66.0F, -90.0F, 0.0F, 28.0F, 24.0F, 28.0F);
        b_8.texOffs(3140, 496).addBox(50.0F, -50.0F, -8.0F, 32.0F, 114.0F, 40.0F);
        b_8.texOffs(3560, 1072).addBox(46.0F, -60.0F, -12.0F, 40.0F, 18.0F, 48.0F);
        b_8.texOffs(3740, 1072).addBox(46.0F, 50.0F, -12.0F, 40.0F, 18.0F, 48.0F);
        b_8.texOffs(2260, 1072).addBox(52.0F, -16.0F, -12.0F, 28.0F, 32.0F, 48.0F);
        b_8.texOffs(1996, 1170).addBox(66.0F, -90.0F, 0.0F, 28.0F, 24.0F, 28.0F);
        PartDefinition part_backpack = part_body.addOrReplaceChild("backpack", b_8, PartPose.offset(0.0F, -192.0F, 74.0F));
        CubeListBuilder b_9 = CubeListBuilder.create();
        b_9.texOffs(1364, 4).addBox(-56.0F, -98.0F, -56.0F, 112.0F, 98.0F, 112.0F);
        b_9.texOffs(1420, 1072).addBox(-44.0F, -84.0F, 54.0F, 88.0F, 74.0F, 8.0F);
        b_9.texOffs(1996, 1238).addBox(-36.0F, -68.0F, 59.0F, 72.0F, 14.0F, 6.0F);
        b_9.texOffs(3764, 1170).addBox(-38.0F, -36.0F, -62.0F, 8.0F, 28.0F, 8.0F);
        b_9.texOffs(3800, 1170).addBox(-20.0F, -36.0F, -62.0F, 8.0F, 28.0F, 8.0F);
        b_9.texOffs(3836, 1170).addBox(-2.0F, -36.0F, -62.0F, 8.0F, 28.0F, 8.0F);
        b_9.texOffs(3872, 1170).addBox(16.0F, -36.0F, -62.0F, 8.0F, 28.0F, 8.0F);
        b_9.texOffs(3908, 1170).addBox(34.0F, -36.0F, -62.0F, 8.0F, 28.0F, 8.0F);
        b_9.texOffs(1852, 1238).addBox(-42.0F, -74.0F, -60.0F, 26.0F, 14.0F, 8.0F);
        b_9.texOffs(1924, 1238).addBox(16.0F, -74.0F, -60.0F, 26.0F, 14.0F, 8.0F);
        b_9.texOffs(1652, 1238).addBox(-48.0F, -86.0F, -64.0F, 36.0F, 12.0F, 12.0F);
        b_9.texOffs(1752, 1238).addBox(12.0F, -86.0F, -64.0F, 36.0F, 12.0F, 12.0F);
        b_9.texOffs(3032, 1072).addBox(-10.0F, -92.0F, -68.0F, 20.0F, 58.0F, 14.0F);
        b_9.texOffs(1816, 822).addBox(-66.0F, -60.0F, -60.0F, 24.0F, 54.0F, 66.0F);
        b_9.texOffs(2000, 822).addBox(42.0F, -60.0F, -60.0F, 24.0F, 54.0F, 66.0F);
        b_9.texOffs(2188, 1170).addBox(-44.0F, -26.0F, -64.0F, 88.0F, 26.0F, 18.0F);
        b_9.texOffs(1312, 496).addBox(-68.0F, -108.0F, -68.0F, 136.0F, 22.0F, 136.0F);
        b_9.texOffs(2508, 496).addBox(-60.0F, -138.0F, -60.0F, 120.0F, 34.0F, 120.0F);
        b_9.texOffs(2184, 822).addBox(-50.0F, -152.0F, -50.0F, 100.0F, 18.0F, 100.0F);
        b_9.texOffs(1772, 954).addBox(-16.0F, -168.0F, -42.0F, 32.0F, 18.0F, 90.0F);
        b_9.texOffs(4, 954).addBox(-12.0F, -196.0F, -38.0F, 24.0F, 32.0F, 82.0F);
        b_9.texOffs(352, 1072).addBox(-10.0F, -210.0F, -30.0F, 20.0F, 20.0F, 68.0F);
        b_9.texOffs(3104, 1072).addBox(-12.0F, -190.0F, 38.0F, 24.0F, 48.0F, 22.0F);
        PartDefinition part_head = part_body.addOrReplaceChild("head", b_9, PartPose.offset(0.0F, -288.0F, 0.0F));
        CubeListBuilder b_10 = CubeListBuilder.create();
        b_10.texOffs(1816, 4).addBox(-72.0F, -142.0F, -72.0F, 144.0F, 52.0F, 144.0F);
        PartDefinition part_extra_head = part_head.addOrReplaceChild("extra_head", b_10, PartPose.ZERO);
        CubeListBuilder b_11 = CubeListBuilder.create();
        b_11.texOffs(3356, 300).addBox(-58.0F, -44.0F, -56.0F, 116.0F, 60.0F, 112.0F);
        b_11.texOffs(2576, 672).addBox(-64.0F, -12.0F, -60.0F, 128.0F, 18.0F, 120.0F);
        b_11.texOffs(1028, 954).addBox(-48.0F, -62.0F, -44.0F, 96.0F, 22.0F, 88.0F);
        b_11.texOffs(4, 1238).addBox(-36.0F, -36.0F, -64.0F, 72.0F, 22.0F, 12.0F);
        b_11.texOffs(2156, 1238).addBox(-22.0F, -31.0F, -68.0F, 44.0F, 12.0F, 8.0F);
        b_11.texOffs(1536, 672).addBox(-46.0F, 12.0F, -38.0F, 92.0F, 68.0F, 76.0F);
        b_11.texOffs(3300, 822).addBox(-52.0F, 70.0F, -44.0F, 104.0F, 28.0F, 88.0F);
        b_11.texOffs(1152, 300).addBox(-48.0F, 96.0F, -44.0F, 96.0F, 88.0F, 88.0F);
        b_11.texOffs(1100, 822).addBox(-62.0F, 106.0F, -30.0F, 20.0F, 66.0F, 60.0F);
        b_11.texOffs(2416, 1072).addBox(-62.0F, 116.0F, -18.0F, 12.0F, 44.0F, 36.0F);
        b_11.texOffs(220, 954).addBox(-54.0F, 180.0F, -46.0F, 108.0F, 18.0F, 92.0F);
        b_11.texOffs(2588, 822).addBox(-46.0F, 198.0F, -42.0F, 92.0F, 34.0F, 84.0F);
        b_11.texOffs(2964, 1170).addBox(-42.0F, 230.0F, -34.0F, 32.0F, 20.0F, 16.0F);
        b_11.texOffs(348, 1238).addBox(0.0F, 230.0F, -34.0F, 32.0F, 18.0F, 16.0F);
        b_11.texOffs(3064, 1170).addBox(-42.0F, 230.0F, -14.0F, 32.0F, 20.0F, 16.0F);
        b_11.texOffs(448, 1238).addBox(0.0F, 230.0F, -14.0F, 32.0F, 18.0F, 16.0F);
        b_11.texOffs(3164, 1170).addBox(-42.0F, 230.0F, 6.0F, 32.0F, 20.0F, 16.0F);
        b_11.texOffs(548, 1238).addBox(0.0F, 230.0F, 6.0F, 32.0F, 18.0F, 16.0F);
        b_11.texOffs(3264, 1170).addBox(-42.0F, 230.0F, 26.0F, 32.0F, 20.0F, 16.0F);
        b_11.texOffs(648, 1238).addBox(0.0F, 230.0F, 26.0F, 32.0F, 18.0F, 16.0F);
        PartDefinition part_right_arm = part_body.addOrReplaceChild("right_arm", b_11, PartPose.offset(-124.0F, -250.0F, 0.0F));
        CubeListBuilder b_12 = CubeListBuilder.create();
        b_12.texOffs(4, 496).addBox(-58.0F, -44.0F, -56.0F, 116.0F, 60.0F, 112.0F);
        b_12.texOffs(3076, 672).addBox(-64.0F, -12.0F, -60.0F, 128.0F, 18.0F, 120.0F);
        b_12.texOffs(1400, 954).addBox(-48.0F, -62.0F, -44.0F, 96.0F, 22.0F, 88.0F);
        b_12.texOffs(176, 1238).addBox(-36.0F, -36.0F, -64.0F, 72.0F, 22.0F, 12.0F);
        b_12.texOffs(2264, 1238).addBox(-22.0F, -31.0F, -68.0F, 44.0F, 12.0F, 8.0F);
        b_12.texOffs(1876, 672).addBox(-46.0F, 12.0F, -38.0F, 92.0F, 68.0F, 76.0F);
        b_12.texOffs(3688, 822).addBox(-52.0F, 70.0F, -44.0F, 104.0F, 28.0F, 88.0F);
        b_12.texOffs(1524, 300).addBox(-48.0F, 96.0F, -44.0F, 96.0F, 88.0F, 88.0F);
        b_12.texOffs(1264, 822).addBox(42.0F, 106.0F, -30.0F, 20.0F, 66.0F, 60.0F);
        b_12.texOffs(2516, 1072).addBox(50.0F, 116.0F, -18.0F, 12.0F, 44.0F, 36.0F);
        b_12.texOffs(624, 954).addBox(-54.0F, 180.0F, -46.0F, 108.0F, 18.0F, 92.0F);
        b_12.texOffs(2944, 822).addBox(-46.0F, 198.0F, -42.0F, 92.0F, 34.0F, 84.0F);
        b_12.texOffs(3364, 1170).addBox(-42.0F, 230.0F, -34.0F, 32.0F, 20.0F, 16.0F);
        b_12.texOffs(748, 1238).addBox(0.0F, 230.0F, -34.0F, 32.0F, 18.0F, 16.0F);
        b_12.texOffs(3464, 1170).addBox(-42.0F, 230.0F, -14.0F, 32.0F, 20.0F, 16.0F);
        b_12.texOffs(848, 1238).addBox(0.0F, 230.0F, -14.0F, 32.0F, 18.0F, 16.0F);
        b_12.texOffs(3564, 1170).addBox(-42.0F, 230.0F, 6.0F, 32.0F, 20.0F, 16.0F);
        b_12.texOffs(948, 1238).addBox(0.0F, 230.0F, 6.0F, 32.0F, 18.0F, 16.0F);
        b_12.texOffs(3664, 1170).addBox(-42.0F, 230.0F, 26.0F, 32.0F, 20.0F, 16.0F);
        b_12.texOffs(1048, 1238).addBox(0.0F, 230.0F, 26.0F, 32.0F, 18.0F, 16.0F);
        PartDefinition part_left_arm = part_body.addOrReplaceChild("left_arm", b_12, PartPose.offset(124.0F, -250.0F, 0.0F));
        return LayerDefinition.create(mesh, 4096, 4096);
    }

    @Override
    public void setupAnim(GolemRenderState state) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.extraHead.visible = state.hasHeadArmor;
        this.extraChest.visible = state.hasChestArmor;
        this.extraLegsR.visible = state.hasLegsArmor;
        this.extraLegsL.visible = state.hasLegsArmor;
        this.extraFeetR.visible = state.hasFeetArmor;
        this.extraFeetL.visible = state.hasFeetArmor;

        if (state.isDisabled) {
            this.body.xRot = 0.26F;
            this.head.xRot = 0.65F;
            this.head.yRot = -0.14F;
            this.rightArm.xRot = -0.18F;
            this.leftArm.xRot = -0.18F;
            return;
        }

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
