package com.manyl.mod.entity;

import com.manyl.mod.entity.ai.FollowOwnerGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Голем-воин (макс. 5 на игрока).
 * Охраняет владельца и патрулирует область, атакует враждебных мобов и нападающих на хозяина.
 */
public class WarriorGolemEntity extends BaseGolemEntity {
    public WarriorGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public GolemRole getRole() {
        return GolemRole.WARRIOR;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.15D, 5.0F, 2.5F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide() && !isSitting() && getTarget() == null) {
            // Автоматическая защита владельца, если его кто-то ударил
            LivingEntity owner = getOwnerEntity();
            if (owner != null && distanceToSqr(owner) < 256.0D) {
                LivingEntity attacker = owner.getLastHurtByMob();
                if (attacker != null && attacker.isAlive() && !(attacker instanceof BaseGolemEntity)) {
                    setTarget(attacker);
                }
            }
        }
    }
}
