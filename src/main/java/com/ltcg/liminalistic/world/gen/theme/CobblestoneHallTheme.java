package com.ltcg.liminalistic.world.gen.theme;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CobblestoneHallTheme implements RoomTheme {
	private static final BlockState FLOOR = Blocks.COBBLESTONE.defaultBlockState();
	private static final BlockState WALL = Blocks.COBBLESTONE.defaultBlockState();
	private static final BlockState CEILING = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState LIGHT = Blocks.SEA_LANTERN.defaultBlockState();
	private static final BlockState STAIR = Blocks.COBBLESTONE_STAIRS.defaultBlockState();

	@Override
	public void generate(WorldGenLevel level, ChunkPos chunkPos, int floorY, int roomHeight) {
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

				level.setBlock(pos.set(worldX, floorY, worldZ), FLOOR, 2);
				level.setBlock(pos.set(worldX, floorY + roomHeight + 1, worldZ), CEILING, 2);

				for (int y = 1; y <= roomHeight; y++) {
					BlockState wallState = AIR;
					if (edgeX && !doorwayZ) {
						wallState = WALL;
					} else if (edgeZ && !doorwayX) {
						wallState = WALL;
					}
					level.setBlock(pos.set(worldX, floorY + y, worldZ), wallState, 2);
				}
			}
		}

		level.setBlock(pos.set(originX + 8, floorY + roomHeight, originZ + 8), LIGHT, 2);

		// Unreachable stairs: ascend against the east interior wall, dead-ending into the ceiling.
		for (int step = 0; step < roomHeight - 1; step++) {
			level.setBlock(pos.set(originX + 13, floorY + 1 + step, originZ + 12 - step), STAIR, 2);
		}
	}
}
