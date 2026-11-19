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
 * Ультра-детализированная 3D-модель Голема-Воина (139 скульптурных элементов, 24x геометрия, текстура 4096x4096).
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
        b_0.texOffs(408, 300).addBox(-42.0F, 0.0F, -42.0F, 84.0F, 96.0F, 84.0F);
        b_0.texOffs(916, 1062).addBox(-36.0F, 8.0F, -50.0F, 72.0F, 74.0F, 12.0F);
        b_0.texOffs(1420, 1062).addBox(-32.0F, 10.0F, 40.0F, 64.0F, 70.0F, 12.0F);
        b_0.texOffs(3548, 944).addBox(-48.0F, -6.0F, -38.0F, 96.0F, 18.0F, 76.0F);
        b_0.texOffs(2216, 662).addBox(-58.0F, 8.0F, -34.0F, 20.0F, 76.0F, 68.0F);
        b_0.texOffs(2436, 944).addBox(-62.0F, 18.0F, -24.0F, 14.0F, 56.0F, 48.0F);
        b_0.texOffs(880, 1160).addBox(-66.0F, 28.0F, -12.0F, 8.0F, 34.0F, 24.0F);
        b_0.texOffs(3724, 1062).addBox(-38.0F, 86.0F, -56.0F, 76.0F, 46.0F, 18.0F);
        b_0.texOffs(2212, 1160).addBox(-30.0F, 94.0F, -64.0F, 60.0F, 32.0F, 12.0F);
        b_0.texOffs(1156, 1228).addBox(-14.0F, 102.0F, -70.0F, 28.0F, 18.0F, 8.0F);
        b_0.texOffs(1588, 492).addBox(-40.0F, 128.0F, -40.0F, 80.0F, 76.0F, 80.0F);
        b_0.texOffs(2420, 1062).addBox(-30.0F, 134.0F, -48.0F, 60.0F, 62.0F, 12.0F);
        b_0.texOffs(596, 1160).addBox(-10.0F, 138.0F, -52.0F, 20.0F, 54.0F, 8.0F);
        b_0.texOffs(2692, 944).addBox(-44.0F, 162.0F, -42.0F, 88.0F, 12.0F, 84.0F);
        b_0.texOffs(3600, 300).addBox(-46.0F, 196.0F, -62.0F, 92.0F, 48.0F, 118.0F);
        b_0.texOffs(616, 662).addBox(-50.0F, 228.0F, -68.0F, 100.0F, 16.0F, 128.0F);
        b_0.texOffs(196, 1160).addBox(-36.0F, 206.0F, -80.0F, 72.0F, 36.0F, 26.0F);
        b_0.texOffs(2508, 1160).addBox(-26.0F, 212.0F, -88.0F, 52.0F, 26.0F, 12.0F);
        PartDefinition part_right_leg = part_root.addOrReplaceChild("right_leg", b_0, PartPose.offset(-56.0F, -228.0F, 0.0F));
        CubeListBuilder b_1 = CubeListBuilder.create();
        b_1.texOffs(3420, 4).addBox(-48.0F, 4.0F, -52.0F, 96.0F, 84.0F, 104.0F);
        PartDefinition part_extra_legs_right_leg = part_right_leg.addOrReplaceChild("extra_legs_right_leg", b_1, PartPose.ZERO);
        CubeListBuilder b_2 = CubeListBuilder.create();
        b_2.texOffs(2396, 4).addBox(-54.0F, 198.0F, -86.0F, 108.0F, 48.0F, 146.0F);
        PartDefinition part_extra_feet_right_leg = part_right_leg.addOrReplaceChild("extra_feet_right_leg", b_2, PartPose.ZERO);
        CubeListBuilder b_3 = CubeListBuilder.create();
        b_3.texOffs(748, 300).addBox(-42.0F, 0.0F, -42.0F, 84.0F, 96.0F, 84.0F);
        b_3.texOffs(1088, 1062).addBox(-36.0F, 8.0F, -50.0F, 72.0F, 74.0F, 12.0F);
        b_3.texOffs(1576, 1062).addBox(-32.0F, 10.0F, 40.0F, 64.0F, 70.0F, 12.0F);
        b_3.texOffs(4, 1062).addBox(-48.0F, -6.0F, -38.0F, 96.0F, 18.0F, 76.0F);
        b_3.texOffs(2396, 662).addBox(38.0F, 8.0F, -34.0F, 20.0F, 76.0F, 68.0F);
        b_3.texOffs(2564, 944).addBox(42.0F, 18.0F, -24.0F, 14.0F, 56.0F, 48.0F);
        b_3.texOffs(948, 1160).addBox(46.0F, 28.0F, -12.0F, 8.0F, 34.0F, 24.0F);
        b_3.texOffs(4, 1160).addBox(-38.0F, 86.0F, -56.0F, 76.0F, 46.0F, 18.0F);
        b_3.texOffs(2360, 1160).addBox(-30.0F, 94.0F, -64.0F, 60.0F, 32.0F, 12.0F);
        b_3.texOffs(1232, 1228).addBox(-14.0F, 102.0F, -70.0F, 28.0F, 18.0F, 8.0F);
        b_3.texOffs(1912, 492).addBox(-40.0F, 128.0F, -40.0F, 80.0F, 76.0F, 80.0F);
        b_3.texOffs(2568, 1062).addBox(-30.0F, 134.0F, -48.0F, 60.0F, 62.0F, 12.0F);
        b_3.texOffs(656, 1160).addBox(-10.0F, 138.0F, -52.0F, 20.0F, 54.0F, 8.0F);
        b_3.texOffs(3040, 944).addBox(-44.0F, 162.0F, -42.0F, 88.0F, 12.0F, 84.0F);
        b_3.texOffs(4, 492).addBox(-46.0F, 196.0F, -62.0F, 92.0F, 48.0F, 118.0F);
        b_3.texOffs(1076, 662).addBox(-50.0F, 228.0F, -68.0F, 100.0F, 16.0F, 128.0F);
        b_3.texOffs(396, 1160).addBox(-36.0F, 206.0F, -80.0F, 72.0F, 36.0F, 26.0F);
        b_3.texOffs(2640, 1160).addBox(-26.0F, 212.0F, -88.0F, 52.0F, 26.0F, 12.0F);
        PartDefinition part_left_leg = part_root.addOrReplaceChild("left_leg", b_3, PartPose.offset(56.0F, -228.0F, 0.0F));
        CubeListBuilder b_4 = CubeListBuilder.create();
        b_4.texOffs(4, 300).addBox(-48.0F, 4.0F, -52.0F, 96.0F, 84.0F, 104.0F);
        PartDefinition part_extra_legs_left_leg = part_left_leg.addOrReplaceChild("extra_legs_left_leg", b_4, PartPose.ZERO);
        CubeListBuilder b_5 = CubeListBuilder.create();
        b_5.texOffs(2908, 4).addBox(-54.0F, 198.0F, -86.0F, 108.0F, 48.0F, 146.0F);
        PartDefinition part_extra_feet_left_leg = part_left_leg.addOrReplaceChild("extra_feet_left_leg", b_5, PartPose.ZERO);
        CubeListBuilder b_6 = CubeListBuilder.create();
        b_6.texOffs(1428, 812).addBox(-52.0F, -290.0F, -44.0F, 104.0F, 34.0F, 88.0F);
        b_6.texOffs(3016, 492).addBox(-82.0F, -270.0F, -64.0F, 164.0F, 22.0F, 128.0F);
        b_6.texOffs(720, 4).addBox(-96.0F, -250.0F, -64.0F, 192.0F, 136.0F, 128.0F);
        b_6.texOffs(532, 1062).addBox(-86.0F, -242.0F, -76.0F, 78.0F, 70.0F, 16.0F);
        b_6.texOffs(724, 1062).addBox(8.0F, -242.0F, -76.0F, 78.0F, 70.0F, 16.0F);
        b_6.texOffs(3576, 662).addBox(-90.0F, -246.0F, 54.0F, 180.0F, 118.0F, 18.0F);
        b_6.texOffs(2020, 944).addBox(-48.0F, -220.0F, -82.0F, 96.0F, 90.0F, 18.0F);
        b_6.texOffs(1732, 1062).addBox(-36.0F, -208.0F, -88.0F, 72.0F, 66.0F, 14.0F);
        b_6.texOffs(1568, 1160).addBox(-24.0F, -196.0F, -94.0F, 48.0F, 42.0F, 10.0F);
        b_6.texOffs(992, 1228).addBox(-14.0F, -186.0F, -100.0F, 28.0F, 24.0F, 8.0F);
        b_6.texOffs(1832, 300).addBox(-74.0F, -112.0F, -52.0F, 148.0F, 70.0F, 104.0F);
        b_6.texOffs(4, 812).addBox(-80.0F, -104.0F, -56.0F, 160.0F, 16.0F, 112.0F);
        b_6.texOffs(552, 812).addBox(-80.0F, -78.0F, -56.0F, 160.0F, 16.0F, 112.0F);
        b_6.texOffs(976, 492).addBox(-90.0F, -46.0F, -62.0F, 180.0F, 32.0F, 124.0F);
        b_6.texOffs(4, 662).addBox(-86.0F, -38.0F, -66.0F, 172.0F, 14.0F, 132.0F);
        b_6.texOffs(716, 1160).addBox(-32.0F, -52.0F, -74.0F, 64.0F, 42.0F, 16.0F);
        b_6.texOffs(1068, 1228).addBox(-16.0F, -40.0F, -80.0F, 32.0F, 20.0F, 10.0F);
        b_6.texOffs(3388, 944).addBox(-32.0F, -14.0F, -68.0F, 64.0F, 82.0F, 14.0F);
        b_6.texOffs(804, 1228).addBox(-38.0F, 54.0F, -70.0F, 76.0F, 16.0F, 16.0F);
        b_6.texOffs(1260, 1062).addBox(-32.0F, -14.0F, 54.0F, 64.0F, 70.0F, 14.0F);
        PartDefinition part_body = part_root.addOrReplaceChild("body", b_6, PartPose.offset(0.0F, -228.0F, 0.0F));
        CubeListBuilder b_7 = CubeListBuilder.create();
        b_7.texOffs(4, 4).addBox(-104.0F, -254.0F, -74.0F, 208.0F, 144.0F, 148.0F);
        PartDefinition part_extra_chest = part_body.addOrReplaceChild("extra_chest", b_7, PartPose.ZERO);
        CubeListBuilder b_8 = CubeListBuilder.create();
        b_8.texOffs(2340, 300).addBox(-62.0F, -56.0F, -12.0F, 124.0F, 130.0F, 44.0F);
        b_8.texOffs(1016, 1160).addBox(-50.0F, -68.0F, -4.0F, 100.0F, 20.0F, 36.0F);
        b_8.texOffs(1292, 1160).addBox(-50.0F, 66.0F, -4.0F, 100.0F, 18.0F, 36.0F);
        b_8.texOffs(2252, 944).addBox(-38.0F, -38.0F, 28.0F, 76.0F, 90.0F, 14.0F);
        b_8.texOffs(2716, 1062).addBox(-24.0F, -24.0F, 38.0F, 48.0F, 62.0F, 10.0F);
        b_8.texOffs(1920, 1160).addBox(-14.0F, -14.0F, 44.0F, 28.0F, 42.0F, 8.0F);
        b_8.texOffs(2720, 492).addBox(-82.0F, -50.0F, -8.0F, 32.0F, 114.0F, 40.0F);
        b_8.texOffs(3004, 1062).addBox(-86.0F, -60.0F, -12.0F, 40.0F, 18.0F, 48.0F);
        b_8.texOffs(3184, 1062).addBox(-86.0F, 50.0F, -12.0F, 40.0F, 18.0F, 48.0F);
        b_8.texOffs(1908, 1062).addBox(-80.0F, -16.0F, -12.0F, 28.0F, 32.0F, 48.0F);
        b_8.texOffs(1688, 1160).addBox(-66.0F, -90.0F, 0.0F, 28.0F, 24.0F, 28.0F);
        b_8.texOffs(2868, 492).addBox(50.0F, -50.0F, -8.0F, 32.0F, 114.0F, 40.0F);
        b_8.texOffs(3364, 1062).addBox(46.0F, -60.0F, -12.0F, 40.0F, 18.0F, 48.0F);
        b_8.texOffs(3544, 1062).addBox(46.0F, 50.0F, -12.0F, 40.0F, 18.0F, 48.0F);
        b_8.texOffs(2064, 1062).addBox(52.0F, -16.0F, -12.0F, 28.0F, 32.0F, 48.0F);
        b_8.texOffs(1804, 1160).addBox(66.0F, -90.0F, 0.0F, 28.0F, 24.0F, 28.0F);
        PartDefinition part_backpack = part_body.addOrReplaceChild("backpack", b_8, PartPose.offset(0.0F, -192.0F, 74.0F));
        CubeListBuilder b_9 = CubeListBuilder.create();
        b_9.texOffs(1364, 4).addBox(-56.0F, -98.0F, -56.0F, 112.0F, 98.0F, 112.0F);
        b_9.texOffs(3572, 1160).addBox(-38.0F, -36.0F, -62.0F, 8.0F, 28.0F, 8.0F);
        b_9.texOffs(3608, 1160).addBox(-20.0F, -36.0F, -62.0F, 8.0F, 28.0F, 8.0F);
        b_9.texOffs(3644, 1160).addBox(-2.0F, -36.0F, -62.0F, 8.0F, 28.0F, 8.0F);
        b_9.texOffs(3680, 1160).addBox(16.0F, -36.0F, -62.0F, 8.0F, 28.0F, 8.0F);
        b_9.texOffs(3716, 1160).addBox(34.0F, -36.0F, -62.0F, 8.0F, 28.0F, 8.0F);
        b_9.texOffs(1508, 1228).addBox(-42.0F, -74.0F, -60.0F, 26.0F, 14.0F, 8.0F);
        b_9.texOffs(1580, 1228).addBox(16.0F, -74.0F, -60.0F, 26.0F, 14.0F, 8.0F);
        b_9.texOffs(1308, 1228).addBox(-48.0F, -86.0F, -64.0F, 36.0F, 12.0F, 12.0F);
        b_9.texOffs(1408, 1228).addBox(12.0F, -86.0F, -64.0F, 36.0F, 12.0F, 12.0F);
        b_9.texOffs(2836, 1062).addBox(-10.0F, -92.0F, -68.0F, 20.0F, 58.0F, 14.0F);
        b_9.texOffs(1816, 812).addBox(-66.0F, -60.0F, -60.0F, 24.0F, 54.0F, 66.0F);
        b_9.texOffs(2000, 812).addBox(42.0F, -60.0F, -60.0F, 24.0F, 54.0F, 66.0F);
        b_9.texOffs(1996, 1160).addBox(-44.0F, -26.0F, -64.0F, 88.0F, 26.0F, 18.0F);
        b_9.texOffs(428, 492).addBox(-68.0F, -108.0F, -68.0F, 136.0F, 22.0F, 136.0F);
        b_9.texOffs(2236, 492).addBox(-60.0F, -138.0F, -60.0F, 120.0F, 34.0F, 120.0F);
        b_9.texOffs(2184, 812).addBox(-50.0F, -152.0F, -50.0F, 100.0F, 18.0F, 100.0F);
        b_9.texOffs(1772, 944).addBox(-16.0F, -168.0F, -42.0F, 32.0F, 18.0F, 90.0F);
        b_9.texOffs(4, 944).addBox(-12.0F, -196.0F, -38.0F, 24.0F, 32.0F, 82.0F);
        b_9.texOffs(352, 1062).addBox(-10.0F, -210.0F, -30.0F, 20.0F, 20.0F, 68.0F);
        b_9.texOffs(2908, 1062).addBox(-12.0F, -190.0F, 38.0F, 24.0F, 48.0F, 22.0F);
        PartDefinition part_head = part_body.addOrReplaceChild("head", b_9, PartPose.offset(0.0F, -288.0F, 0.0F));
        CubeListBuilder b_10 = CubeListBuilder.create();
        b_10.texOffs(1816, 4).addBox(-72.0F, -142.0F, -72.0F, 144.0F, 52.0F, 144.0F);
        PartDefinition part_extra_head = part_head.addOrReplaceChild("extra_head", b_10, PartPose.ZERO);
        CubeListBuilder b_11 = CubeListBuilder.create();
        b_11.texOffs(2680, 300).addBox(-58.0F, -44.0F, -56.0F, 116.0F, 60.0F, 112.0F);
        b_11.texOffs(2576, 662).addBox(-64.0F, -12.0F, -60.0F, 128.0F, 18.0F, 120.0F);
        b_11.texOffs(1028, 944).addBox(-48.0F, -62.0F, -44.0F, 96.0F, 22.0F, 88.0F);
        b_11.texOffs(3752, 1160).addBox(-36.0F, -36.0F, -64.0F, 72.0F, 22.0F, 12.0F);
        b_11.texOffs(1652, 1228).addBox(-22.0F, -31.0F, -68.0F, 44.0F, 12.0F, 8.0F);
        b_11.texOffs(1536, 662).addBox(-46.0F, 12.0F, -38.0F, 92.0F, 68.0F, 76.0F);
        b_11.texOffs(3300, 812).addBox(-52.0F, 70.0F, -44.0F, 104.0F, 28.0F, 88.0F);
        b_11.texOffs(1088, 300).addBox(-48.0F, 96.0F, -44.0F, 96.0F, 88.0F, 88.0F);
        b_11.texOffs(1100, 812).addBox(22.0F, 106.0F, -30.0F, 20.0F, 66.0F, 60.0F);
        b_11.texOffs(2220, 1062).addBox(38.0F, 116.0F, -18.0F, 12.0F, 44.0F, 36.0F);
        b_11.texOffs(220, 944).addBox(-54.0F, 180.0F, -46.0F, 108.0F, 18.0F, 92.0F);
        b_11.texOffs(2588, 812).addBox(-46.0F, 198.0F, -42.0F, 92.0F, 34.0F, 84.0F);
        b_11.texOffs(2772, 1160).addBox(-42.0F, 230.0F, -34.0F, 32.0F, 20.0F, 16.0F);
        b_11.texOffs(4, 1228).addBox(0.0F, 230.0F, -34.0F, 32.0F, 18.0F, 16.0F);
        b_11.texOffs(2872, 1160).addBox(-42.0F, 230.0F, -14.0F, 32.0F, 20.0F, 16.0F);
        b_11.texOffs(104, 1228).addBox(0.0F, 230.0F, -14.0F, 32.0F, 18.0F, 16.0F);
        b_11.texOffs(2972, 1160).addBox(-42.0F, 230.0F, 6.0F, 32.0F, 20.0F, 16.0F);
        b_11.texOffs(204, 1228).addBox(0.0F, 230.0F, 6.0F, 32.0F, 18.0F, 16.0F);
        b_11.texOffs(3072, 1160).addBox(-42.0F, 230.0F, 26.0F, 32.0F, 20.0F, 16.0F);
        b_11.texOffs(304, 1228).addBox(0.0F, 230.0F, 26.0F, 32.0F, 18.0F, 16.0F);
        PartDefinition part_right_arm = part_body.addOrReplaceChild("right_arm", b_11, PartPose.offset(-124.0F, -250.0F, 0.0F));
        CubeListBuilder b_12 = CubeListBuilder.create();
        b_12.texOffs(3140, 300).addBox(-58.0F, -44.0F, -56.0F, 116.0F, 60.0F, 112.0F);
        b_12.texOffs(3076, 662).addBox(-64.0F, -12.0F, -60.0F, 128.0F, 18.0F, 120.0F);
        b_12.texOffs(1400, 944).addBox(-48.0F, -62.0F, -44.0F, 96.0F, 22.0F, 88.0F);
        b_12.texOffs(3924, 1160).addBox(-36.0F, -36.0F, -64.0F, 72.0F, 22.0F, 12.0F);
        b_12.texOffs(1760, 1228).addBox(-22.0F, -31.0F, -68.0F, 44.0F, 12.0F, 8.0F);
        b_12.texOffs(1876, 662).addBox(-46.0F, 12.0F, -38.0F, 92.0F, 68.0F, 76.0F);
        b_12.texOffs(3688, 812).addBox(-52.0F, 70.0F, -44.0F, 104.0F, 28.0F, 88.0F);
        b_12.texOffs(1460, 300).addBox(-48.0F, 96.0F, -44.0F, 96.0F, 88.0F, 88.0F);
        b_12.texOffs(1264, 812).addBox(42.0F, 106.0F, -30.0F, 20.0F, 66.0F, 60.0F);
        b_12.texOffs(2320, 1062).addBox(50.0F, 116.0F, -18.0F, 12.0F, 44.0F, 36.0F);
        b_12.texOffs(624, 944).addBox(-54.0F, 180.0F, -46.0F, 108.0F, 18.0F, 92.0F);
        b_12.texOffs(2944, 812).addBox(-46.0F, 198.0F, -42.0F, 92.0F, 34.0F, 84.0F);
        b_12.texOffs(3172, 1160).addBox(-42.0F, 230.0F, -34.0F, 32.0F, 20.0F, 16.0F);
        b_12.texOffs(404, 1228).addBox(0.0F, 230.0F, -34.0F, 32.0F, 18.0F, 16.0F);
        b_12.texOffs(3272, 1160).addBox(-42.0F, 230.0F, -14.0F, 32.0F, 20.0F, 16.0F);
        b_12.texOffs(504, 1228).addBox(0.0F, 230.0F, -14.0F, 32.0F, 18.0F, 16.0F);
        b_12.texOffs(3372, 1160).addBox(-42.0F, 230.0F, 6.0F, 32.0F, 20.0F, 16.0F);
        b_12.texOffs(604, 1228).addBox(0.0F, 230.0F, 6.0F, 32.0F, 18.0F, 16.0F);
        b_12.texOffs(3472, 1160).addBox(-42.0F, 230.0F, 26.0F, 32.0F, 20.0F, 16.0F);
        b_12.texOffs(704, 1228).addBox(0.0F, 230.0F, 26.0F, 32.0F, 18.0F, 16.0F);
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
