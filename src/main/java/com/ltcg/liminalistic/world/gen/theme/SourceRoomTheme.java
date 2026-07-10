package com.ltcg.liminalistic.world.gen.theme;

import com.ltcg.liminalistic.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SourceRoomTheme implements RoomTheme {
	private static final BlockState FLOOR = Blocks.BLACKSTONE.defaultBlockState();
	private static final BlockState WALL = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
	private static final BlockState CEILING = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState LIGHT = Blocks.SOUL_LANTERN.defaultBlockState();

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
					if ((edgeX && !doorwayZ) || (edgeZ && !doorwayX)) {
						wallState = WALL;
					}
					level.setBlock(pos.set(worldX, floorY + y, worldZ), wallState, 2);
				}
			}
		}

		int centerX = originX + 8;
		int centerZ = originZ + 8;
		level.setBlock(pos.set(centerX, floorY + 1, centerZ), ModBlocks.CORRUPTED_CORE.defaultBlockState(), 2);

		int[][] lanternOffsets = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
		for (int[] offset : lanternOffsets) {
			level.setBlock(pos.set(centerX + offset[0], floorY + 1, centerZ + offset[1]), LIGHT, 2);
		}
	}
}
