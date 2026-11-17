package com.manyl.mod.client.model;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Ванильные ключевые анимации для всех прислужников-големов Manyl_mod:
 * IDLE (дыхание), WALK (ходьба), WORK (добыча/ферма), ATTACK (удар),
 * CARRY (перенос груза), SIT (ожидание), SUMMON (появление), EAT (поедание еды).
 */
public final class GolemAnimations {

    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(2.4F).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, -0.35F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 6.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2F, KeyframeAnimations.degreeVec(-4.0F, 0.0F, -6.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -3.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(0.8F).looping()
            .addAnimation("right_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(-28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8F, KeyframeAnimations.degreeVec(28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("left_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8F, KeyframeAnimations.degreeVec(-28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(4.0F, 0.0F, -3.5F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(4.0F, 0.0F, 3.5F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.8F, KeyframeAnimations.degreeVec(4.0F, 0.0F, -3.5F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(32.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(32.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4F, KeyframeAnimations.degreeVec(-32.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8F, KeyframeAnimations.degreeVec(32.0F, 0.0F, -4.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition WORK = AnimationDefinition.Builder.withLength(0.75F).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.35F, KeyframeAnimations.degreeVec(22.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-115.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.35F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(-115.0F, 0.0F, 8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-95.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.35F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(-95.0F, 0.0F, -8.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition ATTACK = AnimationDefinition.Builder.withLength(0.55F)
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.22F, KeyframeAnimations.degreeVec(28.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.55F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-135.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.22F, KeyframeAnimations.degreeVec(-25.0F, -10.0F, 5.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.55F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-135.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.22F, KeyframeAnimations.degreeVec(-25.0F, 10.0F, -5.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.55F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition CARRY = AnimationDefinition.Builder.withLength(1.0F).looping()
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-75.0F, -12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(-82.0F, -12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(-75.0F, -12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-75.0F, 12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(-82.0F, 12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(-75.0F, 12.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition SIT = AnimationDefinition.Builder.withLength(2.0F).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 2.2F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 2.2F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(18.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(14.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(-22.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition SUMMON = AnimationDefinition.Builder.withLength(1.1F)
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 13.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, -1.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 360.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-140.0F, 0.0F, 35.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-140.0F, 0.0F, -35.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition EAT = AnimationDefinition.Builder.withLength(0.85F)
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.45F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.65F, KeyframeAnimations.degreeVec(-6.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.85F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-95.0F, 22.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.45F, KeyframeAnimations.degreeVec(-110.0F, 28.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.85F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();
}
