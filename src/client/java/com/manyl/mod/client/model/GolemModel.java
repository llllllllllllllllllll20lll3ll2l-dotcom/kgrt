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
 * Мастер-HD 3D-модель 2-блочного Голема-Воина (208 скульптурных элементов, 24x геометрия, текстура 4096x4096).
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
        buildRightLeg(root);
        buildLeftLeg(root);
        PartDefinition body = buildBody(root);
        buildBackpack(body);
        buildHead(body);
        buildRightArm(body);
        buildLeftArm(body);
        return LayerDefinition.create(mesh, 4096, 4096);
    }

    private static void buildRightLeg(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(1960, 4).addBox(-44F, 0F, -44F, 88.0F, 102.0F, 88.0F);
        b.texOffs(2564, 1030).addBox(-36F, 10F, -52F, 72.0F, 76.0F, 10.0F);
        b.texOffs(3212, 1030).addBox(-32F, 10F, 42F, 64.0F, 74.0F, 10.0F);
        b.texOffs(2664, 910).addBox(-52F, -6F, -40F, 104.0F, 20.0F, 80.0F);
        b.texOffs(514, 1316).addBox(-30F, 16F, -55F, 8.0F, 8.0F, 5.0F);
        b.texOffs(484, 1316).addBox(22F, 16F, -55F, 8.0F, 8.0F, 5.0F);
        b.texOffs(454, 1316).addBox(-30F, 72F, -55F, 8.0F, 8.0F, 5.0F);
        b.texOffs(424, 1316).addBox(22F, 72F, -55F, 8.0F, 8.0F, 5.0F);
        b.texOffs(3564, 280).addBox(-62F, 6F, -38F, 18.0F, 84.0F, 76.0F);
        b.texOffs(1208, 778).addBox(-68F, 16F, -30F, 14.0F, 64.0F, 60.0F);
        b.texOffs(316, 1134).addBox(-71F, 26F, -16F, 10.0F, 42.0F, 32.0F);
        b.texOffs(328, 1316).addBox(-70F, 18F, -26F, 6.0F, 8.0F, 8.0F);
        b.texOffs(360, 1316).addBox(-70F, 68F, -26F, 6.0F, 8.0F, 8.0F);
        b.texOffs(1992, 1134).addBox(-38F, 90F, -58F, 76.0F, 48.0F, 18.0F);
        b.texOffs(1784, 1212).addBox(-32F, 96F, -66F, 64.0F, 36.0F, 12.0F);
        b.texOffs(2436, 1212).addBox(-44F, 102F, -46F, 12.0F, 26.0F, 20.0F);
        b.texOffs(2368, 1212).addBox(32F, 102F, -46F, 12.0F, 26.0F, 20.0F);
        b.texOffs(3204, 1274).addBox(-16F, 104F, -72F, 32.0F, 22.0F, 8.0F);
        b.texOffs(60, 1316).addBox(-8F, 109F, -76F, 16.0F, 12.0F, 5.0F);
        b.texOffs(824, 462).addBox(-42F, 132F, -42F, 84.0F, 74.0F, 84.0F);
        b.texOffs(4, 1134).addBox(-32F, 136F, -50F, 64.0F, 62.0F, 12.0F);
        b.texOffs(3624, 1134).addBox(-10F, 140F, -54F, 20.0F, 54.0F, 6.0F);
        b.texOffs(2792, 1134).addBox(-30F, 142F, 36F, 60.0F, 50.0F, 14.0F);
        b.texOffs(4, 1030).addBox(-46F, 164F, -44F, 92.0F, 12.0F, 88.0F);
        b.texOffs(788, 910).addBox(-42F, 194F, -50F, 84.0F, 16.0F, 100.0F);
        b.texOffs(1004, 280).addBox(-48F, 204F, -64F, 96.0F, 48.0F, 120.0F);
        b.texOffs(3400, 462).addBox(-54F, 236F, -70F, 108.0F, 16.0F, 132.0F);
        b.texOffs(1360, 1134).addBox(-40F, 214F, -86F, 80.0F, 38.0F, 28.0F);
        b.texOffs(3196, 1212).addBox(-28F, 220F, -94F, 56.0F, 28.0F, 12.0F);
        b.texOffs(2696, 1212).addBox(-32F, 204F, -72F, 64.0F, 18.0F, 24.0F);
        b.texOffs(1940, 1212).addBox(-36F, 216F, 54F, 72.0F, 30.0F, 16.0F);
        b.texOffs(3364, 1274).addBox(-8F, 222F, 66F, 16.0F, 16.0F, 12.0F);
        parent.addOrReplaceChild("right_leg", b, PartPose.offset(-60F, -228F, 0F));
    }

    private static void buildLeftLeg(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(1604, 4).addBox(-44F, 0F, -44F, 88.0F, 102.0F, 88.0F);
        b.texOffs(2396, 1030).addBox(-36F, 10F, -52F, 72.0F, 76.0F, 10.0F);
        b.texOffs(3060, 1030).addBox(-32F, 10F, 42F, 64.0F, 74.0F, 10.0F);
        b.texOffs(3036, 910).addBox(-52F, -6F, -40F, 104.0F, 20.0F, 80.0F);
        b.texOffs(814, 1316).addBox(-30F, 16F, -55F, 8.0F, 8.0F, 5.0F);
        b.texOffs(694, 1316).addBox(22F, 16F, -55F, 8.0F, 8.0F, 5.0F);
        b.texOffs(664, 1316).addBox(-30F, 72F, -55F, 8.0F, 8.0F, 5.0F);
        b.texOffs(634, 1316).addBox(22F, 72F, -55F, 8.0F, 8.0F, 5.0F);
        b.texOffs(3756, 280).addBox(44F, 6F, -38F, 18.0F, 84.0F, 76.0F);
        b.texOffs(1360, 778).addBox(50F, 16F, -30F, 14.0F, 64.0F, 60.0F);
        b.texOffs(404, 1134).addBox(53F, 26F, -16F, 10.0F, 42.0F, 32.0F);
        b.texOffs(392, 1316).addBox(54F, 18F, -26F, 6.0F, 8.0F, 8.0F);
        b.texOffs(296, 1316).addBox(54F, 68F, -26F, 6.0F, 8.0F, 8.0F);
        b.texOffs(1800, 1134).addBox(-38F, 90F, -58F, 76.0F, 48.0F, 18.0F);
        b.texOffs(1628, 1212).addBox(-32F, 96F, -66F, 64.0F, 36.0F, 12.0F);
        b.texOffs(2504, 1212).addBox(-44F, 102F, -46F, 12.0F, 26.0F, 20.0F);
        b.texOffs(2300, 1212).addBox(32F, 102F, -46F, 12.0F, 26.0F, 20.0F);
        b.texOffs(3120, 1274).addBox(-16F, 104F, -72F, 32.0F, 22.0F, 8.0F);
        b.texOffs(106, 1316).addBox(-8F, 109F, -76F, 16.0F, 12.0F, 5.0F);
        b.texOffs(1164, 462).addBox(-42F, 132F, -42F, 84.0F, 74.0F, 84.0F);
        b.texOffs(160, 1134).addBox(-32F, 136F, -50F, 64.0F, 62.0F, 12.0F);
        b.texOffs(3680, 1134).addBox(-10F, 140F, -54F, 20.0F, 54.0F, 6.0F);
        b.texOffs(2640, 1134).addBox(-30F, 142F, 36F, 60.0F, 50.0F, 14.0F);
        b.texOffs(3408, 910).addBox(-46F, 164F, -44F, 92.0F, 12.0F, 88.0F);
        b.texOffs(416, 910).addBox(-42F, 194F, -50F, 84.0F, 16.0F, 100.0F);
        b.texOffs(1440, 280).addBox(-48F, 204F, -64F, 96.0F, 48.0F, 120.0F);
        b.texOffs(4, 626).addBox(-54F, 236F, -70F, 108.0F, 16.0F, 132.0F);
        b.texOffs(1580, 1134).addBox(-40F, 214F, -86F, 80.0F, 38.0F, 28.0F);
        b.texOffs(3056, 1212).addBox(-28F, 220F, -94F, 56.0F, 28.0F, 12.0F);
        b.texOffs(2876, 1212).addBox(-32F, 204F, -72F, 64.0F, 18.0F, 24.0F);
        b.texOffs(2120, 1212).addBox(-36F, 216F, 54F, 72.0F, 30.0F, 16.0F);
        b.texOffs(3424, 1274).addBox(-8F, 222F, 66F, 16.0F, 16.0F, 12.0F);
        parent.addOrReplaceChild("left_leg", b, PartPose.offset(60F, -228F, 0F));
    }

    private static PartDefinition buildBody(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(3504, 626).addBox(-52F, -292F, -46F, 104.0F, 36.0F, 92.0F);
        b.texOffs(1504, 462).addBox(-84F, -272F, -66F, 168.0F, 22.0F, 132.0F);
        b.texOffs(3460, 1212).addBox(-70F, -258F, -72F, 140.0F, 18.0F, 20.0F);
        b.texOffs(544, 1316).addBox(-58F, -254F, -76F, 8.0F, 8.0F, 5.0F);
        b.texOffs(574, 1316).addBox(50F, -254F, -76F, 8.0F, 8.0F, 5.0F);
        b.texOffs(4, 4).addBox(-98F, -252F, -66F, 196.0F, 140.0F, 132.0F);
        b.texOffs(1588, 1030).addBox(-90F, -244F, -78F, 82.0F, 72.0F, 16.0F);
        b.texOffs(1788, 1030).addBox(8F, -244F, -78F, 82.0F, 72.0F, 16.0F);
        b.texOffs(1568, 626).addBox(-94F, -248F, 56F, 188.0F, 122.0F, 18.0F);
        b.texOffs(604, 1316).addBox(-82F, -236F, -82F, 8.0F, 8.0F, 5.0F);
        b.texOffs(724, 1316).addBox(-22F, -236F, -82F, 8.0F, 8.0F, 5.0F);
        b.texOffs(784, 1316).addBox(14F, -236F, -82F, 8.0F, 8.0F, 5.0F);
        b.texOffs(754, 1316).addBox(74F, -236F, -82F, 8.0F, 8.0F, 5.0F);
        b.texOffs(2232, 910).addBox(-50F, -222F, -84F, 100.0F, 94.0F, 18.0F);
        b.texOffs(2748, 1274).addBox(-58F, -228F, -88F, 22.0F, 22.0F, 14.0F);
        b.texOffs(2596, 1274).addBox(36F, -228F, -88F, 22.0F, 22.0F, 14.0F);
        b.texOffs(2520, 1274).addBox(-58F, -148F, -88F, 22.0F, 22.0F, 14.0F);
        b.texOffs(2672, 1274).addBox(36F, -148F, -88F, 22.0F, 22.0F, 14.0F);
        b.texOffs(3364, 1030).addBox(-36F, -208F, -92F, 72.0F, 66.0F, 14.0F);
        b.texOffs(1088, 1212).addBox(-24F, -196F, -98F, 48.0F, 42.0F, 10.0F);
        b.texOffs(3288, 1274).addBox(-14F, -186F, -104F, 28.0F, 22.0F, 8.0F);
        b.texOffs(3432, 4).addBox(-78F, -112F, -54F, 156.0F, 72.0F, 108.0F);
        b.texOffs(2564, 626).addBox(-84F, -106F, -60F, 168.0F, 16.0F, 120.0F);
        b.texOffs(1984, 626).addBox(-84F, -80F, -60F, 168.0F, 16.0F, 120.0F);
        b.texOffs(1132, 4).addBox(-108F, -204F, -50F, 16.0F, 94.0F, 100.0F);
        b.texOffs(1368, 4).addBox(92F, -204F, -50F, 16.0F, 94.0F, 100.0F);
        b.texOffs(2936, 280).addBox(-92F, -48F, -64F, 184.0F, 32.0F, 128.0F);
        b.texOffs(2108, 462).addBox(-88F, -40F, -68F, 176.0F, 14.0F, 136.0F);
        b.texOffs(3372, 1134).addBox(-34F, -54F, -76F, 68.0F, 44.0F, 16.0F);
        b.texOffs(3024, 1274).addBox(-18F, -42F, -82F, 36.0F, 20.0F, 10.0F);
        b.texOffs(4, 1316).addBox(-10F, -38F, -86F, 20.0F, 12.0F, 6.0F);
        b.texOffs(368, 1030).addBox(-34F, -16F, -70F, 68.0F, 84.0F, 14.0F);
        b.texOffs(2824, 1274).addBox(-40F, 54F, -72F, 80.0F, 16.0F, 18.0F);
        b.texOffs(2228, 1030).addBox(-34F, -16F, 56F, 68.0F, 72.0F, 14.0F);
        b.texOffs(1876, 280).addBox(-98F, -20F, -52F, 18.0F, 62.0F, 104.0F);
        b.texOffs(2124, 280).addBox(80F, -20F, -52F, 18.0F, 62.0F, 104.0F);
        return parent.addOrReplaceChild("body", b, PartPose.offset(0F, -228F, 0F));
    }

    private static void buildBackpack(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(3076, 4).addBox(-64F, -58F, -14F, 128.0F, 136.0F, 48.0F);
        b.texOffs(3084, 1134).addBox(-52F, -70F, -6F, 104.0F, 22.0F, 38.0F);
        b.texOffs(3736, 1134).addBox(-52F, 68F, -6F, 104.0F, 20.0F, 38.0F);
        b.texOffs(2472, 910).addBox(-40F, -40F, 28F, 80.0F, 96.0F, 14.0F);
        b.texOffs(3540, 1030).addBox(-26F, -26F, 38F, 52.0F, 68.0F, 10.0F);
        b.texOffs(1004, 1212).addBox(-16F, -16F, 44F, 32.0F, 48.0F, 8.0F);
        b.texOffs(4, 462).addBox(-86F, -52F, -10F, 36.0F, 118.0F, 42.0F);
        b.texOffs(592, 1134).addBox(-90F, -62F, -14F, 44.0F, 18.0F, 50.0F);
        b.texOffs(976, 1134).addBox(-90F, 52F, -14F, 44.0F, 18.0F, 50.0F);
        b.texOffs(2732, 1030).addBox(-83F, -16F, -14F, 30.0F, 34.0F, 50.0F);
        b.texOffs(452, 1212).addBox(-90F, -34F, -12F, 44.0F, 10.0F, 46.0F);
        b.texOffs(268, 1212).addBox(-90F, 24F, -12F, 44.0F, 10.0F, 46.0F);
        b.texOffs(136, 1212).addBox(-58F, -92F, 0F, 32.0F, 26.0F, 32.0F);
        b.texOffs(2020, 1274).addBox(-54F, -102F, 4F, 24.0F, 12.0F, 24.0F);
        b.texOffs(164, 462).addBox(50F, -52F, -10F, 36.0F, 118.0F, 42.0F);
        b.texOffs(784, 1134).addBox(46F, -62F, -14F, 44.0F, 18.0F, 50.0F);
        b.texOffs(1168, 1134).addBox(46F, 52F, -14F, 44.0F, 18.0F, 50.0F);
        b.texOffs(2896, 1030).addBox(53F, -16F, -14F, 30.0F, 34.0F, 50.0F);
        b.texOffs(636, 1212).addBox(46F, -34F, -12F, 44.0F, 10.0F, 46.0F);
        b.texOffs(820, 1212).addBox(46F, 24F, -12F, 44.0F, 10.0F, 46.0F);
        b.texOffs(4, 1212).addBox(26F, -92F, 0F, 32.0F, 26.0F, 32.0F);
        b.texOffs(2420, 1274).addBox(30F, -102F, 4F, 24.0F, 12.0F, 24.0F);
        parent.addOrReplaceChild("backpack", b, PartPose.offset(0F, -192F, 78F));
    }

    private static void buildHead(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(664, 4).addBox(-58F, -100F, -58F, 116.0F, 100.0F, 116.0F);
        b.texOffs(1448, 1274).addBox(-40F, -36F, -64F, 8.0F, 30.0F, 8.0F);
        b.texOffs(1412, 1274).addBox(-22F, -36F, -64F, 8.0F, 30.0F, 8.0F);
        b.texOffs(1340, 1274).addBox(-4F, -36F, -64F, 8.0F, 30.0F, 8.0F);
        b.texOffs(1376, 1274).addBox(14F, -36F, -64F, 8.0F, 30.0F, 8.0F);
        b.texOffs(1484, 1274).addBox(32F, -36F, -64F, 8.0F, 30.0F, 8.0F);
        b.texOffs(3988, 1274).addBox(-42F, -74F, -62F, 26.0F, 14.0F, 8.0F);
        b.texOffs(3916, 1274).addBox(16F, -74F, -62F, 26.0F, 14.0F, 8.0F);
        b.texOffs(3484, 1274).addBox(-48F, -86F, -66F, 36.0F, 12.0F, 12.0F);
        b.texOffs(3584, 1274).addBox(12F, -86F, -66F, 36.0F, 12.0F, 12.0F);
        b.texOffs(3900, 626).addBox(-68F, -62F, -64F, 26.0F, 58.0F, 70.0F);
        b.texOffs(4, 778).addBox(42F, -62F, -64F, 26.0F, 58.0F, 70.0F);
        b.texOffs(1400, 1212).addBox(-46F, -26F, -68F, 92.0F, 28.0F, 20.0F);
        b.texOffs(2572, 1212).addBox(-24F, -32F, -72F, 48.0F, 32.0F, 12.0F);
        b.texOffs(3772, 1030).addBox(-74F, -80F, -20F, 12.0F, 38.0F, 38.0F);
        b.texOffs(3668, 1030).addBox(62F, -80F, -20F, 12.0F, 38.0F, 38.0F);
        b.texOffs(3684, 1274).addBox(-78F, -66F, -6F, 6.0F, 12.0F, 12.0F);
        b.texOffs(3724, 1274).addBox(72F, -66F, -6F, 6.0F, 12.0F, 12.0F);
        b.texOffs(3876, 1030).addBox(-10F, -94F, -72F, 20.0F, 62.0F, 14.0F);
        b.texOffs(2372, 280).addBox(-70F, -110F, -70F, 140.0F, 22.0F, 140.0F);
        b.texOffs(324, 462).addBox(-62F, -140F, -62F, 124.0F, 34.0F, 124.0F);
        b.texOffs(912, 778).addBox(-64F, -136F, 52F, 128.0F, 106.0F, 18.0F);
        b.texOffs(1512, 778).addBox(-52F, -154F, -52F, 104.0F, 18.0F, 104.0F);
        b.texOffs(1968, 910).addBox(-18F, -170F, -44F, 36.0F, 18.0F, 94.0F);
        b.texOffs(3336, 1212).addBox(-20F, -176F, 38F, 40.0F, 20.0F, 20.0F);
        b.texOffs(3108, 778).addBox(-12F, -198F, -42F, 24.0F, 32.0F, 86.0F);
        b.texOffs(536, 1030).addBox(-10F, -212F, -34F, 20.0F, 20.0F, 72.0F);
        b.texOffs(2944, 1134).addBox(-8F, -216F, -24F, 16.0F, 10.0F, 52.0F);
        b.texOffs(492, 1134).addBox(-12F, -192F, 38F, 24.0F, 48.0F, 24.0F);
        b.texOffs(3544, 1134).addBox(-10F, -156F, 52F, 20.0F, 42.0F, 18.0F);
        parent.addOrReplaceChild("head", b, PartPose.offset(0F, -288F, 0F));
    }

    private static void buildRightArm(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(2716, 778).addBox(-18F, -24F, -36F, 24.0F, 48.0F, 72.0F);
        b.texOffs(4, 280).addBox(-90F, -46F, -58F, 132.0F, 62.0F, 116.0F);
        b.texOffs(488, 626).addBox(-96F, -12F, -62F, 144.0F, 20.0F, 124.0F);
        b.texOffs(1564, 910).addBox(-80F, -64F, -46F, 108.0F, 22.0F, 92.0F);
        b.texOffs(2184, 1134).addBox(-56F, -72F, -26F, 60.0F, 12.0F, 52.0F);
        b.texOffs(252, 1274).addBox(-50F, -38F, -64F, 48.0F, 24.0F, 14.0F);
        b.texOffs(3764, 1274).addBox(-40F, -33F, -68F, 28.0F, 14.0F, 8.0F);
        b.texOffs(152, 1316).addBox(-84F, -36F, -62F, 10.0F, 10.0F, 6.0F);
        b.texOffs(260, 1316).addBox(26F, -36F, -62F, 10.0F, 10.0F, 6.0F);
        b.texOffs(2736, 462).addBox(-66F, 12F, -40F, 84.0F, 70.0F, 80.0F);
        b.texOffs(1020, 1030).addBox(-60F, 66F, -34F, 72.0F, 20.0F, 68.0F);
        b.texOffs(1932, 778).addBox(-78F, 72F, -46F, 102.0F, 30.0F, 92.0F);
        b.texOffs(724, 1030).addBox(-88F, 68F, -26F, 20.0F, 38.0F, 52.0F);
        b.texOffs(2316, 4).addBox(-72F, 98F, -46F, 96.0F, 92.0F, 92.0F);
        b.texOffs(3324, 626).addBox(-86F, 108F, -32F, 24.0F, 70.0F, 64.0F);
        b.texOffs(2108, 1030).addBox(-92F, 118F, -20F, 18.0F, 48.0F, 40.0F);
        b.texOffs(4, 910).addBox(-78F, 182F, -48F, 108.0F, 20.0F, 96.0F);
        b.texOffs(556, 778).addBox(-68F, 200F, -44F, 88.0F, 36.0F, 88.0F);
        b.texOffs(4, 1274).addBox(-74F, 208F, -48F, 100.0F, 16.0F, 22.0F);
        b.texOffs(924, 1274).addBox(-64F, 234F, -36F, 34.0F, 22.0F, 16.0F);
        b.texOffs(1520, 1274).addBox(-22F, 234F, -36F, 32.0F, 20.0F, 16.0F);
        b.texOffs(612, 1274).addBox(-64F, 234F, -16F, 34.0F, 22.0F, 16.0F);
        b.texOffs(1720, 1274).addBox(-22F, 234F, -16F, 32.0F, 20.0F, 16.0F);
        b.texOffs(716, 1274).addBox(-64F, 234F, 4F, 34.0F, 22.0F, 16.0F);
        b.texOffs(1820, 1274).addBox(-22F, 234F, 4F, 32.0F, 20.0F, 16.0F);
        b.texOffs(820, 1274).addBox(-64F, 234F, 24F, 34.0F, 22.0F, 16.0F);
        b.texOffs(1920, 1274).addBox(-22F, 234F, 24F, 32.0F, 20.0F, 16.0F);
        b.texOffs(1208, 1212).addBox(2F, 214F, -42F, 24.0F, 28.0F, 22.0F);
        parent.addOrReplaceChild("right_arm", b, PartPose.offset(-126F, -252F, 0F));
    }

    private static void buildLeftArm(PartDefinition parent) {
        CubeListBuilder b = CubeListBuilder.create();
        b.texOffs(2912, 778).addBox(-6F, -24F, -36F, 24.0F, 48.0F, 72.0F);
        b.texOffs(504, 280).addBox(-42F, -46F, -58F, 132.0F, 62.0F, 116.0F);
        b.texOffs(1028, 626).addBox(-48F, -12F, -62F, 144.0F, 20.0F, 124.0F);
        b.texOffs(1160, 910).addBox(-32F, -64F, -46F, 108.0F, 22.0F, 92.0F);
        b.texOffs(2412, 1134).addBox(-8F, -72F, -26F, 60.0F, 12.0F, 52.0F);
        b.texOffs(380, 1274).addBox(-2F, -38F, -64F, 48.0F, 24.0F, 14.0F);
        b.texOffs(3840, 1274).addBox(8F, -33F, -68F, 28.0F, 14.0F, 8.0F);
        b.texOffs(188, 1316).addBox(-36F, -36F, -62F, 10.0F, 10.0F, 6.0F);
        b.texOffs(224, 1316).addBox(74F, -36F, -62F, 10.0F, 10.0F, 6.0F);
        b.texOffs(3068, 462).addBox(-18F, 12F, -40F, 84.0F, 70.0F, 80.0F);
        b.texOffs(1304, 1030).addBox(-12F, 66F, -34F, 72.0F, 20.0F, 68.0F);
        b.texOffs(2324, 778).addBox(-30F, 72F, -46F, 102.0F, 30.0F, 92.0F);
        b.texOffs(872, 1030).addBox(58F, 68F, -26F, 20.0F, 38.0F, 52.0F);
        b.texOffs(2696, 4).addBox(-24F, 98F, -46F, 96.0F, 92.0F, 92.0F);
        b.texOffs(3144, 626).addBox(62F, 108F, -32F, 24.0F, 70.0F, 64.0F);
        b.texOffs(1988, 1030).addBox(70F, 118F, -20F, 18.0F, 48.0F, 40.0F);
        b.texOffs(3332, 778).addBox(-30F, 182F, -48F, 108.0F, 20.0F, 96.0F);
        b.texOffs(200, 778).addBox(-20F, 200F, -44F, 88.0F, 36.0F, 88.0F);
        b.texOffs(3784, 1212).addBox(-26F, 208F, -48F, 100.0F, 16.0F, 22.0F);
        b.texOffs(1028, 1274).addBox(-16F, 234F, -36F, 34.0F, 22.0F, 16.0F);
        b.texOffs(2120, 1274).addBox(26F, 234F, -36F, 32.0F, 20.0F, 16.0F);
        b.texOffs(1236, 1274).addBox(-16F, 234F, -16F, 34.0F, 22.0F, 16.0F);
        b.texOffs(2320, 1274).addBox(26F, 234F, -16F, 32.0F, 20.0F, 16.0F);
        b.texOffs(508, 1274).addBox(-16F, 234F, 4F, 34.0F, 22.0F, 16.0F);
        b.texOffs(2220, 1274).addBox(26F, 234F, 4F, 32.0F, 20.0F, 16.0F);
        b.texOffs(1132, 1274).addBox(-16F, 234F, 24F, 34.0F, 22.0F, 16.0F);
        b.texOffs(1620, 1274).addBox(26F, 234F, 24F, 32.0F, 20.0F, 16.0F);
        b.texOffs(1304, 1212).addBox(-36F, 214F, -42F, 24.0F, 28.0F, 22.0F);
        parent.addOrReplaceChild("left_arm", b, PartPose.offset(126F, -252F, 0F));
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
