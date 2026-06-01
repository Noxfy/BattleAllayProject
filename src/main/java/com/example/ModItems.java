package com.example;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {

    public static final Item HORN = register("horn", HornItem::new, new Item.Properties().stacksTo(1));
    public static final Item GEM = register("gem", Item::new, new Item.Properties().stacksTo(1));
    public static final Item STAFF = register("staff", StaffItem::new, new Item.Properties().stacksTo(1));
    public static final Item PEARL = register("pearl", Item::new, new Item.Properties().stacksTo(1));

    // Item Helper
    public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, name)
        );

        // Create the item instance.
        T item = itemFactory.apply(settings.setId(itemKey));

        // Register the item.
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }

    public static void registerModItems() {

    }
}