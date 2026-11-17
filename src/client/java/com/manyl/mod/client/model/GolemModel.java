package com.manyl.mod.client.model;

import com.manyl.mod.ManylMod;
import com.manyl.mod.client.renderer.GolemRenderState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Ванильная 3D-модель прислужника-голема (высота ~0.81 блока),
 * полностью синхронизированная по пивотам, кубам и UV 64x64 с нашими .bbmodel и текстурами.
 */
public class GolemModel extends EntityModel<GolemRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ManylMod.id("golem"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;

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

        this.idleAnim = GolemAnimations.IDLE.bake(root);
        this.walkAnim = GolemAnimations.WALK.bake(root);
        this.workAnim = GolemAnimations.WORK.bake(root);
        this.attackAnim = GolemAnimations.ATTACK.bake(root);
        this.carryAnim = GolemAnimations.CARRY.bake(root);
        this.sitAnim = GolemAnimations.SIT.bake(root);
        this.summonAnim = GolemAnimations.SUMMON.bake(root);
        this.eatAnim = GolemAnimations.EAT.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        PartDefinition root = rootPart.addOrReplaceChild("root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Ноги (4x3x4) с опорой на y=24
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offset(-2.0F, 21.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(17, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offset(2.0F, 21.0F, 0.0F));

        // Торс (8x5x6)
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 12).addBox(-4.0F, -5.0F, -3.0F, 8.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        // Ранец / корзина / сундучок на спине (6x4x3)
        body.addOrReplaceChild("backpack",
                CubeListBuilder.create().texOffs(32, 12).addBox(-3.0F, -2.0F, 0.0F, 6.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, -2.5F, 3.0F));

        // Голова (6x5x6) + шлем/шляпа (8x3x8) + гребень/кристалл/антенна (2x3x2)
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 5.0F, 6.0F)
                        .texOffs(32, 0).addBox(-4.0F, -6.5F, -4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.15F))
                        .texOffs(34, 34).addBox(-1.0F, -9.2F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -5.0F, 0.0F));

        // Правая рука + наплечник
        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-3.0F, -0.5F, -1.5F, 3.0F, 6.0F, 3.0F)
                        .texOffs(32, 20).addBox(-3.5F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)),
                PartPose.offset(-4.0F, -4.5F, 0.0F));

        // Левая рука + наплечник
        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(13, 24).addBox(0.0F, -0.5F, -1.5F, 3.0F, 6.0F, 3.0F)
                        .texOffs(48, 20).addBox(-0.5F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.1F)),
                PartPose.offset(4.0F, -4.5F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(GolemRenderState state) {
        super.setupAnim(state);

        // Поворот головы по направлению взгляда
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

        // Базовое дыхание и походка
        this.idleAnim.apply(state.idleAnimationState, state.ageInTicks);
        this.walkAnim.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 2.2F, 2.5F);

        // Состояния действий и эмоций
        this.sitAnim.apply(state.sitAnimationState, state.ageInTicks);
        this.carryAnim.apply(state.carryAnimationState, state.ageInTicks);
        this.workAnim.apply(state.workAnimationState, state.ageInTicks);
        this.attackAnim.apply(state.attackAnimationState, state.ageInTicks);
        this.eatAnim.apply(state.eatAnimationState, state.ageInTicks);
        this.summonAnim.apply(state.summonAnimationState, state.ageInTicks);
    }
}
