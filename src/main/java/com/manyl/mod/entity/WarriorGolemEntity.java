package com.manyl.mod.entity;

import com.manyl.mod.data.PlayerGolemSettings;
import com.manyl.mod.entity.ai.FollowOwnerGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

/**
 * Голем-воин (без ограничений по количеству).
 * Автоматически берёт в руку лучшее оружие из своего инвентаря (отображается в 3D и усиливает удар),
 * никогда не атакует своего владельца и союзных стражей.
 */
public class WarriorGolemEntity extends BaseGolemEntity {
    public WarriorGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public GolemRole getRole() {
        return GolemRole.WARRIOR;
    }

    protected boolean isWeaponHidden() {
        return false;
    }

    public boolean isFriendlyEntity(LivingEntity entity) {
        if (entity == null) return false;
        if (entity instanceof Player player && getOwnerUuid() != null && getOwnerUuid().equals(player.getUUID())) {
            return true;
        }
        if (entity instanceof BaseGolemEntity otherGolem) {
            if (getOwnerUuid() != null && getOwnerUuid().equals(otherGolem.getOwnerUuid())) {
                return true;
            }
            if (getOwnerUuid() == null && otherGolem.getOwnerUuid() == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setTarget(LivingEntity target) {
        if (target != null && isFriendlyEntity(target)) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    protected void onInventoryChanged() {
        if (level().isClientSide()) return;
        if (isWeaponHidden()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.setDisplayItem(ItemStack.EMPTY);
            return;
        }
        ItemStack best = ItemStack.EMPTY;
        double bestScore = -1.0D;

        for (int i = 1; i < INVENTORY_SIZE; i++) {
            ItemStack s = inventory.getItem(i);
            if (s.isEmpty() || s.has(DataComponents.FOOD)) continue;

            double score = scoreWeapon(s);
            if (score > bestScore) {
                bestScore = score;
                best = s;
            }
        }

        this.setItemSlot(EquipmentSlot.MAINHAND, best.isEmpty() ? ItemStack.EMPTY : best.copy());
        this.setDisplayItem(best);
    }

    @Override
    protected boolean isProtectedGearSlot(int slot) {
        ItemStack s = inventory.getItem(slot);
        return isToolOrWeaponItem(s) || isFoodItem(s);
    }

    private double scoreWeapon(ItemStack stack) {
        double dmg = 0.0D;
        ItemAttributeModifiers mods = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry entry : mods.modifiers()) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)) {
                dmg += entry.modifier().amount();
            }
        }
        if (stack.is(ItemTags.SWORDS)) dmg += 2.0D;
        else if (stack.is(ItemTags.AXES)) dmg += 1.5D;
        if (dmg > 0.0D) return 10.0D + dmg;
        return 1.0D;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.15D, 5.0F, 2.5F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                LivingEntity lastHurt = WarriorGolemEntity.this.getLastHurtByMob();
                if (lastHurt != null && isFriendlyEntity(lastHurt)) {
                    WarriorGolemEntity.this.setLastHurtByMob(null);
                    return false;
                }
                return super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                (target, level) -> matchesTargetFilter(target)));
    }

    private boolean matchesTargetFilter(LivingEntity target) {
        if (isFriendlyEntity(target)) return false;
        String filter = getOwnerSettings().getTargetFilter();
        if (filter == null || filter.isEmpty()) return true;
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).getPath();
        return path.contains(filter.toLowerCase());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) return;

        // Сброс цели, если по каким-то причинам целью оказался владелец или союзник
        LivingEntity curTarget = getTarget();
        if (curTarget != null && isFriendlyEntity(curTarget)) {
            super.setTarget(null);
        }

        PlayerGolemSettings settings = getOwnerSettings();
        String guardMode = settings.getGuardMode();

        if ("patrol".equals(guardMode) && !isSitting() && getTarget() == null) {
            BlockPos home = getHomePos();
            if (home != null && position().distanceToSqr(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D) > 64.0D) {
                getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, 1.1D);
            }
        }

        if (!isSitting() && getTarget() == null) {
            LivingEntity owner = getOwnerEntity();
            if (owner != null && distanceToSqr(owner) < 256.0D) {
                LivingEntity attacker = owner.getLastHurtByMob();
                if (attacker != null && attacker.isAlive() && !isFriendlyEntity(attacker)) {
                    setTarget(attacker);
                }
            }
        }
    }
}
