package com.ltcg.liminalistic.registry;

import com.ltcg.liminalistic.LiminalisticMod;
import com.ltcg.liminalistic.world.gen.CorridorChunkGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ModDimensions {
	public static final ResourceKey<Level> CORRIDOR_LEVEL = ResourceKey.create(
			Registries.DIMENSION, Identifier.fromNamespaceAndPath(LiminalisticMod.MOD_ID, "infinite_corridor"));

	public static final MapCodec<CorridorChunkGenerator> CORRIDOR_CHUNK_GENERATOR = Registry.register(
			BuiltInRegistries.CHUNK_GENERATOR,
			Identifier.fromNamespaceAndPath(LiminalisticMod.MOD_ID, "corridor"),
			CorridorChunkGenerator.CODEC);

	private ModDimensions() {
	}

	public static void init() {
	}
}
