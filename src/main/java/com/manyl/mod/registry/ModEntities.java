package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.entity.WarriorGolemEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Регистрация сущностей големов (Воины).
 */
public final class ModEntities {
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
        return WARRIOR_GOLEM;
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(WARRIOR_GOLEM, BaseGolemEntity.createGolemAttributes(GolemRole.WARRIOR));
    }
}
