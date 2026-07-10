package com.ltcg.liminalistic.registry;

import com.ltcg.liminalistic.LiminalisticMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public final class ModItems {
	public static final Item SIGIL_FRAGMENT = register("sigil_fragment", Item::new, new Item.Properties());
	public static final Item CORRUPTED_CORE = register("corrupted_core",
			properties -> new BlockItem(ModBlocks.CORRUPTED_CORE, properties), new Item.Properties());

	private ModItems() {
	}

	public static void init() {
	}

	private static Item register(String path, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(LiminalisticMod.MOD_ID, path));
		Item item = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}
}
