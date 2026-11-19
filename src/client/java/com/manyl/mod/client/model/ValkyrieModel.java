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
 * Мастер-HD 3D-модель 2-блочного Голема-Воительницы (187 скульптурных элементов, 24x геометрия, текстура 4096x4096).
 */
public class ValkyrieModel extends EntityModel<GolemRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ManylMod.id("valkyrie_golem"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart helmet;
    private final ModelPart blush;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart armorHead;
    private final ModelPart armorChest;
    private final ModelPart armorLegsR;
    private final ModelPart armorLegsL;
    private final ModelPart armorFeetR;
    private final ModelPart armorFeetL;

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
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightLeg = this.root.getChild("right_leg");
        this.leftLeg = this.root.getChild("left_leg");
        this.armorHead = this.head.getChild("armor_head");
        this.armorChest = this.body.getChild("armor_chest");
        this.armorLegsR = this.rightLeg.getChild("armor_legs_r");
        this.armorLegsL = this.leftLeg.getChild("armor_legs_l");
        this.armorFeetR = this.rightLeg.getChild("armor_feet_r");
        this.armorFeetL = this.leftLeg.getChild("armor_feet_l");

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
        PartDefinition root = rootPart.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition rightLeg = buildRightLeg(root);
        rightLeg.addOrReplaceChild("armor_legs_r",
                CubeListBuilder.create().texOffs(3132, 206).addBox(-43F, 4F, -44F, 86.0F, 92.0F, 88.0F),
                PartPose.ZERO);
        rightLeg.addOrReplaceChild("armor_feet_r",
                CubeListBuilder.create().texOffs(3020, 4).addBox(-42F, 206F, -68F, 84.0F, 56.0F, 118.0F),
                PartPose.ZERO);
        PartDefinition leftLeg = buildLeftLeg(root);
        leftLeg.addOrReplaceChild("armor_legs_l",
                CubeListBuilder.create().texOffs(2824, 206).addBox(-43F, 4F, -44F, 86.0F, 92.0F, 88.0F),
                PartPose.ZERO);
        leftLeg.addOrReplaceChild("armor_feet_l",
                CubeListBuilder.create().texOffs(3360, 4).addBox(-42F, 206F, -68F, 84.0F, 56.0F, 118.0F),
                PartPose.ZERO);
        PartDefinition body = buildBody(root);
        body.addOrReplaceChild("armor_chest",
                CubeListBuilder.create().texOffs(888, 4).addBox(-86F, -252F, -58F, 172.0F, 92.0F, 116.0F),
                PartPose.ZERO);
        buildBackpack(body);
        PartDefinition head = buildHead(body);
        head.addOrReplaceChild("armor_head",
                CubeListBuilder.create().texOffs(2372, 206).addBox(-62F, -142F, -62F, 124.0F, 46.0F, 124.0F),
                PartPose.ZERO);
        buildRightArm(body);
        buildLeftArm(body);
        return LayerDefinition.create(mesh, 4096, 4096);
    }

    private static PartDefinition buildRightLeg(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(3132, 206).addBox(-38F, 0F, -38F, 76.0F, 56.0F, 76.0F);
        b.texOffs(1924, 346).addBox(-34F, 54F, -34F, 68.0F, 46.0F, 68.0F);
        b.texOffs(1252, 586).addBox(-42F, -4F, -36F, 84.0F, 16.0F, 72.0F);
        b.texOffs(3696, 680).addBox(-30F, 92F, -46F, 60.0F, 38.0F, 16.0F);
        b.texOffs(1804, 754).addBox(-24F, 98F, -54F, 48.0F, 26.0F, 10.0F);
        b.texOffs(2020, 806).addBox(-12F, 103F, -60F, 24.0F, 16.0F, 8.0F);
        b.texOffs(3612, 806).addBox(-6F, 106F, -64F, 12.0F, 10.0F, 5.0F);
        b.texOffs(1996, 476).addBox(-38F, 118F, -38F, 76.0F, 18.0F, 76.0F);
        b.texOffs(2468, 4).addBox(-34F, 134F, -34F, 68.0F, 74.0F, 68.0F);
        b.texOffs(3836, 586).addBox(-26F, 140F, -40F, 52.0F, 62.0F, 12.0F);
        b.texOffs(2604, 680).addBox(-6F, 142F, -44F, 12.0F, 56.0F, 6.0F);
        b.texOffs(1852, 680).addBox(-24F, 144F, 26F, 48.0F, 52.0F, 12.0F);
        b.texOffs(3688, 806).addBox(-22F, 144F, -42F, 6.0F, 6.0F, 4.0F);
        b.texOffs(3712, 806).addBox(16F, 144F, -42F, 6.0F, 6.0F, 4.0F);
        b.texOffs(2304, 476).addBox(-32F, 200F, -40F, 64.0F, 14.0F, 80.0F);
        b.texOffs(3020, 4).addBox(-34F, 212F, -54F, 68.0F, 38.0F, 100.0F);
        b.texOffs(912, 346).addBox(-38F, 250F, -62F, 76.0F, 14.0F, 106.0F);
        b.texOffs(2856, 680).addBox(-28F, 224F, -78F, 56.0F, 32.0F, 28.0F);
        b.texOffs(2960, 754).addBox(-20F, 230F, -86F, 40.0F, 22.0F, 12.0F);
        b.texOffs(1080, 754).addBox(-22F, 214F, -60F, 44.0F, 16.0F, 22.0F);
        b.texOffs(1712, 680).addBox(-24F, 218F, 34F, 48.0F, 46.0F, 20.0F);
        return parent.addOrReplaceChild("right_leg", b, PartPose.offset(-42F, -240F, 0F));
    }

    private static PartDefinition buildLeftLeg(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(2824, 206).addBox(-38F, 0F, -38F, 76.0F, 56.0F, 76.0F);
        b.texOffs(1648, 346).addBox(-34F, 54F, -34F, 68.0F, 46.0F, 68.0F);
        b.texOffs(936, 586).addBox(-42F, -4F, -36F, 84.0F, 16.0F, 72.0F);
        b.texOffs(3540, 680).addBox(-30F, 92F, -46F, 60.0F, 38.0F, 16.0F);
        b.texOffs(1924, 754).addBox(-24F, 98F, -54F, 48.0F, 26.0F, 10.0F);
        b.texOffs(2088, 806).addBox(-12F, 103F, -60F, 24.0F, 16.0F, 8.0F);
        b.texOffs(3650, 806).addBox(-6F, 106F, -64F, 12.0F, 10.0F, 5.0F);
        b.texOffs(1688, 476).addBox(-38F, 118F, -38F, 76.0F, 18.0F, 76.0F);
        b.texOffs(2744, 4).addBox(-34F, 134F, -34F, 68.0F, 74.0F, 68.0F);
        b.texOffs(3704, 586).addBox(-26F, 140F, -40F, 52.0F, 62.0F, 12.0F);
        b.texOffs(2644, 680).addBox(-6F, 142F, -44F, 12.0F, 56.0F, 6.0F);
        b.texOffs(1976, 680).addBox(-24F, 144F, 26F, 48.0F, 52.0F, 12.0F);
        b.texOffs(3736, 806).addBox(-22F, 144F, -42F, 6.0F, 6.0F, 4.0F);
        b.texOffs(3760, 806).addBox(16F, 144F, -42F, 6.0F, 6.0F, 4.0F);
        b.texOffs(2596, 476).addBox(-32F, 200F, -40F, 64.0F, 14.0F, 80.0F);
        b.texOffs(3360, 4).addBox(-34F, 212F, -54F, 68.0F, 38.0F, 100.0F);
        b.texOffs(1280, 346).addBox(-38F, 250F, -62F, 76.0F, 14.0F, 106.0F);
        b.texOffs(2684, 680).addBox(-28F, 224F, -78F, 56.0F, 32.0F, 28.0F);
        b.texOffs(2852, 754).addBox(-20F, 230F, -86F, 40.0F, 22.0F, 12.0F);
        b.texOffs(1216, 754).addBox(-22F, 214F, -60F, 44.0F, 16.0F, 22.0F);
        b.texOffs(1572, 680).addBox(-24F, 218F, 34F, 48.0F, 46.0F, 20.0F);
        return parent.addOrReplaceChild("left_leg", b, PartPose.offset(42F, -240F, 0F));
    }

    private static PartDefinition buildBody(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(948, 476).addBox(-34F, -290F, -30F, 68.0F, 38.0F, 60.0F);
        b.texOffs(268, 346).addBox(-68F, -264F, -52F, 136.0F, 20.0F, 104.0F);
        b.texOffs(1340, 806).addBox(-52F, -252F, -56F, 104.0F, 14.0F, 14.0F);
        b.texOffs(888, 4).addBox(-80F, -248F, -50F, 160.0F, 56.0F, 100.0F);
        b.texOffs(2520, 346).addBox(-62F, -192F, -42F, 124.0F, 26.0F, 84.0F);
        b.texOffs(4, 586).addBox(-46F, -166F, -34F, 92.0F, 22.0F, 68.0F);
        b.texOffs(1776, 586).addBox(-72F, -238F, -70F, 62.0F, 62.0F, 24.0F);
        b.texOffs(1952, 586).addBox(10F, -238F, -70F, 62.0F, 62.0F, 24.0F);
        b.texOffs(3292, 680).addBox(-66F, -230F, -76F, 50.0F, 46.0F, 10.0F);
        b.texOffs(3416, 680).addBox(16F, -230F, -76F, 50.0F, 46.0F, 10.0F);
        b.texOffs(3196, 586).addBox(-30F, -224F, -80F, 60.0F, 62.0F, 16.0F);
        b.texOffs(3992, 680).addBox(-20F, -214F, -86F, 40.0F, 42.0F, 10.0F);
        b.texOffs(188, 806).addBox(-12F, -206F, -91F, 24.0F, 26.0F, 6.0F);
        b.texOffs(2124, 206).addBox(-34F, -144F, -27F, 68.0F, 80.0F, 54.0F);
        b.texOffs(4, 680).addBox(-37F, -132F, -30F, 74.0F, 10.0F, 60.0F);
        b.texOffs(748, 680).addBox(-36F, -104F, -29F, 72.0F, 10.0F, 58.0F);
        b.texOffs(276, 680).addBox(-37F, -78F, -30F, 74.0F, 10.0F, 60.0F);
        b.texOffs(2628, 586).addBox(-42F, -66F, -33F, 84.0F, 18.0F, 66.0F);
        b.texOffs(344, 754).addBox(-24F, -68F, -44F, 48.0F, 30.0F, 14.0F);
        b.texOffs(1948, 806).addBox(-13F, -60F, -50F, 26.0F, 16.0F, 8.0F);
        b.texOffs(536, 476).addBox(-62F, -48F, -40F, 124.0F, 20.0F, 80.0F);
        b.texOffs(4, 206).addBox(-82F, -28F, -50F, 164.0F, 36.0F, 100.0F);
        b.texOffs(3440, 206).addBox(-30F, -34F, -58F, 60.0F, 114.0F, 14.0F);
        b.texOffs(3316, 754).addBox(-36F, 72F, -60F, 72.0F, 14.0F, 18.0F);
        b.texOffs(752, 346).addBox(-32F, -34F, 46F, 64.0F, 108.0F, 14.0F);
        b.texOffs(3124, 754).addBox(-38F, 66F, 44F, 76.0F, 14.0F, 18.0F);
        b.texOffs(648, 4).addBox(-90F, -28F, -50F, 18.0F, 60.0F, 100.0F);
        b.texOffs(1668, 4).addBox(-102F, 20F, -54F, 18.0F, 48.0F, 108.0F);
        b.texOffs(3592, 206).addBox(-105F, 62F, -56F, 18.0F, 14.0F, 112.0F);
        b.texOffs(3488, 586).addBox(-108F, 10F, -20F, 12.0F, 36.0F, 40.0F);
        b.texOffs(72, 754).addBox(-112F, 16F, -12F, 8.0F, 22.0F, 24.0F);
        b.texOffs(408, 4).addBox(72F, -28F, -50F, 18.0F, 60.0F, 100.0F);
        b.texOffs(1412, 4).addBox(84F, 20F, -54F, 18.0F, 48.0F, 108.0F);
        b.texOffs(4, 346).addBox(87F, 62F, -56F, 18.0F, 14.0F, 112.0F);
        b.texOffs(3596, 586).addBox(92F, 10F, -20F, 12.0F, 36.0F, 40.0F);
        b.texOffs(208, 754).addBox(98F, 16F, -12F, 8.0F, 22.0F, 24.0F);
        return parent.addOrReplaceChild("body", b, PartPose.offset(0F, -240F, 0F));
    }

    private static void buildBackpack(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(3700, 4).addBox(-42F, -48F, -12F, 84.0F, 102.0F, 36.0F);
        b.texOffs(2128, 586).addBox(-28F, -34F, 20F, 56.0F, 72.0F, 14.0F);
        b.texOffs(3852, 680).addBox(-16F, -20F, 28F, 32.0F, 44.0F, 10.0F);
        b.texOffs(3068, 754).addBox(-10F, -12F, 34F, 20.0F, 28.0F, 6.0F);
        b.texOffs(2356, 680).addBox(-74F, -50F, 8F, 42.0F, 44.0F, 18.0F);
        b.texOffs(2100, 680).addBox(-108F, -82F, 14F, 46.0F, 46.0F, 16.0F);
        b.texOffs(3028, 680).addBox(-140F, -114F, 18F, 48.0F, 42.0F, 16.0F);
        b.texOffs(592, 754).addBox(-168F, -138F, 22F, 44.0F, 30.0F, 14.0F);
        b.texOffs(2612, 754).addBox(-194F, -148F, 24F, 46.0F, 22.0F, 12.0F);
        b.texOffs(2940, 806).addBox(-202F, -142F, 28F, 50.0F, 12.0F, 8.0F);
        b.texOffs(2472, 754).addBox(-168F, -104F, 20F, 56.0F, 22.0F, 12.0F);
        b.texOffs(2424, 806).addBox(-174F, -98F, 24F, 56.0F, 12.0F, 8.0F);
        b.texOffs(2188, 754).addBox(-142F, -68F, 16F, 58.0F, 22.0F, 12.0F);
        b.texOffs(2292, 806).addBox(-146F, -62F, 20F, 56.0F, 12.0F, 8.0F);
        b.texOffs(3636, 754).addBox(-116F, -34F, 14F, 54.0F, 20.0F, 12.0F);
        b.texOffs(3060, 806).addBox(-118F, -28F, 18F, 50.0F, 11.0F, 8.0F);
        b.texOffs(3892, 754).addBox(-88F, -8F, 10F, 46.0F, 20.0F, 12.0F);
        b.texOffs(3300, 806).addBox(-88F, -4F, 14F, 40.0F, 10.0F, 8.0F);
        b.texOffs(2480, 680).addBox(32F, -50F, 8F, 42.0F, 44.0F, 18.0F);
        b.texOffs(2228, 680).addBox(62F, -82F, 14F, 46.0F, 46.0F, 16.0F);
        b.texOffs(3160, 680).addBox(92F, -114F, 18F, 48.0F, 42.0F, 16.0F);
        b.texOffs(472, 754).addBox(124F, -138F, 22F, 44.0F, 30.0F, 14.0F);
        b.texOffs(2732, 754).addBox(148F, -148F, 24F, 46.0F, 22.0F, 12.0F);
        b.texOffs(2820, 806).addBox(152F, -142F, 28F, 50.0F, 12.0F, 8.0F);
        b.texOffs(2332, 754).addBox(112F, -104F, 20F, 56.0F, 22.0F, 12.0F);
        b.texOffs(2556, 806).addBox(118F, -98F, 24F, 56.0F, 12.0F, 8.0F);
        b.texOffs(2044, 754).addBox(84F, -68F, 16F, 58.0F, 22.0F, 12.0F);
        b.texOffs(2688, 806).addBox(90F, -62F, 20F, 56.0F, 12.0F, 8.0F);
        b.texOffs(3500, 754).addBox(62F, -34F, 14F, 54.0F, 20.0F, 12.0F);
        b.texOffs(3180, 806).addBox(68F, -28F, 18F, 50.0F, 11.0F, 8.0F);
        b.texOffs(3772, 754).addBox(42F, -8F, 10F, 46.0F, 20.0F, 12.0F);
        b.texOffs(3400, 806).addBox(48F, -4F, 14F, 40.0F, 10.0F, 8.0F);
        parent.addOrReplaceChild("backpack", b, PartPose.offset(0F, -186F, 54F));
    }

    private static PartDefinition buildHead(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(4, 4).addBox(-50F, -98F, -50F, 100.0F, 98.0F, 100.0F);
        b.texOffs(2156, 806).addBox(-36F, -66F, -54F, 24.0F, 14.0F, 8.0F);
        b.texOffs(2224, 806).addBox(12F, -66F, -54F, 24.0F, 14.0F, 8.0F);
        b.texOffs(2360, 346).addBox(-58F, -60F, -54F, 18.0F, 54.0F, 60.0F);
        b.texOffs(2200, 346).addBox(40F, -60F, -54F, 18.0F, 54.0F, 60.0F);
        b.texOffs(1488, 754).addBox(-38F, -20F, -56F, 76.0F, 20.0F, 16.0F);
        b.texOffs(784, 586).addBox(-26F, -100F, 48F, 52.0F, 68.0F, 22.0F);
        b.texOffs(3352, 586).addBox(-22F, -36F, 60F, 44.0F, 56.0F, 22.0F);
        b.texOffs(932, 754).addBox(-24F, 12F, 68F, 48.0F, 14.0F, 24.0F);
        b.texOffs(2512, 586).addBox(-18F, 24F, 76F, 36.0F, 66.0F, 20.0F);
        b.texOffs(1676, 754).addBox(-20F, 82F, 78F, 40.0F, 14.0F, 22.0F);
        b.texOffs(3968, 586).addBox(-14F, 94F, 84F, 28.0F, 56.0F, 18.0F);
        b.texOffs(4, 754).addBox(-9F, 144F, 88F, 18.0F, 34.0F, 14.0F);
        PartDefinition headPart = parent.addOrReplaceChild("head", b, PartPose.offset(0F, -288F, 0F));

        // Съёмный крылатый шлем Валькирии
        CubeListBuilder h = CubeListBuilder.create();
        h.texOffs(3940, 680).addBox(-7F, -86F, -58F, 14.0F, 44.0F, 10.0F);
        h.texOffs(2372, 206).addBox(-56F, -112F, -56F, 112.0F, 20.0F, 112.0F);
        h.texOffs(712, 754).addBox(-14F, -128F, -62F, 28.0F, 32.0F, 12.0F);
        h.texOffs(1900, 806).addBox(-8F, -122F, -66F, 16.0F, 20.0F, 6.0F);
        h.texOffs(536, 206).addBox(-52F, -140F, -52F, 104.0F, 32.0F, 104.0F);
        h.texOffs(4, 476).addBox(-44F, -154F, -44F, 88.0F, 18.0F, 88.0F);
        h.texOffs(3064, 586).addBox(-72F, -108F, -20F, 20.0F, 40.0F, 44.0F);
        h.texOffs(2392, 586).addBox(-84F, -144F, -6F, 20.0F, 48.0F, 38.0F);
        h.texOffs(548, 680).addBox(-88F, -176F, 10F, 18.0F, 40.0F, 30.0F);
        h.texOffs(276, 754).addBox(-86F, -166F, 16F, 12.0F, 26.0F, 20.0F);
        h.texOffs(864, 754).addBox(-90F, -192F, 20F, 12.0F, 24.0F, 20.0F);
        h.texOffs(2932, 586).addBox(52F, -108F, -20F, 20.0F, 40.0F, 44.0F);
        h.texOffs(2272, 586).addBox(64F, -144F, -6F, 20.0F, 48.0F, 38.0F);
        h.texOffs(648, 680).addBox(70F, -176F, 10F, 18.0F, 40.0F, 30.0F);
        h.texOffs(140, 754).addBox(74F, -166F, 16F, 12.0F, 26.0F, 20.0F);
        h.texOffs(796, 754).addBox(78F, -192F, 20F, 12.0F, 24.0F, 20.0F);
        h.texOffs(1568, 586).addBox(-14F, -168F, -36F, 28.0F, 14.0F, 74.0F);
        h.texOffs(360, 476).addBox(-10F, -204F, -30F, 20.0F, 38.0F, 66.0F);
        headPart.addOrReplaceChild("helmet", h, PartPose.ZERO);

        // Лёгкий розовый румянец на щеках (UV 3856, 692)
        CubeListBuilder blushBuilder = CubeListBuilder.create();
        blushBuilder.texOffs(3856, 692).addBox(-42F, -48F, -53F, 16.0F, 8.0F, 3.0F);
        blushBuilder.texOffs(3856, 692).addBox(26F, -48F, -53F, 16.0F, 8.0F, 3.0F);
        headPart.addOrReplaceChild("blush", blushBuilder, PartPose.ZERO);

        return headPart;
    }

    private static void buildRightArm(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(1764, 206).addBox(-64F, -36F, -44F, 90.0F, 46.0F, 88.0F);
        b.texOffs(3340, 346).addBox(-70F, -6F, -48F, 102.0F, 14.0F, 96.0F);
        b.texOffs(556, 586).addBox(-74F, -50F, -34F, 42.0F, 20.0F, 70.0F);
        b.texOffs(4, 806).addBox(-36F, -30F, -50F, 32.0F, 20.0F, 12.0F);
        b.texOffs(3556, 806).addBox(-30F, -25F, -54F, 20.0F, 10.0F, 6.0F);
        b.texOffs(1180, 206).addBox(-44F, 12F, -30F, 50.0F, 76.0F, 60.0F);
        b.texOffs(1204, 680).addBox(-40F, 72F, -26F, 42.0F, 16.0F, 52.0F);
        b.texOffs(2888, 476).addBox(-54F, 78F, -36F, 70.0F, 22.0F, 72.0F);
        b.texOffs(2196, 4).addBox(-50F, 98F, -36F, 62.0F, 82.0F, 72.0F);
        b.texOffs(3740, 346).addBox(-62F, 108F, -24F, 16.0F, 60.0F, 48.0F);
        b.texOffs(1396, 680).addBox(-67F, 118F, -15F, 12.0F, 38.0F, 30.0F);
        b.texOffs(3760, 476).addBox(-54F, 174F, -38F, 70.0F, 16.0F, 76.0F);
        b.texOffs(1208, 476).addBox(-46F, 188F, -32F, 54.0F, 34.0F, 64.0F);
        b.texOffs(1580, 806).addBox(-50F, 194F, -36F, 62.0F, 12.0F, 16.0F);
        b.texOffs(462, 806).addBox(-42F, 220F, -26F, 22.0F, 20.0F, 11.0F);
        b.texOffs(944, 806).addBox(-18F, 220F, -26F, 20.0F, 18.0F, 11.0F);
        b.texOffs(392, 806).addBox(-42F, 220F, -12F, 22.0F, 20.0F, 11.0F);
        b.texOffs(1274, 806).addBox(-18F, 220F, -12F, 20.0F, 18.0F, 11.0F);
        b.texOffs(322, 806).addBox(-42F, 220F, 2F, 22.0F, 20.0F, 11.0F);
        b.texOffs(1208, 806).addBox(-18F, 220F, 2F, 20.0F, 18.0F, 11.0F);
        b.texOffs(532, 806).addBox(-42F, 220F, 16F, 22.0F, 20.0F, 11.0F);
        b.texOffs(1010, 806).addBox(-18F, 220F, 16F, 20.0F, 18.0F, 11.0F);
        b.texOffs(1352, 754).addBox(-4F, 200F, -30F, 16.0F, 22.0F, 16.0F);
        parent.addOrReplaceChild("right_arm", b, PartPose.offset(-99F, -246F, 0F));
    }

    private static void buildLeftArm(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(1404, 206).addBox(-26F, -36F, -44F, 90.0F, 46.0F, 88.0F);
        b.texOffs(2940, 346).addBox(-32F, -6F, -48F, 102.0F, 14.0F, 96.0F);
        b.texOffs(328, 586).addBox(26F, -50F, -34F, 42.0F, 20.0F, 70.0F);
        b.texOffs(96, 806).addBox(2F, -30F, -50F, 32.0F, 20.0F, 12.0F);
        b.texOffs(3500, 806).addBox(8F, -25F, -54F, 20.0F, 10.0F, 6.0F);
        b.texOffs(956, 206).addBox(-6F, 12F, -30F, 50.0F, 76.0F, 60.0F);
        b.texOffs(1012, 680).addBox(-2F, 72F, -26F, 42.0F, 16.0F, 52.0F);
        b.texOffs(3176, 476).addBox(-16F, 78F, -36F, 70.0F, 22.0F, 72.0F);
        b.texOffs(1924, 4).addBox(-12F, 98F, -36F, 62.0F, 82.0F, 72.0F);
        b.texOffs(3872, 346).addBox(46F, 108F, -24F, 16.0F, 60.0F, 48.0F);
        b.texOffs(1484, 680).addBox(52F, 118F, -15F, 12.0F, 38.0F, 30.0F);
        b.texOffs(3464, 476).addBox(-16F, 174F, -38F, 70.0F, 16.0F, 76.0F);
        b.texOffs(1448, 476).addBox(-8F, 188F, -32F, 54.0F, 34.0F, 64.0F);
        b.texOffs(1740, 806).addBox(-12F, 194F, -36F, 62.0F, 12.0F, 16.0F);
        b.texOffs(742, 806).addBox(-4F, 220F, -26F, 22.0F, 20.0F, 11.0F);
        b.texOffs(812, 806).addBox(20F, 220F, -26F, 20.0F, 18.0F, 11.0F);
        b.texOffs(672, 806).addBox(-4F, 220F, -12F, 22.0F, 20.0F, 11.0F);
        b.texOffs(1142, 806).addBox(20F, 220F, -12F, 20.0F, 18.0F, 11.0F);
        b.texOffs(602, 806).addBox(-4F, 220F, 2F, 22.0F, 20.0F, 11.0F);
        b.texOffs(1076, 806).addBox(20F, 220F, 2F, 20.0F, 18.0F, 11.0F);
        b.texOffs(252, 806).addBox(-4F, 220F, 16F, 22.0F, 20.0F, 11.0F);
        b.texOffs(878, 806).addBox(20F, 220F, 16F, 20.0F, 18.0F, 11.0F);
        b.texOffs(1420, 754).addBox(-20F, 200F, -30F, 16.0F, 22.0F, 16.0F);
        parent.addOrReplaceChild("left_arm", b, PartPose.offset(99F, -246F, 0F));
    }

    @Override
    public void setupAnim(GolemRenderState state) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.helmet.visible = true;
        this.helmet.xScale = 1.0F;
        this.helmet.yScale = 1.0F;
        this.helmet.zScale = 1.0F;
        this.blush.visible = false;

        this.armorHead.visible = state.hasHeadArmor && !state.isDaydreaming;
        this.armorChest.visible = state.hasChestArmor;
        this.armorLegsR.visible = state.hasLegsArmor;
        this.armorLegsL.visible = state.hasLegsArmor;
        this.armorFeetR.visible = state.hasFeetArmor;
        this.armorFeetL.visible = state.hasFeetArmor;

        if (state.isDisabled) {
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
        this.walkAnim.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 2.2F, 2.4F);
        this.workAnim.apply(state.workAnimationState, state.ageInTicks, 1.0F);
        this.attackAnim.apply(state.attackAnimationState, state.ageInTicks, 1.0F);
        this.carryAnim.apply(state.carryAnimationState, state.ageInTicks, 1.0F);
        this.sitAnim.apply(state.sitAnimationState, state.ageInTicks, 1.0F);
        this.summonAnim.apply(state.summonAnimationState, state.ageInTicks, 1.0F);
        this.eatAnim.apply(state.eatAnimationState, state.ageInTicks, 1.0F);

        if (state.isDaydreaming) {
            int dt = state.daydreamTicks;
            float p;
            if (dt > 142) {
                p = (160 - dt) / 18.0F;
            } else if (dt < 18) {
                p = dt / 18.0F;
            } else {
                p = 1.0F;
            }
            p = Mth.clamp(p, 0.0F, 1.0F);

            this.helmet.y = -110.0F * p;
            this.helmet.z = 70.0F * p;
            this.helmet.xRot = -0.45F * p;
            float sc = Math.max(0.01F, 1.0F - p * 0.99F);
            this.helmet.xScale = sc;
            this.helmet.yScale = sc;
            this.helmet.zScale = sc;
            this.helmet.visible = (p < 0.92F);
            this.blush.visible = (p > 0.35F);

            this.head.zRot += 0.14F * p;
            this.head.xRot -= 0.08F * p;
            this.rightArm.xRot -= 0.42F * p;
            this.rightArm.zRot += 0.18F * p;
        }
    }
}
