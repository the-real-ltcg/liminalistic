package com.ltcg.liminalistic.world.gen.theme;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BirchForestTheme implements RoomTheme {
	private static final BlockState FLOOR = Blocks.GRASS_BLOCK.defaultBlockState();
	private static final BlockState BOUNDARY = Blocks.BIRCH_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState LOG = Blocks.BIRCH_LOG.defaultBlockState();
	private static final int CANOPY_HEIGHT = 3;

	private static final int[][] TREE_OFFSETS = {{4, 4}, {11, 11}, {4, 11}, {11, 4}};

	@Override
	public void generate(WorldGenLevel level, ChunkPos chunkPos, int floorY, int roomHeight) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int originX = chunkPos.getMinBlockX();
		int originZ = chunkPos.getMinBlockZ();
		int ceilingY = floorY + roomHeight + CANOPY_HEIGHT;

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int worldX = originX + x;
				int worldZ = originZ + z;
				boolean edgeX = x == 0 || x == 15;
				boolean edgeZ = z == 0 || z == 15;
				boolean doorwayX = x >= 6 && x <= 9;
				boolean doorwayZ = z >= 6 && z <= 9;

				level.setBlock(pos.set(worldX, floorY, worldZ), FLOOR, 2);
				level.setBlock(pos.set(worldX, ceilingY, worldZ), BOUNDARY, 2);

				for (int y = 1; y < ceilingY - floorY; y++) {
					BlockState wallState = AIR;
					if (edgeX && !doorwayZ) {
						wallState = BOUNDARY;
					} else if (edgeZ && !doorwayX) {
						wallState = BOUNDARY;
					}
					level.setBlock(pos.set(worldX, floorY + y, worldZ), wallState, 2);
				}
			}
		}

		for (int[] offset : TREE_OFFSETS) {
			int trunkX = originX + offset[0];
			int trunkZ = originZ + offset[1];
			for (int y = 1; y <= roomHeight; y++) {
				level.setBlock(pos.set(trunkX, floorY + y, trunkZ), LOG, 2);
			}
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					level.setBlock(pos.set(trunkX + dx, floorY + roomHeight + 1, trunkZ + dz), BOUNDARY, 2);
				}
			}
		}
	}
}
