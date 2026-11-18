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
 * Ультра-HD 3D-модель 2-блочного Голема-Воительницы (136 скульптурных элементов, 16x геометрия, текстура 4096x4096).
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
                        .texOffs(3668, 4).addBox(-26F, 0F, -26F, 52.0F, 42.0F, 52.0F)
                        .texOffs(2836, 148).addBox(-23F, 40F, -23F, 46.0F, 36.0F, 46.0F)
                        .texOffs(548, 322).addBox(-28F, -2F, -24F, 56.0F, 12.0F, 48.0F)
                        .texOffs(3640, 322).addBox(-20F, 66F, -32F, 40.0F, 26.0F, 12.0F)
                        .texOffs(1720, 388).addBox(-15F, 70F, -36F, 30.0F, 18.0F, 8.0F)
                        .texOffs(3018, 388).addBox(-6F, 74F, -39F, 12.0F, 10.0F, 5.0F)
                        .texOffs(2516, 244).addBox(-25F, 82F, -25F, 50.0F, 14.0F, 50.0F)
                        .texOffs(2976, 4).addBox(-23F, 94F, -23F, 46.0F, 50.0F, 46.0F)
                        .texOffs(1636, 322).addBox(-18F, 98F, -28F, 36.0F, 42.0F, 10.0F)
                        .texOffs(2604, 322).addBox(-16F, 100F, 18F, 32.0F, 36.0F, 10.0F)
                        .texOffs(1904, 244).addBox(-22F, 138F, -28F, 44.0F, 10.0F, 56.0F)
                        .texOffs(2200, 4).addBox(-24F, 146F, -38F, 48.0F, 30.0F, 70.0F)
                        .texOffs(2324, 148).addBox(-26F, 174F, -44F, 52.0F, 10.0F, 74.0F)
                        .texOffs(2904, 322).addBox(-20F, 154F, -54F, 40.0F, 24.0F, 20.0F)
                        .texOffs(912, 388).addBox(-16F, 148F, -42F, 32.0F, 12.0F, 16.0F)
                        .texOffs(1984, 322).addBox(-18F, 152F, 24F, 36.0F, 32.0F, 16.0F)
                , PartPose.offset(-28F, -160F, 0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(3456, 4).addBox(-26F, 0F, -26F, 52.0F, 42.0F, 52.0F)
                        .texOffs(3024, 148).addBox(-23F, 40F, -23F, 46.0F, 36.0F, 46.0F)
                        .texOffs(336, 322).addBox(-28F, -2F, -24F, 56.0F, 12.0F, 48.0F)
                        .texOffs(3748, 322).addBox(-20F, 66F, -32F, 40.0F, 26.0F, 12.0F)
                        .texOffs(1800, 388).addBox(-15F, 70F, -36F, 30.0F, 18.0F, 8.0F)
                        .texOffs(2980, 388).addBox(-6F, 74F, -39F, 12.0F, 10.0F, 5.0F)
                        .texOffs(2312, 244).addBox(-25F, 82F, -25F, 50.0F, 14.0F, 50.0F)
                        .texOffs(2788, 4).addBox(-23F, 94F, -23F, 46.0F, 50.0F, 46.0F)
                        .texOffs(1540, 322).addBox(-18F, 98F, -28F, 36.0F, 42.0F, 10.0F)
                        .texOffs(2692, 322).addBox(-16F, 100F, 18F, 32.0F, 36.0F, 10.0F)
                        .texOffs(2108, 244).addBox(-22F, 138F, -28F, 44.0F, 10.0F, 56.0F)
                        .texOffs(1960, 4).addBox(-24F, 146F, -38F, 48.0F, 30.0F, 70.0F)
                        .texOffs(2580, 148).addBox(-26F, 174F, -44F, 52.0F, 10.0F, 74.0F)
                        .texOffs(2780, 322).addBox(-20F, 154F, -54F, 40.0F, 24.0F, 20.0F)
                        .texOffs(1012, 388).addBox(-16F, 148F, -42F, 32.0F, 12.0F, 16.0F)
                        .texOffs(1876, 322).addBox(-18F, 152F, 24F, 36.0F, 32.0F, 16.0F)
                , PartPose.offset(28F, -160F, 0F));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(1252, 244).addBox(-24F, -194F, -22F, 48.0F, 26.0F, 44.0F)
                        .texOffs(1992, 148).addBox(-46F, -176F, -36F, 92.0F, 14.0F, 72.0F)
                        .texOffs(4, 4).addBox(-54F, -166F, -34F, 108.0F, 72.0F, 68.0F)
                        .texOffs(3228, 244).addBox(-48F, -158F, -48F, 42.0F, 46.0F, 18.0F)
                        .texOffs(3104, 244).addBox(6F, -158F, -48F, 42.0F, 46.0F, 18.0F)
                        .texOffs(3356, 322).addBox(-44F, -152F, -52F, 34.0F, 34.0F, 8.0F)
                        .texOffs(3444, 322).addBox(10F, -152F, -52F, 34.0F, 34.0F, 8.0F)
                        .texOffs(1272, 322).addBox(-20F, -148F, -54F, 40.0F, 44.0F, 12.0F)
                        .texOffs(3894, 322).addBox(-12F, -140F, -58F, 24.0F, 28.0F, 8.0F)
                        .texOffs(636, 4).addBox(-38F, -94F, -28F, 76.0F, 56.0F, 56.0F)
                        .texOffs(3428, 148).addBox(-42F, -88F, -32F, 84.0F, 12.0F, 64.0F)
                        .texOffs(548, 244).addBox(-40F, -66F, -30F, 80.0F, 12.0F, 60.0F)
                        .texOffs(968, 148).addBox(-48F, -38F, -36F, 96.0F, 18.0F, 72.0F)
                        .texOffs(3532, 322).addBox(-20F, -42F, -44F, 40.0F, 28.0F, 12.0F)
                        .texOffs(2264, 388).addBox(-10F, -34F, -48F, 20.0F, 14.0F, 6.0F)
                        .texOffs(2440, 4).addBox(-52F, -20F, -34F, 104.0F, 28.0F, 68.0F)
                        .texOffs(3212, 148).addBox(-20F, -16F, -40F, 40.0F, 72.0F, 10.0F)
                        .texOffs(2012, 388).addBox(-24F, 50F, -42F, 48.0F, 10.0F, 12.0F)
                        .texOffs(3316, 148).addBox(-22F, -16F, 32F, 44.0F, 68.0F, 10.0F)
                        .texOffs(1880, 388).addBox(-26F, 46F, 30F, 52.0F, 10.0F, 12.0F)
                        .texOffs(1264, 4).addBox(-60F, -18F, -34F, 12.0F, 42.0F, 68.0F)
                        .texOffs(904, 4).addBox(-68F, 14F, -38F, 12.0F, 34.0F, 76.0F)
                        .texOffs(1804, 148).addBox(-70F, 42F, -40F, 12.0F, 10.0F, 80.0F)
                        .texOffs(2092, 322).addBox(-72F, 6F, -12F, 8.0F, 24.0F, 24.0F)
                        .texOffs(1428, 4).addBox(48F, -18F, -34F, 12.0F, 42.0F, 68.0F)
                        .texOffs(1084, 4).addBox(56F, 14F, -38F, 12.0F, 34.0F, 76.0F)
                        .texOffs(1616, 148).addBox(58F, 42F, -40F, 12.0F, 10.0F, 80.0F)
                        .texOffs(2160, 322).addBox(62F, 6F, -12F, 8.0F, 24.0F, 24.0F)
                , PartPose.offset(0F, -160F, 0F));

        body.addOrReplaceChild("backpack",
                CubeListBuilder.create()
                        .texOffs(500, 148).addBox(-28F, -32F, -8F, 56.0F, 68.0F, 24.0F)
                        .texOffs(1176, 322).addBox(-18F, -22F, 14F, 36.0F, 48.0F, 10.0F)
                        .texOffs(3962, 322).addBox(-10F, -12F, 20F, 20.0F, 28.0F, 6.0F)
                        .texOffs(2420, 322).addBox(-52F, -34F, 6F, 30.0F, 32.0F, 14.0F)
                        .texOffs(2228, 322).addBox(-76F, -58F, 10F, 34.0F, 34.0F, 12.0F)
                        .texOffs(3028, 322).addBox(-102F, -82F, 14F, 38.0F, 32.0F, 12.0F)
                        .texOffs(52, 388).addBox(-124F, -96F, 16F, 36.0F, 20.0F, 10.0F)
                        .texOffs(2412, 388).addBox(-130F, -90F, 19F, 38.0F, 12.0F, 6.0F)
                        .texOffs(584, 388).addBox(-102F, -52F, 14F, 44.0F, 18.0F, 10.0F)
                        .texOffs(2504, 388).addBox(-106F, -48F, 17F, 44.0F, 10.0F, 6.0F)
                        .texOffs(1348, 388).addBox(-84F, -24F, 12F, 40.0F, 16.0F, 10.0F)
                        .texOffs(2712, 388).addBox(-84F, -20F, 15F, 36.0F, 9.0F, 6.0F)
                        .texOffs(1632, 388).addBox(-62F, -4F, 8F, 32.0F, 16.0F, 10.0F)
                        .texOffs(2512, 322).addBox(22F, -34F, 6F, 30.0F, 32.0F, 14.0F)
                        .texOffs(2324, 322).addBox(42F, -58F, 10F, 34.0F, 34.0F, 12.0F)
                        .texOffs(3132, 322).addBox(64F, -82F, 14F, 38.0F, 32.0F, 12.0F)
                        .texOffs(148, 388).addBox(88F, -96F, 16F, 36.0F, 20.0F, 10.0F)
                        .texOffs(2320, 388).addBox(92F, -90F, 19F, 38.0F, 12.0F, 6.0F)
                        .texOffs(696, 388).addBox(58F, -52F, 14F, 44.0F, 18.0F, 10.0F)
                        .texOffs(2608, 388).addBox(62F, -48F, 17F, 44.0F, 10.0F, 6.0F)
                        .texOffs(1244, 388).addBox(44F, -24F, 12F, 40.0F, 16.0F, 10.0F)
                        .texOffs(2800, 388).addBox(48F, -20F, 15F, 36.0F, 9.0F, 6.0F)
                        .texOffs(1544, 388).addBox(30F, -4F, 8F, 32.0F, 16.0F, 10.0F)
                , PartPose.offset(0F, -124F, 36F));

        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(360, 4).addBox(-34F, -66F, -34F, 68.0F, 66.0F, 68.0F)
                        .texOffs(2888, 388).addBox(-24F, -44F, -36F, 16.0F, 10.0F, 5.0F)
                        .texOffs(2934, 388).addBox(8F, -44F, -36F, 16.0F, 10.0F, 5.0F)
                        .texOffs(3856, 322).addBox(-5F, -58F, -39F, 10.0F, 30.0F, 7.0F)
                        .texOffs(3728, 148).addBox(-40F, -40F, -36F, 12.0F, 36.0F, 40.0F)
                        .texOffs(3836, 148).addBox(28F, -40F, -36F, 12.0F, 36.0F, 40.0F)
                        .texOffs(1112, 388).addBox(-26F, -14F, -38F, 52.0F, 14.0F, 12.0F)
                        .texOffs(1308, 148).addBox(-38F, -76F, -38F, 76.0F, 14.0F, 76.0F)
                        .texOffs(532, 388).addBox(-8F, -86F, -42F, 16.0F, 22.0F, 8.0F)
                        .texOffs(3164, 4).addBox(-36F, -94F, -36F, 72.0F, 22.0F, 72.0F)
                        .texOffs(832, 244).addBox(-30F, -104F, -30F, 60.0F, 12.0F, 60.0F)
                        .texOffs(904, 322).addBox(-50F, -72F, -14F, 14.0F, 28.0F, 32.0F)
                        .texOffs(248, 322).addBox(-58F, -98F, -4F, 14.0F, 34.0F, 28.0F)
                        .texOffs(1804, 322).addBox(-60F, -120F, 8F, 12.0F, 28.0F, 22.0F)
                        .texOffs(4018, 322).addBox(-58F, -112F, 12F, 8.0F, 18.0F, 14.0F)
                        .texOffs(1000, 322).addBox(36F, -72F, -14F, 14.0F, 28.0F, 32.0F)
                        .texOffs(160, 322).addBox(44F, -98F, -4F, 14.0F, 34.0F, 28.0F)
                        .texOffs(1732, 322).addBox(48F, -120F, 8F, 12.0F, 28.0F, 22.0F)
                        .texOffs(4, 388).addBox(50F, -112F, 12F, 8.0F, 18.0F, 14.0F)
                        .texOffs(760, 322).addBox(-10F, -112F, -24F, 20.0F, 10.0F, 50.0F)
                        .texOffs(1784, 244).addBox(-7F, -136F, -20F, 14.0F, 26.0F, 44.0F)
                        .texOffs(3352, 244).addBox(-18F, -68F, 32F, 36.0F, 48.0F, 16.0F)
                        .texOffs(1380, 322).addBox(-15F, -24F, 40F, 30.0F, 38.0F, 16.0F)
                        .texOffs(808, 388).addBox(-16F, 8F, 46F, 32.0F, 10.0F, 18.0F)
                        .texOffs(1096, 322).addBox(-12F, 16F, 52F, 24.0F, 46.0F, 14.0F)
                        .texOffs(1452, 388).addBox(-14F, 56F, 54F, 28.0F, 10.0F, 16.0F)
                        .texOffs(1476, 322).addBox(-9F, 64F, 58F, 18.0F, 42.0F, 12.0F)
                , PartPose.offset(0F, -192F, 0F));

        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(252, 148).addBox(-44F, -24F, -30F, 62.0F, 32.0F, 60.0F)
                        .texOffs(276, 244).addBox(-48F, -4F, -32F, 70.0F, 10.0F, 64.0F)
                        .texOffs(4, 322).addBox(-50F, -34F, -24F, 28.0F, 14.0F, 48.0F)
                        .texOffs(2200, 388).addBox(-24F, -20F, -34F, 22.0F, 14.0F, 8.0F)
                        .texOffs(816, 148).addBox(-30F, 8F, -20F, 34.0F, 52.0F, 40.0F)
                        .texOffs(2720, 244).addBox(-36F, 52F, -24F, 46.0F, 16.0F, 48.0F)
                        .texOffs(1776, 4).addBox(-34F, 66F, -24F, 42.0F, 56.0F, 48.0F)
                        .texOffs(1164, 244).addBox(-42F, 72F, -16F, 10.0F, 40.0F, 32.0F)
                        .texOffs(3236, 322).addBox(-45F, 80F, -10F, 8.0F, 24.0F, 20.0F)
                        .texOffs(3460, 244).addBox(-36F, 118F, -26F, 46.0F, 10.0F, 52.0F)
                        .texOffs(1440, 244).addBox(-32F, 128F, -22F, 40.0F, 26.0F, 44.0F)
                        .texOffs(244, 388).addBox(-30F, 152F, -20F, 16.0F, 12.0F, 18.0F)
                        .texOffs(316, 388).addBox(-12F, 152F, -20F, 16.0F, 12.0F, 18.0F)
                , PartPose.offset(-66F, -164F, 0F));

        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(4, 148).addBox(-18F, -24F, -30F, 62.0F, 32.0F, 60.0F)
                        .texOffs(4, 244).addBox(-22F, -4F, -32F, 70.0F, 10.0F, 64.0F)
                        .texOffs(3860, 244).addBox(18F, -34F, -24F, 28.0F, 14.0F, 48.0F)
                        .texOffs(2136, 388).addBox(2F, -20F, -34F, 22.0F, 14.0F, 8.0F)
                        .texOffs(664, 148).addBox(-4F, 8F, -20F, 34.0F, 52.0F, 40.0F)
                        .texOffs(2912, 244).addBox(-10F, 52F, -24F, 46.0F, 16.0F, 48.0F)
                        .texOffs(1592, 4).addBox(-8F, 66F, -24F, 42.0F, 56.0F, 48.0F)
                        .texOffs(1076, 244).addBox(32F, 72F, -16F, 10.0F, 40.0F, 32.0F)
                        .texOffs(3296, 322).addBox(36F, 80F, -10F, 8.0F, 24.0F, 20.0F)
                        .texOffs(3660, 244).addBox(-10F, 118F, -26F, 46.0F, 10.0F, 52.0F)
                        .texOffs(1612, 244).addBox(-6F, 128F, -22F, 40.0F, 26.0F, 44.0F)
                        .texOffs(460, 388).addBox(-4F, 152F, -20F, 16.0F, 12.0F, 18.0F)
                        .texOffs(388, 388).addBox(14F, 152F, -20F, 16.0F, 12.0F, 18.0F)
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
