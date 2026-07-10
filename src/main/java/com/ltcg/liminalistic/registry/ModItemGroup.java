package com.ltcg.liminalistic.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public final class ModItemGroup {
	private static final ResourceKey<CreativeModeTab> INGREDIENTS =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("ingredients"));

	private ModItemGroup() {
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(INGREDIENTS).register(output -> {
			output.accept(ModItems.SIGIL_FRAGMENT);
			output.accept(ModItems.CORRUPTED_CORE);
		});
	}
}
