package com.manyl.mod.registry;

import com.manyl.mod.ManylMod;
import com.manyl.mod.entity.BaseGolemEntity;
import com.manyl.mod.entity.GolemRole;
import com.manyl.mod.entity.ValkyrieGolemEntity;
import com.manyl.mod.entity.WarriorGolemEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Регистрация сущностей големов (Воин и Воительница).
 */
public final class ModEntities {
    public static final EntityType<WarriorGolemEntity> WARRIOR_GOLEM = register(
            GolemRole.WARRIOR.getId(), WarriorGolemEntity::new, 0.85F, 2.02F);

    public static final EntityType<ValkyrieGolemEntity> VALKYRIE_GOLEM = register(
            GolemRole.VALKYRIE.getId(), ValkyrieGolemEntity::new, 0.78F, 2.02F);

    private static <T extends BaseGolemEntity> EntityType<T> register(
            String name, EntityType.EntityFactory<T> factory, float width, float height) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ManylMod.id(name));
        EntityType<T> type = EntityType.Builder.of(factory, MobCategory.CREATURE)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(key);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
    }

    public static EntityType<? extends BaseGolemEntity> forRole(GolemRole role) {
        return switch (role) {
            case WARRIOR -> WARRIOR_GOLEM;
            case VALKYRIE -> VALKYRIE_GOLEM;
        };
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(WARRIOR_GOLEM, BaseGolemEntity.createGolemAttributes(GolemRole.WARRIOR));
        FabricDefaultAttributeRegistry.register(VALKYRIE_GOLEM, BaseGolemEntity.createGolemAttributes(GolemRole.VALKYRIE));
    }
}
