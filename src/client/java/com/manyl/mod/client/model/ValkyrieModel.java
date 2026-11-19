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
 * Реалистичная HD 3D-модель женщины-робота Воительницы (267 скульптурных элементов, 24x геометрия, 4096x4096)
 * с симметричными крыльями, плотными суставами, съёмной бронёй и открытым эстетичным купальником.
 */
public class ValkyrieModel extends EntityModel<GolemRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ManylMod.id("valkyrie_golem"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart helmet;
    private final ModelPart blush;
    private final ModelPart closedEyes;
    private final ModelPart backpack;
    private final ModelPart armorTorso;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart armorRightArm;
    private final ModelPart armorLeftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart armorRightLeg;
    private final ModelPart armorLeftLeg;
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

    public ValkyrieModel(ModelPart root) {
        super(root);
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.helmet = this.head.getChild("helmet");
        this.blush = this.head.getChild("blush");
        this.closedEyes = this.head.getChild("closed_eyes");
        this.backpack = this.body.getChild("backpack");
        this.armorTorso = this.body.getChild("armor_torso");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.armorRightArm = this.rightArm.getChild("armor_right_arm");
        this.armorLeftArm = this.leftArm.getChild("armor_left_arm");
        this.rightLeg = this.root.getChild("right_leg");
        this.leftLeg = this.root.getChild("left_leg");
        this.armorRightLeg = this.rightLeg.getChild("armor_right_leg");
        this.armorLeftLeg = this.leftLeg.getChild("armor_left_leg");
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
        b_0.texOffs(4, 480).addBox(-25.0F, -16.0F, -30.0F, 50.0F, 38.0F, 62.0F);
        b_0.texOffs(256, 584).addBox(-25.0F, 18.0F, -29.0F, 50.0F, 22.0F, 58.0F);
        b_0.texOffs(2636, 584).addBox(-24.0F, 38.0F, -27.0F, 48.0F, 22.0F, 54.0F);
        b_0.texOffs(852, 668).addBox(-22.0F, 58.0F, -25.0F, 44.0F, 22.0F, 50.0F);
        b_0.texOffs(3860, 668).addBox(-20.0F, 78.0F, -23.0F, 40.0F, 22.0F, 46.0F);
        b_0.texOffs(2400, 746).addBox(-17.0F, 98.0F, -21.0F, 34.0F, 18.0F, 42.0F);
        b_0.texOffs(2812, 928).addBox(-13.0F, 100.0F, -24.0F, 26.0F, 14.0F, 7.0F);
        b_0.texOffs(1104, 746).addBox(-18.0F, 114.0F, -21.0F, 36.0F, 20.0F, 44.0F);
        b_0.texOffs(3868, 584).addBox(-19.0F, 132.0F, -21.0F, 38.0F, 26.0F, 48.0F);
        b_0.texOffs(2438, 880).addBox(-16.0F, 136.0F, 20.0F, 32.0F, 24.0F, 9.0F);
        b_0.texOffs(792, 746).addBox(-17.0F, 156.0F, -20.0F, 34.0F, 24.0F, 42.0F);
        b_0.texOffs(1768, 746).addBox(-15.0F, 178.0F, -18.0F, 30.0F, 26.0F, 36.0F);
        b_0.texOffs(3508, 818).addBox(-14.0F, 202.0F, -17.0F, 28.0F, 12.0F, 34.0F);
        b_0.texOffs(3500, 668).addBox(-17.0F, 212.0F, -29.0F, 34.0F, 14.0F, 54.0F);
        b_0.texOffs(2284, 668).addBox(-18.0F, 224.0F, -32.0F, 36.0F, 12.0F, 58.0F);
        b_0.texOffs(3092, 668).addBox(-19.0F, 234.0F, -34.0F, 38.0F, 6.0F, 62.0F);
        b_0.texOffs(530, 880).addBox(-15.0F, 208.0F, -19.0F, 30.0F, 5.0F, 38.0F);
        PartDefinition part_right_leg = part_root.addOrReplaceChild("right_leg", b_0, PartPose.offset(-28.0F, -240.0F, 2.0F));
        CubeListBuilder b_1 = CubeListBuilder.create();
        b_1.texOffs(1996, 4).addBox(-30.0F, -14.0F, -32.0F, 60.0F, 96.0F, 66.0F);
        b_1.texOffs(3444, 204).addBox(-34.0F, 6.0F, -28.0F, 12.0F, 68.0F, 56.0F);
        b_1.texOffs(3292, 584).addBox(-38.0F, 20.0F, -18.0F, 6.0F, 40.0F, 36.0F);
        b_1.texOffs(1654, 880).addBox(-41.0F, 30.0F, -10.0F, 4.0F, 20.0F, 20.0F);
        b_1.texOffs(144, 354).addBox(-29.0F, 76.0F, -30.0F, 58.0F, 56.0F, 60.0F);
        b_1.texOffs(3508, 746).addBox(-27.0F, 84.0F, -37.0F, 54.0F, 42.0F, 16.0F);
        b_1.texOffs(998, 880).addBox(-21.0F, 90.0F, -44.0F, 42.0F, 30.0F, 12.0F);
        b_1.texOffs(2232, 928).addBox(-10.0F, 96.0F, -49.0F, 20.0F, 18.0F, 8.0F);
        b_1.texOffs(1164, 204).addBox(-28.0F, 128.0F, -30.0F, 56.0F, 84.0F, 60.0F);
        b_1.texOffs(3044, 480).addBox(-24.0F, 134.0F, -35.0F, 48.0F, 72.0F, 10.0F);
        b_1.texOffs(1236, 668).addBox(-6.0F, 136.0F, -39.0F, 12.0F, 66.0F, 6.0F);
        b_1.texOffs(3164, 480).addBox(-24.0F, 134.0F, 26.0F, 48.0F, 72.0F, 10.0F);
        b_1.texOffs(2040, 204).addBox(-30.0F, 206.0F, -54.0F, 60.0F, 34.0F, 92.0F);
        b_1.texOffs(2388, 354).addBox(-32.0F, 232.0F, -58.0F, 64.0F, 10.0F, 98.0F);
        b_1.texOffs(4, 880).addBox(-22.0F, 212.0F, -68.0F, 44.0F, 24.0F, 20.0F);
        b_1.texOffs(1944, 928).addBox(-14.0F, 216.0F, -74.0F, 28.0F, 16.0F, 10.0F);
        b_1.texOffs(2030, 880).addBox(-18.0F, 198.0F, -40.0F, 36.0F, 14.0F, 22.0F);
        PartDefinition part_armor_right_leg = part_right_leg.addOrReplaceChild("armor_right_leg", b_1, PartPose.ZERO);
        CubeListBuilder b_2 = CubeListBuilder.create();
        b_2.texOffs(1444, 4).addBox(-33.0F, -10.0F, -35.0F, 66.0F, 92.0F, 70.0F);
        PartDefinition part_extra_legs_right_leg = part_right_leg.addOrReplaceChild("extra_legs_right_leg", b_2, PartPose.ZERO);
        CubeListBuilder b_3 = CubeListBuilder.create();
        b_3.texOffs(460, 204).addBox(-34.0F, 204.0F, -62.0F, 68.0F, 38.0F, 106.0F);
        PartDefinition part_extra_feet_right_leg = part_right_leg.addOrReplaceChild("extra_feet_right_leg", b_3, PartPose.ZERO);
        CubeListBuilder b_4 = CubeListBuilder.create();
        b_4.texOffs(232, 480).addBox(-25.0F, -16.0F, -30.0F, 50.0F, 38.0F, 62.0F);
        b_4.texOffs(476, 584).addBox(-25.0F, 18.0F, -29.0F, 50.0F, 22.0F, 58.0F);
        b_4.texOffs(2844, 584).addBox(-24.0F, 38.0F, -27.0F, 48.0F, 22.0F, 54.0F);
        b_4.texOffs(1044, 668).addBox(-22.0F, 58.0F, -25.0F, 44.0F, 22.0F, 50.0F);
        b_4.texOffs(4, 746).addBox(-20.0F, 78.0F, -23.0F, 40.0F, 22.0F, 46.0F);
        b_4.texOffs(2556, 746).addBox(-17.0F, 98.0F, -21.0F, 34.0F, 18.0F, 42.0F);
        b_4.texOffs(2882, 928).addBox(-13.0F, 100.0F, -24.0F, 26.0F, 14.0F, 7.0F);
        b_4.texOffs(1268, 746).addBox(-18.0F, 114.0F, -21.0F, 36.0F, 20.0F, 44.0F);
        b_4.texOffs(4, 668).addBox(-19.0F, 132.0F, -21.0F, 38.0F, 26.0F, 48.0F);
        b_4.texOffs(2524, 880).addBox(-16.0F, 136.0F, 20.0F, 32.0F, 24.0F, 9.0F);
        b_4.texOffs(948, 746).addBox(-17.0F, 156.0F, -20.0F, 34.0F, 24.0F, 42.0F);
        b_4.texOffs(1904, 746).addBox(-15.0F, 178.0F, -18.0F, 30.0F, 26.0F, 36.0F);
        b_4.texOffs(3636, 818).addBox(-14.0F, 202.0F, -17.0F, 28.0F, 12.0F, 34.0F);
        b_4.texOffs(3680, 668).addBox(-17.0F, 212.0F, -29.0F, 34.0F, 14.0F, 54.0F);
        b_4.texOffs(2476, 668).addBox(-18.0F, 224.0F, -32.0F, 36.0F, 12.0F, 58.0F);
        b_4.texOffs(3296, 668).addBox(-19.0F, 234.0F, -34.0F, 38.0F, 6.0F, 62.0F);
        b_4.texOffs(670, 880).addBox(-15.0F, 208.0F, -19.0F, 30.0F, 5.0F, 38.0F);
        PartDefinition part_left_leg = part_root.addOrReplaceChild("left_leg", b_4, PartPose.offset(28.0F, -240.0F, 2.0F));
        CubeListBuilder b_5 = CubeListBuilder.create();
        b_5.texOffs(2252, 4).addBox(-30.0F, -14.0F, -32.0F, 60.0F, 96.0F, 66.0F);
        b_5.texOffs(3584, 204).addBox(22.0F, 6.0F, -28.0F, 12.0F, 68.0F, 56.0F);
        b_5.texOffs(3380, 584).addBox(32.0F, 20.0F, -18.0F, 6.0F, 40.0F, 36.0F);
        b_5.texOffs(1706, 880).addBox(37.0F, 30.0F, -10.0F, 4.0F, 20.0F, 20.0F);
        b_5.texOffs(384, 354).addBox(-29.0F, 76.0F, -30.0F, 58.0F, 56.0F, 60.0F);
        b_5.texOffs(3652, 746).addBox(-27.0F, 84.0F, -37.0F, 54.0F, 42.0F, 16.0F);
        b_5.texOffs(1110, 880).addBox(-21.0F, 90.0F, -44.0F, 42.0F, 30.0F, 12.0F);
        b_5.texOffs(2292, 928).addBox(-10.0F, 96.0F, -49.0F, 20.0F, 18.0F, 8.0F);
        b_5.texOffs(1400, 204).addBox(-28.0F, 128.0F, -30.0F, 56.0F, 84.0F, 60.0F);
        b_5.texOffs(3284, 480).addBox(-24.0F, 134.0F, -35.0F, 48.0F, 72.0F, 10.0F);
        b_5.texOffs(1276, 668).addBox(-6.0F, 136.0F, -39.0F, 12.0F, 66.0F, 6.0F);
        b_5.texOffs(3404, 480).addBox(-24.0F, 134.0F, 26.0F, 48.0F, 72.0F, 10.0F);
        b_5.texOffs(2348, 204).addBox(-30.0F, 206.0F, -54.0F, 60.0F, 34.0F, 92.0F);
        b_5.texOffs(2716, 354).addBox(-32.0F, 232.0F, -58.0F, 64.0F, 10.0F, 98.0F);
        b_5.texOffs(136, 880).addBox(-22.0F, 212.0F, -68.0F, 44.0F, 24.0F, 20.0F);
        b_5.texOffs(2024, 928).addBox(-14.0F, 216.0F, -74.0F, 28.0F, 16.0F, 10.0F);
        b_5.texOffs(2150, 880).addBox(-18.0F, 198.0F, -40.0F, 36.0F, 14.0F, 22.0F);
        PartDefinition part_armor_left_leg = part_left_leg.addOrReplaceChild("armor_left_leg", b_5, PartPose.ZERO);
        CubeListBuilder b_6 = CubeListBuilder.create();
        b_6.texOffs(1720, 4).addBox(-33.0F, -10.0F, -35.0F, 66.0F, 92.0F, 70.0F);
        PartDefinition part_extra_legs_left_leg = part_left_leg.addOrReplaceChild("extra_legs_left_leg", b_6, PartPose.ZERO);
        CubeListBuilder b_7 = CubeListBuilder.create();
        b_7.texOffs(812, 204).addBox(-34.0F, 204.0F, -62.0F, 68.0F, 38.0F, 106.0F);
        PartDefinition part_extra_feet_left_leg = part_left_leg.addOrReplaceChild("extra_feet_left_leg", b_7, PartPose.ZERO);
        CubeListBuilder b_8 = CubeListBuilder.create();
        b_8.texOffs(1334, 880).addBox(-13.0F, -290.0F, -13.0F, 26.0F, 16.0F, 26.0F);
        b_8.texOffs(2552, 818).addBox(-13.0F, -276.0F, -13.0F, 26.0F, 24.0F, 26.0F);
        b_8.texOffs(3156, 818).addBox(-26.0F, -262.0F, -18.0F, 52.0F, 10.0F, 36.0F);
        b_8.texOffs(556, 818).addBox(-42.0F, -254.0F, -21.0F, 84.0F, 14.0F, 42.0F);
        b_8.texOffs(3232, 746).addBox(-46.0F, -242.0F, -22.0F, 92.0F, 14.0F, 44.0F);
        b_8.texOffs(2140, 746).addBox(-41.0F, -230.0F, -23.0F, 82.0F, 14.0F, 46.0F);
        b_8.texOffs(158, 962).addBox(-5.0F, -246.0F, -23.0F, 10.0F, 7.0F, 3.0F);
        b_8.texOffs(4, 584).addBox(-39.0F, -218.0F, -24.0F, 78.0F, 34.0F, 46.0F);
        b_8.texOffs(3708, 928).addBox(-37.0F, -224.0F, -30.0F, 32.0F, 8.0F, 8.0F);
        b_8.texOffs(3764, 818).addBox(-38.0F, -218.0F, -34.0F, 34.0F, 34.0F, 12.0F);
        b_8.texOffs(1758, 880).addBox(-36.0F, -216.0F, -40.0F, 30.0F, 30.0F, 8.0F);
        b_8.texOffs(3802, 880).addBox(-34.0F, -213.0F, -45.0F, 26.0F, 24.0F, 7.0F);
        b_8.texOffs(2512, 928).addBox(-32.0F, -210.0F, -49.0F, 22.0F, 18.0F, 5.0F);
        b_8.texOffs(4, 962).addBox(-29.0F, -206.0F, -52.0F, 16.0F, 11.0F, 4.0F);
        b_8.texOffs(3792, 928).addBox(5.0F, -224.0F, -30.0F, 32.0F, 8.0F, 8.0F);
        b_8.texOffs(3860, 818).addBox(4.0F, -218.0F, -34.0F, 34.0F, 34.0F, 12.0F);
        b_8.texOffs(1838, 880).addBox(6.0F, -216.0F, -40.0F, 30.0F, 30.0F, 8.0F);
        b_8.texOffs(3872, 880).addBox(8.0F, -213.0F, -45.0F, 26.0F, 24.0F, 7.0F);
        b_8.texOffs(2570, 928).addBox(10.0F, -210.0F, -49.0F, 22.0F, 18.0F, 5.0F);
        b_8.texOffs(48, 962).addBox(13.0F, -206.0F, -52.0F, 16.0F, 11.0F, 4.0F);
        b_8.texOffs(3264, 928).addBox(-4.0F, -212.0F, -37.0F, 8.0F, 14.0F, 6.0F);
        b_8.texOffs(134, 962).addBox(-3.0F, -209.0F, -41.0F, 6.0F, 7.0F, 4.0F);
        b_8.texOffs(3748, 480).addBox(-32.0F, -254.0F, -22.0F, 6.0F, 38.0F, 44.0F);
        b_8.texOffs(3852, 480).addBox(26.0F, -254.0F, -22.0F, 6.0F, 38.0F, 44.0F);
        b_8.texOffs(1176, 818).addBox(-39.0F, -186.0F, -26.0F, 78.0F, 5.0F, 50.0F);
        b_8.texOffs(1436, 818).addBox(-35.0F, -182.0F, -22.0F, 70.0F, 12.0F, 42.0F);
        b_8.texOffs(2156, 818).addBox(-31.0F, -172.0F, -20.0F, 62.0F, 12.0F, 38.0F);
        b_8.texOffs(2724, 818).addBox(-28.0F, -162.0F, -19.0F, 56.0F, 14.0F, 34.0F);
        b_8.texOffs(3336, 818).addBox(-26.0F, -150.0F, -18.0F, 52.0F, 14.0F, 32.0F);
        b_8.texOffs(352, 880).addBox(-27.0F, -138.0F, -18.5F, 54.0F, 10.0F, 33.0F);
        b_8.texOffs(2360, 818).addBox(-29.0F, -130.0F, -19.0F, 58.0F, 14.0F, 36.0F);
        b_8.texOffs(812, 818).addBox(-33.0F, -118.0F, -21.0F, 66.0F, 14.0F, 42.0F);
        b_8.texOffs(1516, 746).addBox(-38.0F, -106.0F, -23.0F, 76.0F, 14.0F, 48.0F);
        b_8.texOffs(2804, 668).addBox(-44.0F, -94.0F, -25.0F, 88.0F, 14.0F, 54.0F);
        b_8.texOffs(188, 962).addBox(-3.0F, -124.0F, -20.0F, 6.0F, 7.0F, 3.0F);
        b_8.texOffs(2316, 584).addBox(-48.0F, -82.0F, -29.0F, 96.0F, 14.0F, 62.0F);
        b_8.texOffs(1696, 480).addBox(-52.0F, -70.0F, -31.0F, 104.0F, 18.0F, 68.0F);
        b_8.texOffs(2044, 480).addBox(-51.0F, -54.0F, -30.0F, 102.0F, 18.0F, 68.0F);
        b_8.texOffs(696, 584).addBox(-48.0F, -38.0F, -28.0F, 96.0F, 16.0F, 62.0F);
        b_8.texOffs(584, 746).addBox(-26.0F, -24.0F, -24.0F, 52.0F, 16.0F, 50.0F);
        b_8.texOffs(1664, 818).addBox(-46.0F, -74.0F, 26.0F, 92.0F, 42.0F, 12.0F);
        b_8.texOffs(810, 880).addBox(-42.0F, -68.0F, 34.0F, 84.0F, 34.0F, 8.0F);
        b_8.texOffs(4, 928).addBox(-36.0F, -62.0F, 39.0F, 72.0F, 24.0F, 6.0F);
        b_8.texOffs(1316, 668).addBox(-50.0F, -82.0F, -30.0F, 100.0F, 6.0F, 64.0F);
        b_8.texOffs(92, 962).addBox(-7.0F, -84.0F, -33.0F, 14.0F, 10.0F, 5.0F);
        PartDefinition part_body = part_root.addOrReplaceChild("body", b_8, PartPose.offset(0.0F, -240.0F, 0.0F));
        CubeListBuilder b_9 = CubeListBuilder.create();
        b_9.texOffs(1432, 354).addBox(-64.0F, -260.0F, -44.0F, 128.0F, 22.0F, 88.0F);
        b_9.texOffs(536, 4).addBox(-70.0F, -244.0F, -48.0F, 140.0F, 68.0F, 96.0F);
        b_9.texOffs(180, 746).addBox(-62.0F, -236.0F, -56.0F, 56.0F, 54.0F, 14.0F);
        b_9.texOffs(324, 746).addBox(6.0F, -236.0F, -56.0F, 56.0F, 54.0F, 14.0F);
        b_9.texOffs(2668, 668).addBox(-26.0F, -226.0F, -62.0F, 52.0F, 56.0F, 14.0F);
        b_9.texOffs(3956, 818).addBox(-16.0F, -216.0F, -68.0F, 32.0F, 36.0F, 10.0F);
        b_9.texOffs(1012, 928).addBox(-10.0F, -210.0F, -72.0F, 20.0F, 24.0F, 6.0F);
        b_9.texOffs(3044, 354).addBox(-56.0F, -180.0F, -38.0F, 112.0F, 30.0F, 76.0F);
        b_9.texOffs(3736, 4).addBox(-42.0F, -152.0F, -31.0F, 84.0F, 84.0F, 62.0F);
        b_9.texOffs(1016, 584).addBox(-44.0F, -140.0F, -34.0F, 88.0F, 10.0F, 68.0F);
        b_9.texOffs(1332, 584).addBox(-44.0F, -112.0F, -34.0F, 88.0F, 10.0F, 68.0F);
        b_9.texOffs(1648, 584).addBox(-44.0F, -84.0F, -34.0F, 88.0F, 10.0F, 68.0F);
        b_9.texOffs(1636, 204).addBox(-56.0F, -84.0F, -38.0F, 112.0F, 40.0F, 88.0F);
        b_9.texOffs(1222, 880).addBox(-20.0F, -72.0F, -44.0F, 40.0F, 28.0F, 14.0F);
        b_9.texOffs(2752, 928).addBox(-10.0F, -64.0F, -48.0F, 20.0F, 14.0F, 8.0F);
        b_9.texOffs(2508, 4).addBox(-74.0F, -46.0F, -46.0F, 148.0F, 60.0F, 96.0F);
        b_9.texOffs(4, 354).addBox(-28.0F, -42.0F, -52.0F, 56.0F, 110.0F, 12.0F);
        b_9.texOffs(1068, 928).addBox(-32.0F, 62.0F, -54.0F, 64.0F, 14.0F, 14.0F);
        b_9.texOffs(828, 354).addBox(-28.0F, -42.0F, 44.0F, 56.0F, 104.0F, 12.0F);
        b_9.texOffs(1228, 928).addBox(-32.0F, 56.0F, 42.0F, 64.0F, 14.0F, 14.0F);
        b_9.texOffs(1012, 4).addBox(-90.0F, -40.0F, -44.0F, 18.0F, 76.0F, 88.0F);
        b_9.texOffs(4, 204).addBox(-98.0F, 12.0F, -48.0F, 16.0F, 50.0F, 96.0F);
        b_9.texOffs(968, 354).addBox(-98.0F, 56.0F, -50.0F, 14.0F, 14.0F, 100.0F);
        b_9.texOffs(2712, 746).addBox(-98.0F, 20.0F, -16.0F, 10.0F, 28.0F, 32.0F);
        b_9.texOffs(1228, 4).addBox(72.0F, -40.0F, -44.0F, 18.0F, 76.0F, 88.0F);
        b_9.texOffs(232, 204).addBox(82.0F, 12.0F, -48.0F, 16.0F, 50.0F, 96.0F);
        b_9.texOffs(1200, 354).addBox(84.0F, 56.0F, -50.0F, 14.0F, 14.0F, 100.0F);
        b_9.texOffs(2800, 746).addBox(88.0F, 20.0F, -16.0F, 10.0F, 28.0F, 32.0F);
        PartDefinition part_armor_torso = part_body.addOrReplaceChild("armor_torso", b_9, PartPose.ZERO);
        CubeListBuilder b_10 = CubeListBuilder.create();
        b_10.texOffs(4, 4).addBox(-76.0F, -248.0F, -56.0F, 152.0F, 84.0F, 112.0F);
        PartDefinition part_extra_chest = part_body.addOrReplaceChild("extra_chest", b_10, PartPose.ZERO);
        CubeListBuilder b_11 = CubeListBuilder.create();
        b_11.texOffs(624, 354).addBox(-36.0F, -42.0F, -8.0F, 72.0F, 88.0F, 28.0F);
        b_11.texOffs(468, 746).addBox(-22.0F, -26.0F, 16.0F, 44.0F, 56.0F, 12.0F);
        b_11.texOffs(1586, 880).addBox(-12.0F, -14.0F, 24.0F, 24.0F, 32.0F, 8.0F);
        b_11.texOffs(4, 818).addBox(-64.0F, -52.0F, 6.0F, 40.0F, 42.0F, 16.0F);
        b_11.texOffs(3796, 746).addBox(-104.0F, -88.0F, 12.0F, 48.0F, 44.0F, 14.0F);
        b_11.texOffs(1876, 818).addBox(-146.0F, -122.0F, 16.0F, 54.0F, 40.0F, 14.0F);
        b_11.texOffs(3322, 880).addBox(-202.0F, -158.0F, 20.0F, 68.0F, 22.0F, 10.0F);
        b_11.texOffs(2952, 928).addBox(-202.0F, -154.0F, 22.0F, 18.0F, 14.0F, 6.0F);
        b_11.texOffs(2610, 880).addBox(-198.0F, -122.0F, 18.0F, 78.0F, 22.0F, 10.0F);
        b_11.texOffs(3004, 928).addBox(-198.0F, -118.0F, 20.0F, 18.0F, 14.0F, 6.0F);
        b_11.texOffs(2970, 880).addBox(-172.0F, -84.0F, 16.0F, 76.0F, 22.0F, 10.0F);
        b_11.texOffs(3056, 928).addBox(-172.0F, -80.0F, 18.0F, 18.0F, 14.0F, 6.0F);
        b_11.texOffs(164, 928).addBox(-138.0F, -48.0F, 14.0F, 68.0F, 20.0F, 10.0F);
        b_11.texOffs(3388, 928).addBox(-138.0F, -44.0F, 16.0F, 18.0F, 12.0F, 6.0F);
        b_11.texOffs(484, 928).addBox(-102.0F, -16.0F, 10.0F, 56.0F, 20.0F, 10.0F);
        b_11.texOffs(3440, 928).addBox(-102.0F, -12.0F, 12.0F, 18.0F, 12.0F, 6.0F);
        b_11.texOffs(1388, 928).addBox(-74.0F, 14.0F, 8.0F, 44.0F, 18.0F, 10.0F);
        b_11.texOffs(3876, 928).addBox(-74.0F, 18.0F, 10.0F, 18.0F, 10.0F, 6.0F);
        b_11.texOffs(120, 818).addBox(24.0F, -52.0F, 6.0F, 40.0F, 42.0F, 16.0F);
        b_11.texOffs(3924, 746).addBox(56.0F, -88.0F, 12.0F, 48.0F, 44.0F, 14.0F);
        b_11.texOffs(2016, 818).addBox(92.0F, -122.0F, 16.0F, 54.0F, 40.0F, 14.0F);
        b_11.texOffs(3482, 880).addBox(134.0F, -158.0F, 20.0F, 68.0F, 22.0F, 10.0F);
        b_11.texOffs(3108, 928).addBox(184.0F, -154.0F, 22.0F, 18.0F, 14.0F, 6.0F);
        b_11.texOffs(2790, 880).addBox(120.0F, -122.0F, 18.0F, 78.0F, 22.0F, 10.0F);
        b_11.texOffs(3160, 928).addBox(180.0F, -118.0F, 20.0F, 18.0F, 14.0F, 6.0F);
        b_11.texOffs(3146, 880).addBox(96.0F, -84.0F, 16.0F, 76.0F, 22.0F, 10.0F);
        b_11.texOffs(3212, 928).addBox(154.0F, -80.0F, 18.0F, 18.0F, 14.0F, 6.0F);
        b_11.texOffs(324, 928).addBox(70.0F, -48.0F, 14.0F, 68.0F, 20.0F, 10.0F);
        b_11.texOffs(3492, 928).addBox(120.0F, -44.0F, 16.0F, 18.0F, 12.0F, 6.0F);
        b_11.texOffs(620, 928).addBox(46.0F, -16.0F, 10.0F, 56.0F, 20.0F, 10.0F);
        b_11.texOffs(3544, 928).addBox(84.0F, -12.0F, 12.0F, 18.0F, 12.0F, 6.0F);
        b_11.texOffs(1500, 928).addBox(30.0F, 14.0F, 8.0F, 44.0F, 18.0F, 10.0F);
        b_11.texOffs(3928, 928).addBox(56.0F, 18.0F, 10.0F, 18.0F, 10.0F, 6.0F);
        PartDefinition part_backpack = part_body.addOrReplaceChild("backpack", b_11, PartPose.offset(0.0F, -190.0F, 44.0F));
        CubeListBuilder b_12 = CubeListBuilder.create();
        b_12.texOffs(3436, 4).addBox(-36.0F, -86.0F, -38.0F, 72.0F, 74.0F, 76.0F);
        b_12.texOffs(2388, 480).addBox(-30.0F, -14.0F, -36.0F, 60.0F, 14.0F, 72.0F);
        b_12.texOffs(1648, 668).addBox(-22.0F, -4.0F, -34.0F, 44.0F, 6.0F, 64.0F);
        b_12.texOffs(3524, 480).addBox(-40.0F, -52.0F, -39.0F, 10.0F, 38.0F, 44.0F);
        b_12.texOffs(3636, 480).addBox(30.0F, -52.0F, -39.0F, 10.0F, 38.0F, 44.0F);
        b_12.texOffs(2908, 818).addBox(-43.0F, -64.0F, -12.0F, 6.0F, 24.0F, 24.0F);
        b_12.texOffs(2972, 818).addBox(37.0F, -64.0F, -12.0F, 6.0F, 24.0F, 24.0F);
        b_12.texOffs(3328, 928).addBox(-45.0F, -56.0F, -5.0F, 3.0F, 10.0F, 10.0F);
        b_12.texOffs(3358, 928).addBox(42.0F, -56.0F, -5.0F, 3.0F, 10.0F, 10.0F);
        b_12.texOffs(3980, 928).addBox(-29.0F, -54.0F, -40.0F, 20.0F, 11.0F, 4.0F);
        b_12.texOffs(4032, 928).addBox(9.0F, -54.0F, -40.0F, 20.0F, 11.0F, 4.0F);
        b_12.texOffs(210, 962).addBox(-31.0F, -61.0F, -41.0F, 22.0F, 4.0F, 5.0F);
        b_12.texOffs(268, 962).addBox(9.0F, -61.0F, -41.0F, 22.0F, 4.0F, 5.0F);
        b_12.texOffs(3640, 354).addBox(-40.0F, -94.0F, -40.0F, 80.0F, 20.0F, 82.0F);
        b_12.texOffs(3424, 354).addBox(-42.0F, -84.0F, 24.0F, 84.0F, 82.0F, 22.0F);
        b_12.texOffs(1732, 928).addBox(-38.0F, -82.0F, -43.0F, 18.0F, 18.0F, 9.0F);
        b_12.texOffs(2628, 928).addBox(-20.0F, -84.0F, -44.0F, 20.0F, 13.0F, 9.0F);
        b_12.texOffs(2690, 928).addBox(0.0F, -84.0F, -44.0F, 20.0F, 13.0F, 9.0F);
        b_12.texOffs(1790, 928).addBox(20.0F, -82.0F, -43.0F, 18.0F, 18.0F, 9.0F);
        b_12.texOffs(616, 480).addBox(-44.0F, -80.0F, -28.0F, 9.0F, 60.0F, 36.0F);
        b_12.texOffs(710, 480).addBox(35.0F, -80.0F, -28.0F, 9.0F, 60.0F, 36.0F);
        b_12.texOffs(524, 668).addBox(-20.0F, -76.0F, 40.0F, 40.0F, 56.0F, 18.0F);
        b_12.texOffs(2040, 746).addBox(-16.0F, -22.0F, 46.0F, 32.0F, 46.0F, 16.0F);
        b_12.texOffs(756, 928).addBox(-18.0F, 20.0F, 50.0F, 36.0F, 12.0F, 18.0F);
        b_12.texOffs(1432, 746).addBox(-13.0F, 30.0F, 52.0F, 26.0F, 50.0F, 14.0F);
        b_12.texOffs(1848, 928).addBox(-15.0F, 76.0F, 54.0F, 30.0F, 10.0F, 16.0F);
        b_12.texOffs(2660, 818).addBox(-9.0F, 84.0F, 56.0F, 18.0F, 38.0F, 12.0F);
        PartDefinition part_head = part_body.addOrReplaceChild("head", b_12, PartPose.offset(0.0F, -284.0F, 0.0F));
        CubeListBuilder b_13 = CubeListBuilder.create();
        b_13.texOffs(326, 962).addBox(-30.0F, -39.0F, -39.0F, 13.0F, 5.0F, 2.0F);
        b_13.texOffs(360, 962).addBox(17.0F, -39.0F, -39.0F, 13.0F, 5.0F, 2.0F);
        PartDefinition part_blush = part_head.addOrReplaceChild("blush", b_13, PartPose.ZERO);
        CubeListBuilder b_14 = CubeListBuilder.create();
        b_14.texOffs(3596, 928).addBox(-30.0F, -55.0F, -41.0F, 22.0F, 13.0F, 4.0F);
        b_14.texOffs(3652, 928).addBox(8.0F, -55.0F, -41.0F, 22.0F, 13.0F, 4.0F);
        PartDefinition part_closed_eyes = part_head.addOrReplaceChild("closed_eyes", b_14, PartPose.ZERO);
        CubeListBuilder b_15 = CubeListBuilder.create();
        b_15.texOffs(3040, 204).addBox(-50.0F, -102.0F, -50.0F, 100.0F, 24.0F, 100.0F);
        b_15.texOffs(3724, 204).addBox(-46.0F, -128.0F, -46.0F, 92.0F, 30.0F, 92.0F);
        b_15.texOffs(804, 480).addBox(-38.0F, -140.0F, -38.0F, 76.0F, 16.0F, 76.0F);
        b_15.texOffs(268, 880).addBox(-14.0F, -120.0F, -56.0F, 28.0F, 32.0F, 12.0F);
        b_15.texOffs(2464, 928).addBox(-8.0F, -114.0F, -60.0F, 16.0F, 20.0F, 6.0F);
        b_15.texOffs(3296, 928).addBox(-4.0F, -84.0F, -50.0F, 8.0F, 14.0F, 6.0F);
        b_15.texOffs(2656, 480).addBox(-12.0F, -154.0F, -32.0F, 24.0F, 18.0F, 68.0F);
        b_15.texOffs(460, 480).addBox(-8.0F, -188.0F, -26.0F, 16.0F, 36.0F, 60.0F);
        b_15.texOffs(3052, 584).addBox(-64.0F, -104.0F, -18.0F, 18.0F, 36.0F, 40.0F);
        b_15.texOffs(644, 668).addBox(-76.0F, -136.0F, -4.0F, 18.0F, 42.0F, 32.0F);
        b_15.texOffs(3064, 746).addBox(-84.0F, -164.0F, 10.0F, 16.0F, 36.0F, 24.0F);
        b_15.texOffs(1918, 880).addBox(-86.0F, -180.0F, 16.0F, 10.0F, 22.0F, 16.0F);
        b_15.texOffs(3172, 584).addBox(46.0F, -104.0F, -18.0F, 18.0F, 36.0F, 40.0F);
        b_15.texOffs(748, 668).addBox(58.0F, -136.0F, -4.0F, 18.0F, 42.0F, 32.0F);
        b_15.texOffs(3148, 746).addBox(68.0F, -164.0F, 10.0F, 16.0F, 36.0F, 24.0F);
        b_15.texOffs(1974, 880).addBox(76.0F, -180.0F, 16.0F, 10.0F, 22.0F, 16.0F);
        PartDefinition part_helmet = part_head.addOrReplaceChild("helmet", b_15, PartPose.ZERO);
        CubeListBuilder b_16 = CubeListBuilder.create();
        b_16.texOffs(3000, 4).addBox(-54.0F, -132.0F, -54.0F, 108.0F, 42.0F, 108.0F);
        PartDefinition part_extra_head = part_head.addOrReplaceChild("extra_head", b_16, PartPose.ZERO);
        CubeListBuilder b_17 = CubeListBuilder.create();
        b_17.texOffs(3642, 880).addBox(-6.0F, -6.0F, -10.0F, 18.0F, 12.0F, 20.0F);
        b_17.texOffs(2270, 880).addBox(-9.0F, -8.0F, -11.0F, 18.0F, 14.0F, 22.0F);
        b_17.texOffs(2888, 746).addBox(-10.0F, 4.0F, -11.0F, 20.0F, 38.0F, 22.0F);
        b_17.texOffs(236, 818).addBox(-9.0F, 40.0F, -10.0F, 18.0F, 38.0F, 20.0F);
        b_17.texOffs(868, 928).addBox(-8.0F, 76.0F, -9.0F, 16.0F, 12.0F, 18.0F);
        b_17.texOffs(316, 818).addBox(-12.0F, 86.0F, -10.0F, 18.0F, 38.0F, 20.0F);
        b_17.texOffs(1032, 818).addBox(-13.0F, 122.0F, -9.0F, 16.0F, 38.0F, 18.0F);
        b_17.texOffs(2104, 928).addBox(-12.0F, 158.0F, -8.0F, 14.0F, 10.0F, 16.0F);
        b_17.texOffs(1442, 880).addBox(-13.0F, 166.0F, -9.0F, 16.0F, 24.0F, 18.0F);
        b_17.texOffs(2352, 928).addBox(-11.0F, 188.0F, -7.0F, 12.0F, 12.0F, 14.0F);
        PartDefinition part_right_arm = part_body.addOrReplaceChild("right_arm", b_17, PartPose.offset(-54.0F, -238.0F, 0.0F));
        CubeListBuilder b_18 = CubeListBuilder.create();
        b_18.texOffs(1868, 354).addBox(-28.0F, -30.0F, -34.0F, 60.0F, 42.0F, 68.0F);
        b_18.texOffs(1112, 480).addBox(-32.0F, -6.0F, -38.0F, 68.0F, 14.0F, 76.0F);
        b_18.texOffs(180, 668).addBox(-48.0F, -44.0F, -26.0F, 32.0F, 22.0F, 52.0F);
        b_18.texOffs(1612, 928).addBox(-40.0F, -26.0F, -42.0F, 18.0F, 18.0F, 10.0F);
        b_18.texOffs(3468, 584).addBox(-22.0F, 74.0F, -26.0F, 46.0F, 22.0F, 52.0F);
        b_18.texOffs(2656, 204).addBox(-20.0F, 94.0F, -26.0F, 42.0F, 74.0F, 52.0F);
        b_18.texOffs(2844, 480).addBox(-30.0F, 104.0F, -18.0F, 12.0F, 48.0F, 36.0F);
        b_18.texOffs(3036, 818).addBox(-32.0F, 114.0F, -10.0F, 8.0F, 28.0F, 20.0F);
        b_18.texOffs(1868, 668).addBox(-22.0F, 164.0F, -28.0F, 46.0F, 14.0F, 56.0F);
        b_18.texOffs(1964, 584).addBox(-18.0F, 176.0F, -24.0F, 38.0F, 30.0F, 48.0F);
        PartDefinition part_armor_right_arm = part_right_arm.addOrReplaceChild("armor_right_arm", b_18, PartPose.ZERO);
        CubeListBuilder b_19 = CubeListBuilder.create();
        b_19.texOffs(3722, 880).addBox(-12.0F, -6.0F, -10.0F, 18.0F, 12.0F, 20.0F);
        b_19.texOffs(2354, 880).addBox(-9.0F, -8.0F, -11.0F, 18.0F, 14.0F, 22.0F);
        b_19.texOffs(2976, 746).addBox(-10.0F, 4.0F, -11.0F, 20.0F, 38.0F, 22.0F);
        b_19.texOffs(396, 818).addBox(-9.0F, 40.0F, -10.0F, 18.0F, 38.0F, 20.0F);
        b_19.texOffs(940, 928).addBox(-8.0F, 76.0F, -9.0F, 16.0F, 12.0F, 18.0F);
        b_19.texOffs(476, 818).addBox(-6.0F, 86.0F, -10.0F, 18.0F, 38.0F, 20.0F);
        b_19.texOffs(1104, 818).addBox(-3.0F, 122.0F, -9.0F, 16.0F, 38.0F, 18.0F);
        b_19.texOffs(2168, 928).addBox(-2.0F, 158.0F, -8.0F, 14.0F, 10.0F, 16.0F);
        b_19.texOffs(1514, 880).addBox(-3.0F, 166.0F, -9.0F, 16.0F, 24.0F, 18.0F);
        b_19.texOffs(2408, 928).addBox(-1.0F, 188.0F, -7.0F, 12.0F, 12.0F, 14.0F);
        PartDefinition part_left_arm = part_body.addOrReplaceChild("left_arm", b_19, PartPose.offset(54.0F, -238.0F, 0.0F));
        CubeListBuilder b_20 = CubeListBuilder.create();
        b_20.texOffs(2128, 354).addBox(-28.0F, -30.0F, -34.0F, 60.0F, 42.0F, 68.0F);
        b_20.texOffs(1404, 480).addBox(-32.0F, -6.0F, -38.0F, 68.0F, 14.0F, 76.0F);
        b_20.texOffs(352, 668).addBox(16.0F, -44.0F, -26.0F, 32.0F, 22.0F, 52.0F);
        b_20.texOffs(1672, 928).addBox(22.0F, -26.0F, -42.0F, 18.0F, 18.0F, 10.0F);
        b_20.texOffs(3668, 584).addBox(-22.0F, 74.0F, -26.0F, 46.0F, 22.0F, 52.0F);
        b_20.texOffs(2848, 204).addBox(-20.0F, 94.0F, -26.0F, 42.0F, 74.0F, 52.0F);
        b_20.texOffs(2944, 480).addBox(18.0F, 104.0F, -18.0F, 12.0F, 48.0F, 36.0F);
        b_20.texOffs(3096, 818).addBox(24.0F, 114.0F, -10.0F, 8.0F, 28.0F, 20.0F);
        b_20.texOffs(2076, 668).addBox(-22.0F, 164.0F, -28.0F, 46.0F, 14.0F, 56.0F);
        b_20.texOffs(2140, 584).addBox(-18.0F, 176.0F, -24.0F, 38.0F, 30.0F, 48.0F);
        PartDefinition part_armor_left_arm = part_left_arm.addOrReplaceChild("armor_left_arm", b_20, PartPose.ZERO);
        return LayerDefinition.create(mesh, 4096, 4096);
    }

    private static void setPartScaleAndFade(ModelPart part, float progress) {
        if (progress <= 0.0F) {
            part.visible = true;
            part.xScale = 1.0F;
            part.yScale = 1.0F;
            part.zScale = 1.0F;
            return;
        }
        if (progress >= 0.95F) {
            part.visible = false;
            return;
        }
        part.visible = true;
        float sc = Math.max(0.02F, 1.0F - progress);
        part.xScale = sc;
        part.yScale = sc;
        part.zScale = sc;
        part.y -= 28.0F * progress;
    }

    private static float smooth01(float x) {
        float t = Mth.clamp(x, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float bell(float t, float start, float peakIn, float peakOut, float end) {
        if (t <= start || t >= end) return 0.0F;
        if (t < peakIn) return smooth01((t - start) / Math.max(0.001F, peakIn - start));
        if (t > peakOut) return 1.0F - smooth01((t - peakOut) / Math.max(0.001F, end - peakOut));
        return 1.0F;
    }

    @Override
    public void setupAnim(GolemRenderState state) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean swimsuitReady = state.bathPhase >= 3 || (state.bathPhase == 2 && state.bathTicks >= 45);
        boolean showBaseArmor = !swimsuitReady;

        this.helmet.visible = showBaseArmor;
        this.armorTorso.visible = showBaseArmor;
        this.backpack.visible = showBaseArmor;
        this.armorRightArm.visible = showBaseArmor;
        this.armorLeftArm.visible = showBaseArmor;
        this.armorRightLeg.visible = showBaseArmor;
        this.armorLeftLeg.visible = showBaseArmor;

        this.extraHead.visible = state.hasHeadArmor && showBaseArmor;
        this.extraChest.visible = state.hasChestArmor && showBaseArmor;
        this.extraLegsR.visible = state.hasLegsArmor && showBaseArmor;
        this.extraLegsL.visible = state.hasLegsArmor && showBaseArmor;
        this.extraFeetR.visible = state.hasFeetArmor && showBaseArmor;
        this.extraFeetL.visible = state.hasFeetArmor && showBaseArmor;

        this.blush.visible = false;
        this.closedEyes.visible = false;

        if (state.isDisabled) {
            this.closedEyes.visible = true;
            this.body.xRot = 0.22F;
            this.head.xRot = 0.62F;
            this.head.yRot = 0.14F;
            this.rightArm.xRot = -0.15F;
            this.leftArm.xRot = -0.15F;
            return;
        }

        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.idleAnim.apply(state.idleAnimationState, state.ageInTicks, 1.0F);
        if (state.bathPhase != 4) {
            this.walkAnim.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 2.2F, 2.4F);
        }
        if (state.interactionAnim == 0) {
            this.workAnim.apply(state.workAnimationState, state.ageInTicks, 1.0F);
            this.attackAnim.apply(state.attackAnimationState, state.ageInTicks, 1.0F);
            this.carryAnim.apply(state.carryAnimationState, state.ageInTicks, 1.0F);
            this.sitAnim.apply(state.sitAnimationState, state.ageInTicks, 1.0F);
            this.summonAnim.apply(state.summonAnimationState, state.ageInTicks, 1.0F);
            this.eatAnim.apply(state.eatAnimationState, state.ageInTicks, 1.0F);
        }

        if (state.interactionAnim != 0) {
            float t = state.interactionTicks;
            switch (state.interactionAnim) {
                case 1 -> { // ANIM_PAT: ласковое поглаживание по голове (45 тиков)
                    float env = bell(t, 0.0F, 9.0F, 35.0F, 45.0F);
                    float wiggle = Mth.sin(t * 0.42F);
                    this.blush.visible = env > 0.15F;
                    this.closedEyes.visible = t >= 6.0F && t <= 38.0F;
                    this.head.xRot = Mth.lerp(env, this.head.xRot, -0.15F + 0.05F * wiggle);
                    this.head.zRot += 0.22F * wiggle * env;
                    this.body.xRot += 0.06F * env;
                    this.rightArm.xRot = Mth.lerp(env, this.rightArm.xRot, -0.46F);
                    this.leftArm.xRot = Mth.lerp(env, this.leftArm.xRot, -0.46F);
                    this.rightArm.yRot = Mth.lerp(env, this.rightArm.yRot, 0.28F);
                    this.leftArm.yRot = Mth.lerp(env, this.leftArm.yRot, -0.28F);
                    this.rightArm.zRot += 0.08F * env;
                    this.leftArm.zRot -= 0.08F * env;
                }
                case 2 -> { // ANIM_HUG: тёплые объятия (65 тиков)
                    float open = bell(t, 0.0F, 12.0F, 16.0F, 22.0F);
                    float hug = bell(t, 12.0F, 22.0F, 50.0F, 65.0F);
                    this.blush.visible = t > 8.0F;
                    this.closedEyes.visible = t >= 18.0F && t <= 53.0F;
                    this.body.xRot = 0.14F * hug;
                    this.body.z -= 12.0F * hug;
                    this.head.yRot = Mth.lerp(hug, this.head.yRot, 0.16F);
                    this.head.xRot = Mth.lerp(hug, this.head.xRot, 0.12F);
                    this.head.zRot += 0.22F * hug;
                    this.rightArm.xRot = -0.95F * open - 1.24F * hug;
                    this.leftArm.xRot = -0.95F * open - 1.24F * hug;
                    this.rightArm.yRot = 0.48F * hug;
                    this.leftArm.yRot = -0.48F * hug;
                    this.rightArm.zRot = 0.65F * open + 0.08F * hug;
                    this.leftArm.zRot = -0.65F * open - 0.08F * hug;
                }
                case 3 -> { // ANIM_KISS: смущённый нежный поцелуй (75 тиков)
                    float shy = bell(t, 0.0F, 8.0F, 14.0F, 22.0F);
                    float lean = bell(t, 14.0F, 26.0F, 48.0F, 60.0F);
                    float after = bell(t, 52.0F, 60.0F, 67.0F, 75.0F);
                    this.blush.visible = t > 4.0F;
                    this.closedEyes.visible = t >= 22.0F && t <= 56.0F;

                    // Привстаёт на носочки и тянется вперёд
                    this.root.y -= 10.0F * lean;
                    this.body.xRot = 0.18F * lean;
                    this.body.z -= 16.0F * lean;
                    this.head.yRot = Mth.lerp(lean, this.head.yRot, 0.0F) + 0.18F * shy;
                    this.head.xRot = Mth.lerp(lean, this.head.xRot, -0.14F) + 0.10F * shy;
                    this.head.zRot += 0.12F * shy + 0.14F * after;

                    // Руки: прикрывает щёчку -> нежно держит за плечи -> прикрывает губы
                    this.rightArm.xRot = -1.35F * shy - 1.08F * lean - 1.55F * after;
                    this.rightArm.yRot = 0.35F * shy + 0.24F * lean + 0.42F * after;
                    this.rightArm.zRot = 0.16F * lean + 0.06F * after;
                    this.leftArm.xRot = -0.22F * shy - 1.08F * lean - 0.22F * after;
                    this.leftArm.yRot = -0.24F * lean;
                    this.leftArm.zRot = -0.16F * lean - 0.08F * (shy + after);
                }
                case 4 -> { // ANIM_DANCE: весёлый танец с кружением (100 тиков)
                    float env = bell(t, 0.0F, 12.0F, 86.0F, 100.0F);
                    float p = smooth01(t / 100.0F);
                    float beat = Mth.sin(t * 0.32F);
                    float beatCos = Mth.cos(t * 0.32F);
                    this.blush.visible = true;
                    this.root.yRot = p * Mth.TWO_PI * 2.0F;
                    this.root.y -= Math.abs(beat) * 16.0F * env;
                    this.body.zRot = 0.08F * beat * env;
                    this.head.zRot += 0.16F * beat * env;
                    this.head.xRot -= 0.08F * env;
                    this.rightArm.zRot = (0.85F + 0.35F * beat) * env;
                    this.leftArm.zRot = (-0.85F - 0.35F * beat) * env;
                    this.rightArm.xRot = (-0.58F + 0.35F * beatCos) * env;
                    this.leftArm.xRot = (-0.58F - 0.35F * beatCos) * env;
                    this.rightLeg.xRot = 0.32F * beat * env;
                    this.leftLeg.xRot = -0.32F * beat * env;
                }
                case 5 -> { // ANIM_WEDDING: свадебная церемония и клятва (130 тиков)
                    float bow = bell(t, 0.0F, 12.0F, 22.0F, 32.0F);
                    float ring = bell(t, 26.0F, 38.0F, 64.0F, 76.0F);
                    float twirl = bell(t, 70.0F, 80.0F, 106.0F, 118.0F);
                    float finale = bell(t, 110.0F, 118.0F, 123.0F, 130.0F);

                    this.blush.visible = t > 8.0F;
                    this.closedEyes.visible = (t >= 48.0F && t <= 66.0F) || (t >= 116.0F && t <= 126.0F);

                    // Реверанс -> кольцо -> радостный свадебный пируэт -> финальный поклон
                    this.root.y += 14.0F * bow + 10.0F * finale - 8.0F * twirl;
                    if (t >= 72.0F && t <= 114.0F) {
                        float spinP = smooth01((t - 72.0F) / 42.0F);
                        this.root.yRot = spinP * Mth.TWO_PI;
                    }
                    this.body.xRot = 0.14F * bow + 0.10F * finale;
                    this.head.xRot = Mth.lerp(ring, this.head.xRot, -0.06F) + 0.16F * bow + 0.12F * finale;
                    this.head.zRot += 0.14F * ring + 0.12F * finale;

                    // Левая рука тянется для кольца, правая у сердца, затем обе взлетают в празднике
                    this.leftArm.xRot = -1.25F * bow - 1.18F * ring - 2.15F * twirl - 0.95F * finale;
                    this.leftArm.yRot = -0.50F * bow - 0.18F * ring;
                    this.leftArm.zRot = -0.10F * ring - 0.58F * twirl;

                    this.rightArm.xRot = -1.25F * bow - 1.15F * ring - 2.15F * twirl - 1.45F * finale;
                    this.rightArm.yRot = 0.50F * bow + 0.55F * ring + 0.38F * finale;
                    this.rightArm.zRot = 0.58F * twirl;
                }
            }
            return;
        }

        if (state.bathPhase == 1) {
            // Фаза 1: оглядывается по сторонам (0..40 тиков)
            float t = Mth.clamp(state.bathTicks / 40.0F, 0.0F, 1.0F);
            this.head.yRot += Mth.sin(t * Mth.TWO_PI) * 0.68F;
            this.head.zRot += Mth.sin(t * Mth.TWO_PI) * 0.08F;
            this.rightArm.xRot -= 0.18F;
            this.leftArm.xRot -= 0.18F;
        } else if (state.bathPhase == 2) {
            // Фаза 2: плавно снимает шлем, крылья и броню, оставаясь в раздельном купальнике
            float p = Mth.clamp(state.bathTicks / 45.0F, 0.0F, 1.0F);
            this.blush.visible = p > 0.25F;
            this.head.zRot += 0.10F * Mth.sin(p * Mth.PI);
            this.rightArm.xRot -= 0.55F * Mth.sin(p * Mth.PI);
            this.leftArm.xRot -= 0.55F * Mth.sin(p * Mth.PI);
            this.rightArm.zRot += 0.22F * Mth.sin(p * Mth.PI);
            this.leftArm.zRot -= 0.22F * Mth.sin(p * Mth.PI);

            setPartScaleAndFade(this.helmet, Mth.clamp(p * 1.8F, 0.0F, 1.0F));
            setPartScaleAndFade(this.backpack, Mth.clamp((p - 0.15F) * 1.6F, 0.0F, 1.0F));
            setPartScaleAndFade(this.armorRightArm, Mth.clamp((p - 0.20F) * 1.6F, 0.0F, 1.0F));
            setPartScaleAndFade(this.armorLeftArm, Mth.clamp((p - 0.20F) * 1.6F, 0.0F, 1.0F));
            setPartScaleAndFade(this.armorTorso, Mth.clamp((p - 0.35F) * 1.6F, 0.0F, 1.0F));
            setPartScaleAndFade(this.armorRightLeg, Mth.clamp((p - 0.45F) * 1.85F, 0.0F, 1.0F));
            setPartScaleAndFade(this.armorLeftLeg, Mth.clamp((p - 0.45F) * 1.85F, 0.0F, 1.0F));
            if (state.hasHeadArmor) setPartScaleAndFade(this.extraHead, Mth.clamp(p * 1.8F, 0.0F, 1.0F));
            if (state.hasChestArmor) setPartScaleAndFade(this.extraChest, Mth.clamp((p - 0.35F) * 1.6F, 0.0F, 1.0F));
            if (state.hasLegsArmor) {
                setPartScaleAndFade(this.extraLegsR, Mth.clamp((p - 0.45F) * 1.85F, 0.0F, 1.0F));
                setPartScaleAndFade(this.extraLegsL, Mth.clamp((p - 0.45F) * 1.85F, 0.0F, 1.0F));
            }
            if (state.hasFeetArmor) {
                setPartScaleAndFade(this.extraFeetR, Mth.clamp((p - 0.45F) * 1.85F, 0.0F, 1.0F));
                setPartScaleAndFade(this.extraFeetL, Mth.clamp((p - 0.45F) * 1.85F, 0.0F, 1.0F));
            }
        } else if (state.bathPhase == 3) {
            // Фаза 3: идёт к водоёму в купальнике
            this.blush.visible = true;
            this.rightArm.zRot += 0.08F;
            this.leftArm.zRot -= 0.08F;
        } else if (state.bathPhase == 4) {
            // Фаза 4: сидит в воде и отдыхает
            this.blush.visible = true;
            this.closedEyes.visible = (state.ageInTicks % 90.0F) > 62.0F;
            this.root.y += 155.0F + Mth.sin(state.ageInTicks * 0.12F) * 5.0F;
            this.body.xRot = -0.08F;
            this.head.xRot += 0.10F;
            this.head.zRot += 0.08F * Mth.sin(state.ageInTicks * 0.07F);
            this.rightLeg.xRot = -1.18F;
            this.leftLeg.xRot = -1.18F;
            this.rightLeg.yRot = -0.18F;
            this.leftLeg.yRot = 0.18F;
            this.rightArm.xRot = -0.32F + 0.12F * Mth.sin(state.ageInTicks * 0.14F);
            this.leftArm.xRot = -0.32F - 0.12F * Mth.sin(state.ageInTicks * 0.14F);
            this.rightArm.zRot = 0.24F;
            this.leftArm.zRot = -0.24F;
        } else if (state.bathPhase == 5) {
            // Фаза 5: отдыхает в купальнике
            this.blush.visible = true;
            this.head.zRot += 0.10F;
            this.rightArm.xRot -= 0.22F;
            this.rightArm.zRot += 0.16F;
            this.leftArm.zRot -= 0.10F;
        }
    }
}
