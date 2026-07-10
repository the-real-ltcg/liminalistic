package com.ltcg.liminalistic.registry;

import com.ltcg.liminalistic.LiminalisticMod;
import com.ltcg.liminalistic.block.CorruptedCoreBlock;
import com.ltcg.liminalistic.block.CorruptionFlowerBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

public final class ModBlocks {
	public static final Block CORRUPTED_CORE = register("corrupted_core",
			CorruptedCoreBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_PURPLE)
					.strength(50.0f, 1200.0f)
					.noLootTable());

	public static final Block CORRUPTION_FLOWER = register("corruption_flower",
			CorruptionFlowerBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLACK)
					.noCollision()
					.instabreak()
					.sound(SoundType.GRASS)
					.offsetType(BlockBehaviour.OffsetType.XZ));

	private ModBlocks() {
	}

	public static void init() {
	}

	private static Block register(String path, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(LiminalisticMod.MOD_ID, path));
		Block block = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}
}
