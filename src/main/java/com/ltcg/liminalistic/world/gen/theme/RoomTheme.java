package com.ltcg.liminalistic.world.gen.theme;

import com.ltcg.liminalistic.world.CorruptionState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;

public interface RoomTheme {
	void generate(WorldGenLevel level, ChunkPos chunkPos, int floorY, int roomHeight);

	static RoomTheme forCell(int cellX, int cellZ, int corruptionStage) {
		if (cellX == 0 && cellZ == 0 && corruptionStage >= CorruptionState.MAX_STAGE) {
			return new SourceRoomTheme();
		}

		long hash = mix(cellX, cellZ);
		int distortedChance = Math.max(0, 8 - corruptionStage * 2);
		if (corruptionStage >= 2 && distortedChance > 0 && Math.floorMod(hash, (long) distortedChance) == 0) {
			return new DistortedStructureTheme();
		}
		return (hash & 1) == 0 ? new CobblestoneHallTheme() : new BirchForestTheme();
	}

	private static long mix(int x, int z) {
		long h = (long) x * 0x9E3779B97F4A7C15L ^ (long) z * 0xC2B2AE3D27D4EB4FL;
		h ^= (h >>> 33);
		h *= 0xFF51AFD7ED558CCDL;
		h ^= (h >>> 33);
		return h;
	}
}
