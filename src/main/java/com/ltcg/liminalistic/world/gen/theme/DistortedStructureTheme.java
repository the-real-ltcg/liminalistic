package com.ltcg.liminalistic.world.gen.theme;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DistortedStructureTheme implements RoomTheme {
	private static final BlockState[] FLOOR_VARIANTS = {
			Blocks.COBBLESTONE.defaultBlockState(),
			Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
			Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
			Blocks.STONE_BRICKS.defaultBlockState()
	};
	private static final BlockState[] WALL_VARIANTS = {
			Blocks.STONE_BRICKS.defaultBlockState(),
			Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
			Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
	};
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState LIGHT = Blocks.GLOWSTONE.defaultBlockState();

	@Override
	public void generate(WorldGenLevel level, ChunkPos chunkPos, int floorY, int roomHeight) {
		RandomSource random = RandomSource.create(chunkPos.pack());
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int originX = chunkPos.getMinBlockX();
		int originZ = chunkPos.getMinBlockZ();

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int worldX = originX + x;
				int worldZ = originZ + z;
				boolean edgeX = x == 0 || x == 15;
				boolean edgeZ = z == 0 || z == 15;
				boolean doorwayX = x >= 6 && x <= 9;
				boolean doorwayZ = z >= 6 && z <= 9;

				level.setBlock(pos.set(worldX, floorY, worldZ), randomOf(FLOOR_VARIANTS, random), 2);
				level.setBlock(pos.set(worldX, floorY + roomHeight + 1, worldZ), randomOf(WALL_VARIANTS, random), 2);

				for (int y = 1; y <= roomHeight; y++) {
					BlockState wallState = AIR;
					boolean glitchGap = random.nextInt(24) == 0;
					if ((edgeX && !doorwayZ) || (edgeZ && !doorwayX)) {
						wallState = glitchGap ? AIR : randomOf(WALL_VARIANTS, random);
					}
					level.setBlock(pos.set(worldX, floorY + y, worldZ), wallState, 2);
				}
			}
		}

		int lightX = originX + 4 + random.nextInt(8);
		int lightZ = originZ + 4 + random.nextInt(8);
		level.setBlock(pos.set(lightX, floorY + roomHeight, lightZ), LIGHT, 2);
	}

	private static BlockState randomOf(BlockState[] states, RandomSource random) {
		return states[random.nextInt(states.length)];
	}
}
