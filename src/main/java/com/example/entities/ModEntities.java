package com.example.entities;

import com.example.ExampleMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final EntityType<BattleAllayEntity> BATTLE_ALLAY = register(
            "battle_allay",
            EntityType.Builder.<BattleAllayEntity>of(BattleAllayEntity::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

//    public static void registerModEntityTypes() {
//        ExampleMod.LOGGER.info("Registering EntityTypes for " + ExampleMod.MOD_ID);
//    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(BATTLE_ALLAY, BattleAllayEntity.createAttributes());
    }
}
