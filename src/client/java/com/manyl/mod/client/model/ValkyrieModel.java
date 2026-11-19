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
 * Ультра-детализированная 3D-модель Воительницы (191 скульптурный элемент, 24x геометрия, текстура 4096x4096)
 * с модульной съёмной бронёй, скромным закрытым купальником, румянцем и 6-фазной анимацией купания.
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
        b_0.texOffs(2340, 594).addBox(-27.0F, 0.0F, -27.0F, 54.0F, 24.0F, 54.0F);
        b_0.texOffs(2168, 204).addBox(-24.0F, 22.0F, -24.0F, 48.0F, 86.0F, 48.0F);
        b_0.texOffs(3676, 688).addBox(-20.0F, 102.0F, -22.0F, 40.0F, 26.0F, 44.0F);
        b_0.texOffs(1816, 204).addBox(-21.0F, 126.0F, -22.0F, 42.0F, 92.0F, 44.0F);
        b_0.texOffs(4, 594).addBox(-22.0F, 214.0F, -34.0F, 44.0F, 26.0F, 64.0F);
        b_0.texOffs(3636, 594).addBox(-23.0F, 232.0F, -36.0F, 46.0F, 8.0F, 68.0F);
        b_0.texOffs(3288, 768).addBox(-18.0F, 212.0F, -24.0F, 36.0F, 6.0F, 46.0F);
        PartDefinition part_right_leg = part_root.addOrReplaceChild("right_leg", b_0, PartPose.offset(-32.0F, -240.0F, 0.0F));
        CubeListBuilder b_1 = CubeListBuilder.create();
        b_1.texOffs(1296, 204).addBox(-32.0F, 6.0F, -32.0F, 64.0F, 72.0F, 64.0F);
        b_1.texOffs(4, 480).addBox(-36.0F, 12.0F, -26.0F, 12.0F, 58.0F, 52.0F);
        b_1.texOffs(636, 768).addBox(-38.0F, 22.0F, -16.0F, 6.0F, 36.0F, 32.0F);
        b_1.texOffs(2328, 842).addBox(-39.0F, 32.0F, -8.0F, 4.0F, 16.0F, 16.0F);
        b_1.texOffs(2224, 768).addBox(-28.0F, 88.0F, -36.0F, 56.0F, 40.0F, 16.0F);
        b_1.texOffs(200, 842).addBox(-22.0F, 94.0F, -44.0F, 44.0F, 28.0F, 12.0F);
        b_1.texOffs(312, 890).addBox(-10.0F, 99.0F, -49.0F, 20.0F, 18.0F, 8.0F);
        b_1.texOffs(2764, 688).addBox(-30.0F, 84.0F, -30.0F, 60.0F, 10.0F, 60.0F);
        b_1.texOffs(3084, 4).addBox(-28.0F, 124.0F, -30.0F, 56.0F, 86.0F, 60.0F);
        b_1.texOffs(632, 594).addBox(-24.0F, 130.0F, -35.0F, 48.0F, 74.0F, 10.0F);
        b_1.texOffs(1960, 688).addBox(-6.0F, 132.0F, -39.0F, 12.0F, 68.0F, 6.0F);
        b_1.texOffs(476, 688).addBox(-22.0F, 136.0F, 26.0F, 44.0F, 66.0F, 10.0F);
        b_1.texOffs(2892, 204).addBox(-30.0F, 206.0F, -54.0F, 60.0F, 34.0F, 92.0F);
        b_1.texOffs(508, 480).addBox(-32.0F, 232.0F, -58.0F, 64.0F, 10.0F, 98.0F);
        b_1.texOffs(3832, 768).addBox(-22.0F, 212.0F, -68.0F, 44.0F, 24.0F, 20.0F);
        b_1.texOffs(152, 890).addBox(-14.0F, 216.0F, -74.0F, 28.0F, 16.0F, 10.0F);
        b_1.texOffs(796, 842).addBox(-18.0F, 198.0F, -40.0F, 36.0F, 14.0F, 22.0F);
        PartDefinition part_armor_right_leg = part_right_leg.addOrReplaceChild("armor_right_leg", b_1, PartPose.ZERO);
        CubeListBuilder b_2 = CubeListBuilder.create();
        b_2.texOffs(2220, 4).addBox(-35.0F, 4.0F, -35.0F, 70.0F, 80.0F, 70.0F);
        PartDefinition part_extra_legs_right_leg = part_right_leg.addOrReplaceChild("extra_legs_right_leg", b_2, PartPose.ZERO);
        CubeListBuilder b_3 = CubeListBuilder.create();
        b_3.texOffs(3556, 4).addBox(-34.0F, 204.0F, -62.0F, 68.0F, 38.0F, 106.0F);
        PartDefinition part_extra_feet_right_leg = part_right_leg.addOrReplaceChild("extra_feet_right_leg", b_3, PartPose.ZERO);
        CubeListBuilder b_4 = CubeListBuilder.create();
        b_4.texOffs(2560, 594).addBox(-27.0F, 0.0F, -27.0F, 54.0F, 24.0F, 54.0F);
        b_4.texOffs(2364, 204).addBox(-24.0F, 22.0F, -24.0F, 48.0F, 86.0F, 48.0F);
        b_4.texOffs(3848, 688).addBox(-20.0F, 102.0F, -22.0F, 40.0F, 26.0F, 44.0F);
        b_4.texOffs(1992, 204).addBox(-21.0F, 126.0F, -22.0F, 42.0F, 92.0F, 44.0F);
        b_4.texOffs(224, 594).addBox(-22.0F, 214.0F, -34.0F, 44.0F, 26.0F, 64.0F);
        b_4.texOffs(4, 688).addBox(-23.0F, 232.0F, -36.0F, 46.0F, 8.0F, 68.0F);
        b_4.texOffs(3456, 768).addBox(-18.0F, 212.0F, -24.0F, 36.0F, 6.0F, 46.0F);
        PartDefinition part_left_leg = part_root.addOrReplaceChild("left_leg", b_4, PartPose.offset(32.0F, -240.0F, 0.0F));
        CubeListBuilder b_5 = CubeListBuilder.create();
        b_5.texOffs(1556, 204).addBox(-32.0F, 6.0F, -32.0F, 64.0F, 72.0F, 64.0F);
        b_5.texOffs(136, 480).addBox(26.0F, 12.0F, -26.0F, 12.0F, 58.0F, 52.0F);
        b_5.texOffs(716, 768).addBox(34.0F, 22.0F, -16.0F, 6.0F, 36.0F, 32.0F);
        b_5.texOffs(2372, 842).addBox(37.0F, 32.0F, -8.0F, 4.0F, 16.0F, 16.0F);
        b_5.texOffs(2372, 768).addBox(-28.0F, 88.0F, -36.0F, 56.0F, 40.0F, 16.0F);
        b_5.texOffs(316, 842).addBox(-22.0F, 94.0F, -44.0F, 44.0F, 28.0F, 12.0F);
        b_5.texOffs(372, 890).addBox(-10.0F, 99.0F, -49.0F, 20.0F, 18.0F, 8.0F);
        b_5.texOffs(3008, 688).addBox(-30.0F, 84.0F, -30.0F, 60.0F, 10.0F, 60.0F);
        b_5.texOffs(3320, 4).addBox(-28.0F, 124.0F, -30.0F, 56.0F, 86.0F, 60.0F);
        b_5.texOffs(752, 594).addBox(-24.0F, 130.0F, -35.0F, 48.0F, 74.0F, 10.0F);
        b_5.texOffs(2000, 688).addBox(-6.0F, 132.0F, -39.0F, 12.0F, 68.0F, 6.0F);
        b_5.texOffs(588, 688).addBox(-22.0F, 136.0F, 26.0F, 44.0F, 66.0F, 10.0F);
        b_5.texOffs(3200, 204).addBox(-30.0F, 206.0F, -54.0F, 60.0F, 34.0F, 92.0F);
        b_5.texOffs(836, 480).addBox(-32.0F, 232.0F, -58.0F, 64.0F, 10.0F, 98.0F);
        b_5.texOffs(3964, 768).addBox(-22.0F, 212.0F, -68.0F, 44.0F, 24.0F, 20.0F);
        b_5.texOffs(232, 890).addBox(-14.0F, 216.0F, -74.0F, 28.0F, 16.0F, 10.0F);
        b_5.texOffs(916, 842).addBox(-18.0F, 198.0F, -40.0F, 36.0F, 14.0F, 22.0F);
        PartDefinition part_armor_left_leg = part_left_leg.addOrReplaceChild("armor_left_leg", b_5, PartPose.ZERO);
        CubeListBuilder b_6 = CubeListBuilder.create();
        b_6.texOffs(2504, 4).addBox(-35.0F, 4.0F, -35.0F, 70.0F, 80.0F, 70.0F);
        PartDefinition part_extra_legs_left_leg = part_left_leg.addOrReplaceChild("extra_legs_left_leg", b_6, PartPose.ZERO);
        CubeListBuilder b_7 = CubeListBuilder.create();
        b_7.texOffs(4, 204).addBox(-34.0F, 204.0F, -62.0F, 68.0F, 38.0F, 106.0F);
        PartDefinition part_extra_feet_left_leg = part_left_leg.addOrReplaceChild("extra_feet_left_leg", b_7, PartPose.ZERO);
        CubeListBuilder b_8 = CubeListBuilder.create();
        b_8.texOffs(2040, 688).addBox(-17.0F, -288.0F, -17.0F, 34.0F, 38.0F, 34.0F);
        b_8.texOffs(2180, 688).addBox(-48.0F, -254.0F, -26.0F, 96.0F, 18.0F, 52.0F);
        b_8.texOffs(1476, 768).addBox(-40.0F, -260.0F, -24.0F, 80.0F, 10.0F, 48.0F);
        b_8.texOffs(2560, 204).addBox(-50.0F, -238.0F, -32.0F, 100.0F, 62.0F, 64.0F);
        b_8.texOffs(700, 688).addBox(-42.0F, -246.0F, -30.0F, 84.0F, 14.0F, 60.0F);
        b_8.texOffs(1072, 594).addBox(-44.0F, -248.0F, -31.0F, 16.0F, 18.0F, 62.0F);
        b_8.texOffs(1232, 594).addBox(28.0F, -248.0F, -31.0F, 16.0F, 18.0F, 62.0F);
        b_8.texOffs(1864, 480).addBox(-42.0F, -178.0F, -29.0F, 84.0F, 44.0F, 58.0F);
        b_8.texOffs(780, 352).addBox(-39.0F, -136.0F, -27.0F, 78.0F, 68.0F, 54.0F);
        b_8.texOffs(2480, 688).addBox(-41.0F, -112.0F, -29.0F, 82.0F, 12.0F, 58.0F);
        b_8.texOffs(432, 890).addBox(-10.0F, -116.0F, -32.0F, 20.0F, 18.0F, 8.0F);
        b_8.texOffs(1164, 480).addBox(-48.0F, -70.0F, -31.0F, 96.0F, 46.0F, 62.0F);
        b_8.texOffs(2152, 480).addBox(-54.0F, -28.0F, -33.0F, 108.0F, 30.0F, 66.0F);
        b_8.texOffs(3272, 594).addBox(-56.0F, -4.0F, -34.0F, 112.0F, 8.0F, 68.0F);
        PartDefinition part_body = part_root.addOrReplaceChild("body", b_8, PartPose.offset(0.0F, -240.0F, 0.0F));
        CubeListBuilder b_9 = CubeListBuilder.create();
        b_9.texOffs(2688, 352).addBox(-64.0F, -260.0F, -44.0F, 128.0F, 22.0F, 88.0F);
        b_9.texOffs(876, 4).addBox(-70.0F, -244.0F, -48.0F, 140.0F, 68.0F, 96.0F);
        b_9.texOffs(140, 768).addBox(-62.0F, -236.0F, -56.0F, 56.0F, 54.0F, 14.0F);
        b_9.texOffs(284, 768).addBox(6.0F, -236.0F, -56.0F, 56.0F, 54.0F, 14.0F);
        b_9.texOffs(4, 768).addBox(-26.0F, -226.0F, -62.0F, 52.0F, 56.0F, 14.0F);
        b_9.texOffs(3744, 768).addBox(-16.0F, -216.0F, -68.0F, 32.0F, 36.0F, 10.0F);
        b_9.texOffs(3080, 842).addBox(-10.0F, -210.0F, -72.0F, 20.0F, 24.0F, 6.0F);
        b_9.texOffs(1484, 480).addBox(-56.0F, -180.0F, -38.0F, 112.0F, 30.0F, 76.0F);
        b_9.texOffs(2788, 4).addBox(-42.0F, -152.0F, -31.0F, 84.0F, 84.0F, 62.0F);
        b_9.texOffs(1392, 594).addBox(-44.0F, -140.0F, -34.0F, 88.0F, 10.0F, 68.0F);
        b_9.texOffs(1708, 594).addBox(-44.0F, -112.0F, -34.0F, 88.0F, 10.0F, 68.0F);
        b_9.texOffs(2024, 594).addBox(-44.0F, -84.0F, -34.0F, 88.0F, 10.0F, 68.0F);
        b_9.texOffs(2660, 480).addBox(-48.0F, -68.0F, -36.0F, 96.0F, 22.0F, 72.0F);
        b_9.texOffs(88, 842).addBox(-20.0F, -72.0F, -44.0F, 40.0F, 28.0F, 14.0F);
        b_9.texOffs(540, 890).addBox(-10.0F, -64.0F, -48.0F, 20.0F, 14.0F, 8.0F);
        b_9.texOffs(812, 204).addBox(-74.0F, -46.0F, -46.0F, 148.0F, 44.0F, 92.0F);
        b_9.texOffs(1492, 352).addBox(-28.0F, -42.0F, -52.0F, 56.0F, 104.0F, 12.0F);
        b_9.texOffs(3136, 842).addBox(-32.0F, 54.0F, -54.0F, 64.0F, 14.0F, 14.0F);
        b_9.texOffs(3904, 352).addBox(-28.0F, -42.0F, 42.0F, 56.0F, 98.0F, 12.0F);
        b_9.texOffs(3296, 842).addBox(-32.0F, 48.0F, 40.0F, 64.0F, 14.0F, 14.0F);
        b_9.texOffs(1352, 4).addBox(-90.0F, -40.0F, -44.0F, 18.0F, 72.0F, 88.0F);
        b_9.texOffs(356, 204).addBox(-98.0F, 8.0F, -48.0F, 16.0F, 48.0F, 96.0F);
        b_9.texOffs(1632, 352).addBox(-100.0F, 50.0F, -50.0F, 14.0F, 14.0F, 100.0F);
        b_9.texOffs(1132, 768).addBox(-100.0F, 16.0F, -16.0F, 10.0F, 28.0F, 32.0F);
        b_9.texOffs(1568, 4).addBox(74.0F, -40.0F, -44.0F, 18.0F, 72.0F, 88.0F);
        b_9.texOffs(584, 204).addBox(86.0F, 8.0F, -48.0F, 16.0F, 48.0F, 96.0F);
        b_9.texOffs(1864, 352).addBox(90.0F, 50.0F, -50.0F, 14.0F, 14.0F, 100.0F);
        b_9.texOffs(1220, 768).addBox(94.0F, 16.0F, -16.0F, 10.0F, 28.0F, 32.0F);
        PartDefinition part_armor_torso = part_body.addOrReplaceChild("armor_torso", b_9, PartPose.ZERO);
        CubeListBuilder b_10 = CubeListBuilder.create();
        b_10.texOffs(4, 4).addBox(-76.0F, -248.0F, -56.0F, 152.0F, 84.0F, 112.0F);
        PartDefinition part_extra_chest = part_body.addOrReplaceChild("extra_chest", b_10, PartPose.ZERO);
        CubeListBuilder b_11 = CubeListBuilder.create();
        b_11.texOffs(1288, 352).addBox(-36.0F, -42.0F, -8.0F, 72.0F, 88.0F, 28.0F);
        b_11.texOffs(428, 768).addBox(-22.0F, -26.0F, 16.0F, 44.0F, 56.0F, 12.0F);
        b_11.texOffs(432, 842).addBox(-12.0F, -14.0F, 24.0F, 24.0F, 32.0F, 8.0F);
        b_11.texOffs(1992, 768).addBox(-16.0F, -52.0F, 6.0F, 40.0F, 42.0F, 16.0F);
        b_11.texOffs(1736, 768).addBox(8.0F, -88.0F, 12.0F, 48.0F, 44.0F, 14.0F);
        b_11.texOffs(2672, 768).addBox(38.0F, -122.0F, 16.0F, 54.0F, 40.0F, 14.0F);
        b_11.texOffs(1884, 842).addBox(66.0F, -158.0F, 20.0F, 68.0F, 22.0F, 10.0F);
        b_11.texOffs(600, 890).addBox(162.0F, -154.0F, 22.0F, 20.0F, 14.0F, 6.0F);
        b_11.texOffs(1172, 842).addBox(42.0F, -122.0F, 18.0F, 78.0F, 22.0F, 10.0F);
        b_11.texOffs(656, 890).addBox(158.0F, -118.0F, 20.0F, 20.0F, 14.0F, 6.0F);
        b_11.texOffs(1532, 842).addBox(20.0F, -84.0F, 16.0F, 76.0F, 22.0F, 10.0F);
        b_11.texOffs(712, 890).addBox(132.0F, -80.0F, 18.0F, 20.0F, 14.0F, 6.0F);
        b_11.texOffs(2416, 842).addBox(2.0F, -48.0F, 14.0F, 68.0F, 20.0F, 10.0F);
        b_11.texOffs(968, 890).addBox(98.0F, -44.0F, 16.0F, 20.0F, 12.0F, 6.0F);
        b_11.texOffs(2736, 842).addBox(-10.0F, -16.0F, 10.0F, 56.0F, 20.0F, 10.0F);
        b_11.texOffs(1024, 890).addBox(62.0F, -12.0F, 12.0F, 20.0F, 12.0F, 6.0F);
        b_11.texOffs(3456, 842).addBox(-14.0F, 14.0F, 8.0F, 44.0F, 18.0F, 10.0F);
        b_11.texOffs(1222, 890).addBox(34.0F, 18.0F, 10.0F, 20.0F, 10.0F, 6.0F);
        b_11.texOffs(2108, 768).addBox(24.0F, -52.0F, 6.0F, 40.0F, 42.0F, 16.0F);
        b_11.texOffs(1864, 768).addBox(56.0F, -88.0F, 12.0F, 48.0F, 44.0F, 14.0F);
        b_11.texOffs(2812, 768).addBox(92.0F, -122.0F, 16.0F, 54.0F, 40.0F, 14.0F);
        b_11.texOffs(2044, 842).addBox(134.0F, -158.0F, 20.0F, 68.0F, 22.0F, 10.0F);
        b_11.texOffs(768, 890).addBox(182.0F, -154.0F, 22.0F, 20.0F, 14.0F, 6.0F);
        b_11.texOffs(1352, 842).addBox(120.0F, -122.0F, 18.0F, 78.0F, 22.0F, 10.0F);
        b_11.texOffs(824, 890).addBox(178.0F, -118.0F, 20.0F, 20.0F, 14.0F, 6.0F);
        b_11.texOffs(1708, 842).addBox(96.0F, -84.0F, 16.0F, 76.0F, 22.0F, 10.0F);
        b_11.texOffs(880, 890).addBox(152.0F, -80.0F, 18.0F, 20.0F, 14.0F, 6.0F);
        b_11.texOffs(2576, 842).addBox(70.0F, -48.0F, 14.0F, 68.0F, 20.0F, 10.0F);
        b_11.texOffs(1080, 890).addBox(118.0F, -44.0F, 16.0F, 20.0F, 12.0F, 6.0F);
        b_11.texOffs(2872, 842).addBox(46.0F, -16.0F, 10.0F, 56.0F, 20.0F, 10.0F);
        b_11.texOffs(1136, 890).addBox(82.0F, -12.0F, 12.0F, 20.0F, 12.0F, 6.0F);
        b_11.texOffs(3568, 842).addBox(30.0F, 14.0F, 8.0F, 44.0F, 18.0F, 10.0F);
        b_11.texOffs(1278, 890).addBox(54.0F, 18.0F, 10.0F, 20.0F, 10.0F, 6.0F);
        PartDefinition part_backpack = part_body.addOrReplaceChild("backpack", b_11, PartPose.offset(0.0F, -190.0F, 44.0F));
        CubeListBuilder b_12 = CubeListBuilder.create();
        b_12.texOffs(536, 4).addBox(-42.0F, -88.0F, -42.0F, 84.0F, 88.0F, 84.0F);
        b_12.texOffs(1192, 890).addBox(-4.0F, -34.0F, -46.0F, 8.0F, 12.0F, 5.0F);
        b_12.texOffs(4, 890).addBox(-28.0F, -10.0F, -43.0F, 56.0F, 10.0F, 16.0F);
        b_12.texOffs(2320, 352).addBox(-46.0F, -96.0F, -44.0F, 92.0F, 22.0F, 90.0F);
        b_12.texOffs(3660, 352).addBox(-48.0F, -86.0F, 28.0F, 96.0F, 86.0F, 24.0F);
        b_12.texOffs(1104, 842).addBox(-44.0F, -84.0F, -48.0F, 22.0F, 24.0F, 10.0F);
        b_12.texOffs(3784, 842).addBox(-24.0F, -86.0F, -49.0F, 24.0F, 18.0F, 10.0F);
        b_12.texOffs(3008, 842).addBox(0.0F, -84.0F, -49.0F, 24.0F, 20.0F, 10.0F);
        b_12.texOffs(1036, 842).addBox(22.0F, -84.0F, -48.0F, 22.0F, 26.0F, 10.0F);
        b_12.texOffs(1048, 352).addBox(-50.0F, -80.0F, -38.0F, 12.0F, 76.0F, 46.0F);
        b_12.texOffs(1168, 352).addBox(38.0F, -80.0F, -38.0F, 12.0F, 76.0F, 46.0F);
        b_12.texOffs(2520, 768).addBox(-48.0F, -10.0F, -34.0F, 14.0F, 34.0F, 22.0F);
        b_12.texOffs(2596, 768).addBox(34.0F, -10.0F, -34.0F, 14.0F, 34.0F, 22.0F);
        b_12.texOffs(3140, 594).addBox(-22.0F, -78.0F, 44.0F, 44.0F, 58.0F, 20.0F);
        b_12.texOffs(796, 768).addBox(-18.0F, -22.0F, 52.0F, 36.0F, 48.0F, 18.0F);
        b_12.texOffs(2204, 842).addBox(-20.0F, 22.0F, 56.0F, 40.0F, 12.0F, 20.0F);
        b_12.texOffs(544, 768).addBox(-14.0F, 32.0F, 58.0F, 28.0F, 52.0F, 16.0F);
        b_12.texOffs(3680, 842).addBox(-16.0F, 80.0F, 60.0F, 32.0F, 10.0F, 18.0F);
        b_12.texOffs(3216, 768).addBox(-10.0F, 88.0F, 62.0F, 20.0F, 40.0F, 14.0F);
        PartDefinition part_head = part_body.addOrReplaceChild("head", b_12, PartPose.offset(0.0F, -284.0F, 0.0F));
        CubeListBuilder b_13 = CubeListBuilder.create();
        b_13.texOffs(1438, 890).addBox(-36.0F, -34.0F, -43.0F, 14.0F, 7.0F, 2.0F);
        b_13.texOffs(1474, 890).addBox(22.0F, -34.0F, -43.0F, 14.0F, 7.0F, 2.0F);
        PartDefinition part_blush = part_head.addOrReplaceChild("blush", b_13, PartPose.ZERO);
        CubeListBuilder b_14 = CubeListBuilder.create();
        b_14.texOffs(1334, 890).addBox(-34.0F, -50.0F, -43.0F, 22.0F, 14.0F, 2.0F);
        b_14.texOffs(1386, 890).addBox(12.0F, -50.0F, -43.0F, 22.0F, 14.0F, 2.0F);
        PartDefinition part_closed_eyes = part_head.addOrReplaceChild("closed_eyes", b_14, PartPose.ZERO);
        CubeListBuilder b_15 = CubeListBuilder.create();
        b_15.texOffs(4, 352).addBox(-50.0F, -102.0F, -50.0F, 100.0F, 24.0F, 100.0F);
        b_15.texOffs(408, 352).addBox(-46.0F, -128.0F, -46.0F, 92.0F, 30.0F, 92.0F);
        b_15.texOffs(3000, 480).addBox(-38.0F, -140.0F, -38.0F, 76.0F, 16.0F, 76.0F);
        b_15.texOffs(4, 842).addBox(-14.0F, -120.0F, -56.0F, 28.0F, 32.0F, 12.0F);
        b_15.texOffs(492, 890).addBox(-8.0F, -114.0F, -60.0F, 16.0F, 20.0F, 6.0F);
        b_15.texOffs(936, 890).addBox(-4.0F, -84.0F, -50.0F, 8.0F, 14.0F, 6.0F);
        b_15.texOffs(444, 594).addBox(-12.0F, -154.0F, -32.0F, 24.0F, 18.0F, 68.0F);
        b_15.texOffs(2504, 480).addBox(-8.0F, -188.0F, -26.0F, 16.0F, 36.0F, 60.0F);
        b_15.texOffs(236, 688).addBox(28.0F, -104.0F, -18.0F, 18.0F, 36.0F, 40.0F);
        b_15.texOffs(1752, 688).addBox(40.0F, -136.0F, -4.0F, 18.0F, 42.0F, 32.0F);
        b_15.texOffs(1308, 768).addBox(52.0F, -164.0F, 10.0F, 16.0F, 36.0F, 24.0F);
        b_15.texOffs(684, 842).addBox(66.0F, -180.0F, 16.0F, 10.0F, 22.0F, 16.0F);
        b_15.texOffs(356, 688).addBox(46.0F, -104.0F, -18.0F, 18.0F, 36.0F, 40.0F);
        b_15.texOffs(1856, 688).addBox(58.0F, -136.0F, -4.0F, 18.0F, 42.0F, 32.0F);
        b_15.texOffs(1392, 768).addBox(68.0F, -164.0F, 10.0F, 16.0F, 36.0F, 24.0F);
        b_15.texOffs(740, 842).addBox(76.0F, -180.0F, 16.0F, 10.0F, 22.0F, 16.0F);
        PartDefinition part_helmet = part_head.addOrReplaceChild("helmet", b_15, PartPose.ZERO);
        CubeListBuilder b_16 = CubeListBuilder.create();
        b_16.texOffs(1784, 4).addBox(-54.0F, -132.0F, -54.0F, 108.0F, 42.0F, 108.0F);
        PartDefinition part_extra_head = part_head.addOrReplaceChild("extra_head", b_16, PartPose.ZERO);
        CubeListBuilder b_17 = CubeListBuilder.create();
        b_17.texOffs(2952, 768).addBox(-16.0F, -12.0F, -16.0F, 32.0F, 22.0F, 32.0F);
        b_17.texOffs(268, 480).addBox(-14.0F, 6.0F, -15.0F, 28.0F, 80.0F, 30.0F);
        b_17.texOffs(2096, 352).addBox(-13.0F, 82.0F, -14.0F, 26.0F, 86.0F, 28.0F);
        b_17.texOffs(908, 768).addBox(-13.0F, 166.0F, -14.0F, 26.0F, 34.0F, 28.0F);
        b_17.texOffs(500, 842).addBox(-10.0F, 198.0F, -12.0F, 20.0F, 14.0F, 24.0F);
        PartDefinition part_right_arm = part_body.addOrReplaceChild("right_arm", b_17, PartPose.offset(-68.0F, -238.0F, 0.0F));
        CubeListBuilder b_18 = CubeListBuilder.create();
        b_18.texOffs(3124, 352).addBox(-28.0F, -30.0F, -34.0F, 64.0F, 42.0F, 68.0F);
        b_18.texOffs(3308, 480).addBox(-32.0F, -6.0F, -38.0F, 72.0F, 14.0F, 76.0F);
        b_18.texOffs(1400, 688).addBox(-18.0F, -44.0F, -26.0F, 34.0F, 22.0F, 52.0F);
        b_18.texOffs(3856, 842).addBox(4.0F, -26.0F, -42.0F, 18.0F, 18.0F, 10.0F);
        b_18.texOffs(992, 688).addBox(-22.0F, 74.0F, -26.0F, 48.0F, 22.0F, 52.0F);
        b_18.texOffs(3508, 204).addBox(-20.0F, 94.0F, -26.0F, 44.0F, 74.0F, 52.0F);
        b_18.texOffs(872, 594).addBox(6.0F, 104.0F, -18.0F, 12.0F, 48.0F, 36.0F);
        b_18.texOffs(3624, 768).addBox(16.0F, 114.0F, -10.0F, 8.0F, 28.0F, 20.0F);
        b_18.texOffs(3252, 688).addBox(-22.0F, 164.0F, -28.0F, 48.0F, 14.0F, 56.0F);
        b_18.texOffs(2780, 594).addBox(-18.0F, 176.0F, -24.0F, 40.0F, 30.0F, 48.0F);
        PartDefinition part_armor_right_arm = part_right_arm.addOrReplaceChild("armor_right_arm", b_18, PartPose.ZERO);
        CubeListBuilder b_19 = CubeListBuilder.create();
        b_19.texOffs(3084, 768).addBox(-16.0F, -12.0F, -16.0F, 32.0F, 22.0F, 32.0F);
        b_19.texOffs(388, 480).addBox(-14.0F, 6.0F, -15.0F, 28.0F, 80.0F, 30.0F);
        b_19.texOffs(2208, 352).addBox(-13.0F, 82.0F, -14.0F, 26.0F, 86.0F, 28.0F);
        b_19.texOffs(1020, 768).addBox(-13.0F, 166.0F, -14.0F, 26.0F, 34.0F, 28.0F);
        b_19.texOffs(592, 842).addBox(-10.0F, 198.0F, -12.0F, 20.0F, 14.0F, 24.0F);
        PartDefinition part_left_arm = part_body.addOrReplaceChild("left_arm", b_19, PartPose.offset(68.0F, -238.0F, 0.0F));
        CubeListBuilder b_20 = CubeListBuilder.create();
        b_20.texOffs(3392, 352).addBox(-28.0F, -30.0F, -34.0F, 64.0F, 42.0F, 68.0F);
        b_20.texOffs(3608, 480).addBox(-32.0F, -6.0F, -38.0F, 72.0F, 14.0F, 76.0F);
        b_20.texOffs(1576, 688).addBox(16.0F, -44.0F, -26.0F, 34.0F, 22.0F, 52.0F);
        b_20.texOffs(3916, 842).addBox(22.0F, -26.0F, -42.0F, 18.0F, 18.0F, 10.0F);
        b_20.texOffs(1196, 688).addBox(-22.0F, 74.0F, -26.0F, 48.0F, 22.0F, 52.0F);
        b_20.texOffs(3704, 204).addBox(-20.0F, 94.0F, -26.0F, 44.0F, 74.0F, 52.0F);
        b_20.texOffs(972, 594).addBox(18.0F, 104.0F, -18.0F, 12.0F, 48.0F, 36.0F);
        b_20.texOffs(3684, 768).addBox(24.0F, 114.0F, -10.0F, 8.0F, 28.0F, 20.0F);
        b_20.texOffs(3464, 688).addBox(-22.0F, 164.0F, -28.0F, 48.0F, 14.0F, 56.0F);
        b_20.texOffs(2960, 594).addBox(-18.0F, 176.0F, -24.0F, 40.0F, 30.0F, 48.0F);
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
        this.workAnim.apply(state.workAnimationState, state.ageInTicks, 1.0F);
        this.attackAnim.apply(state.attackAnimationState, state.ageInTicks, 1.0F);
        this.carryAnim.apply(state.carryAnimationState, state.ageInTicks, 1.0F);
        this.sitAnim.apply(state.sitAnimationState, state.ageInTicks, 1.0F);
        this.summonAnim.apply(state.summonAnimationState, state.ageInTicks, 1.0F);
        this.eatAnim.apply(state.eatAnimationState, state.ageInTicks, 1.0F);

        if (state.bathPhase == 1) {
            // Фаза 1: осторожно оглядывается по сторонам (0..40 тиков)
            float t = Mth.clamp(state.bathTicks / 40.0F, 0.0F, 1.0F);
            this.head.yRot += Mth.sin(t * Mth.TWO_PI) * 0.68F;
            this.head.zRot += Mth.sin(t * Mth.TWO_PI) * 0.08F;
            this.rightArm.xRot -= 0.18F;
            this.leftArm.xRot -= 0.18F;
        } else if (state.bathPhase == 2) {
            // Фаза 2: плавно снимает шлем, крылья и броню, оставаясь в скромном купальнике
            float p = Mth.clamp(state.bathTicks / 45.0F, 0.0F, 1.0F);
            this.blush.visible = p > 0.25F;
            this.head.zRot += 0.10F * Mth.sin(p * Mth.PI);
            this.rightArm.xRot -= 0.55F * Mth.sin(p * Mth.PI);
            this.leftArm.xRot -= 0.55F * Mth.sin(p * Mth.PI);
            this.rightArm.zRot += 0.20F * Mth.sin(p * Mth.PI);
            this.leftArm.zRot -= 0.20F * Mth.sin(p * Mth.PI);

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
            // Фаза 3: идёт к водоёму в скромном купальнике
            this.blush.visible = true;
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
            // Фаза 5: отдыхает без брони (если водоёма 3x3+ не нашлось)
            this.blush.visible = true;
            this.head.zRot += 0.10F;
            this.rightArm.xRot -= 0.22F;
            this.rightArm.zRot += 0.14F;
        }
    }
}
