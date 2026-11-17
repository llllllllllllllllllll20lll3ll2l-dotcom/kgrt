package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Регистрация всех 4 типов големов-прислужников (размер хитбокса ~0.6 x 0.85 блока).
 */
public final class ModEntities {
    public static final EntityType<DiggerGolemEntity> DIGGER_GOLEM = register(
            GolemRole.DIGGER.getId(), DiggerGolemEntity::new);
    public static final EntityType<FarmerGolemEntity> FARMER_GOLEM = register(
            GolemRole.FARMER.getId(), FarmerGolemEntity::new);
    public static final EntityType<SorterGolemEntity> SORTER_GOLEM = register(
            GolemRole.SORTER.getId(), SorterGolemEntity::new);
    public static final EntityType<WarriorGolemEntity> WARRIOR_GOLEM = register(
            GolemRole.WARRIOR.getId(), WarriorGolemEntity::new);

    private static <T extends BaseGolemEntity> EntityType<T> register(String name, EntityType.EntityFactory<T> factory) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ManylMod.id(name));
        EntityType<T> type = EntityType.Builder.of(factory, MobCategory.CREATURE)
                .sized(0.6F, 0.85F)
                .clientTrackingRange(8)
                .build(key);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
    }

    public static EntityType<? extends BaseGolemEntity> forRole(GolemRole role) {
        return switch (role) {
            case DIGGER -> DIGGER_GOLEM;
            case FARMER -> FARMER_GOLEM;
            case SORTER -> SORTER_GOLEM;
            case WARRIOR -> WARRIOR_GOLEM;
        };
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(DIGGER_GOLEM, BaseGolemEntity.createGolemAttributes(GolemRole.DIGGER));
        FabricDefaultAttributeRegistry.register(FARMER_GOLEM, BaseGolemEntity.createGolemAttributes(GolemRole.FARMER));
        FabricDefaultAttributeRegistry.register(SORTER_GOLEM, BaseGolemEntity.createGolemAttributes(GolemRole.SORTER));
        FabricDefaultAttributeRegistry.register(WARRIOR_GOLEM, BaseGolemEntity.createGolemAttributes(GolemRole.WARRIOR));
    }
}
