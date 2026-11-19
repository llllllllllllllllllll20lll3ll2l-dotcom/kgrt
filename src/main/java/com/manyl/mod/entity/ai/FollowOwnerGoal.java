package com.manyl.mod.entity.ai;

import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.registry.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * ИИ-цель следования за владельцем, если воин не в режиме ожидания и включено следование.
 */
public class FollowOwnerGoal extends Goal {
    private final BaseGolemEntity golem;
    private final double speedModifier;
    private final float startDistance;
    private final float stopDistance;
    private LivingEntity owner;
    private int recalcPathTicks;

    public FollowOwnerGoal(BaseGolemEntity golem, double speedModifier, float startDistance, float stopDistance) {
        this.golem = golem;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (golem.isDisabled() || golem.isSitting() || golem.isWorking() || golem.isBusyWithRoutine()) return false;
        if (!golem.getOwnerSettings().isFollowOwner()) return false;
        LivingEntity living = golem.getOwnerEntity();
        if (living == null || living.isSpectator()) return false;
        if (golem.distanceToSqr(living) < (double) (startDistance * startDistance)) return false;
        this.owner = living;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (golem.isDisabled() || golem.isSitting() || golem.isWorking() || golem.isBusyWithRoutine()) return false;
        if (owner == null || !owner.isAlive()) return false;
        if (!golem.getOwnerSettings().isFollowOwner()) return false;
        return golem.distanceToSqr(owner) > (double) (stopDistance * stopDistance);
    }

    @Override
    public void start() {
        this.recalcPathTicks = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        golem.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (owner == null) return;
        golem.getLookControl().setLookAt(owner, 10.0F, (float) golem.getMaxHeadXRot());
        if (--recalcPathTicks <= 0) {
            recalcPathTicks = 10;
            double distSq = golem.distanceToSqr(owner);
            if (distSq > 484.0D && golem.level() instanceof ServerLevel sl) {
                double tx = owner.getX() + (golem.getRandom().nextDouble() - 0.5D) * 3.0D;
                double tz = owner.getZ() + (golem.getRandom().nextDouble() - 0.5D) * 3.0D;
                golem.teleportTo(tx, owner.getY(), tz);
                sl.sendParticles(ModParticles.SUMMON_RUNE, tx, owner.getY() + 0.5D, tz, 8, 0.3D, 0.3D, 0.3D, 0.02D);
            } else {
                golem.getNavigation().moveTo(owner, speedModifier);
            }
        }
    }
}
