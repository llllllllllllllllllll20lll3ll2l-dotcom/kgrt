package com.manyl.mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

/**
 * Голем-воительница (женский тип воина: более проворная, с крылатой броней и повышенным уроном).
 * Полностью сохраняет логику подбора оружия, еды, инвентаря и команд охраны.
 */
public class ValkyrieGolemEntity extends WarriorGolemEntity {
    public ValkyrieGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public GolemRole getRole() {
        return GolemRole.VALKYRIE;
    }
}
