package com.ltcg.liminalistic.world;

import com.ltcg.liminalistic.registry.ModDimensions;
import com.ltcg.liminalistic.world.gen.CorridorChunkGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DoorManager {
	private static final int MAX_ACTIVE_DOORS = 3;
	private static final int SPAWN_INTERVAL_TICKS = 200;
	private static final int SPAWN_CHANCE_DENOMINATOR = 4;
	private static final double MIN_SPAWN_DISTANCE = 10.0;
	private static final double MAX_SPAWN_DISTANCE = 24.0;
	private static final double TRIGGER_DISTANCE_SQR = 1.0;

	private static final Map<UUID, BlockPos> returnPoints = new HashMap<>();

	private DoorManager() {
	}

	public static BlockPos getReturnPoint(UUID playerId) {
		return returnPoints.get(playerId);
	}

	public static void init() {
		ServerTickEvents.END_LEVEL_TICK.register(DoorManager::onLevelTick);
	}

	private static void onLevelTick(ServerLevel level) {
		if (level.dimension() != Level.OVERWORLD) {
			return;
		}

		DoorState state = level.getDataStorage().computeIfAbsent(DoorState.TYPE);

		if (level.getGameTime() % SPAWN_INTERVAL_TICKS == 0) {
			trySpawnDoor(level, state);
		}

		checkTriggers(level, state);
	}

	private static void trySpawnDoor(ServerLevel level, DoorState state) {
		if (state.getDoors().size() >= MAX_ACTIVE_DOORS) {
			return;
		}

		List<ServerPlayer> players = level.players();
		if (players.isEmpty()) {
			return;
		}

		RandomSource random = level.getRandom();
		if (random.nextInt(SPAWN_CHANCE_DENOMINATOR) != 0) {
			return;
		}

		ServerPlayer player = players.get(random.nextInt(players.size()));
		double angle = random.nextDouble() * Math.PI * 2.0;
		double distance = MIN_SPAWN_DISTANCE + random.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
		int offsetX = (int) Math.round(Math.cos(angle) * distance);
		int offsetZ = (int) Math.round(Math.sin(angle) * distance);

		BlockPos doorPos = player.blockPosition().offset(offsetX, 0, offsetZ);
		state.addDoor(doorPos);
	}

	private static void checkTriggers(ServerLevel level, DoorState state) {
		List<BlockPos> doors = state.getDoors();
		if (doors.isEmpty()) {
			return;
		}

		for (BlockPos doorPos : List.copyOf(doors)) {
			for (ServerPlayer player : level.players()) {
				if (player.distanceToSqr(doorPos.getX() + 0.5, doorPos.getY() + 0.5, doorPos.getZ() + 0.5) <= TRIGGER_DISTANCE_SQR) {
					triggerDoor(level, state, doorPos, player);
					break;
				}
			}
		}
	}

	private static void triggerDoor(ServerLevel overworld, DoorState state, BlockPos doorPos, ServerPlayer player) {
		state.removeDoor(doorPos);
		CorruptionState.get(overworld).onDoorTriggered();
		returnPoints.put(player.getUUID(), player.blockPosition());

		overworld.sendParticles(ParticleTypes.REVERSE_PORTAL, doorPos.getX() + 0.5, doorPos.getY() + 0.5, doorPos.getZ() + 0.5, 40, 0.5, 0.5, 0.5, 0.05);
		overworld.playSound(null, doorPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.6f);

		ServerLevel corridor = overworld.getServer().getLevel(ModDimensions.CORRIDOR_LEVEL);
		if (corridor == null) {
			return;
		}

		BlockPos destination = cellEntryPoint(doorPos);
		player.teleportTo(corridor, destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5,
				Set.of(), player.getYRot(), player.getXRot(), false);
		player.resetFallDistance();
		player.setDeltaMovement(Vec3.ZERO);

		corridor.sendParticles(ParticleTypes.PORTAL, destination.getX() + 0.5, destination.getY() + 1.0, destination.getZ() + 0.5, 60, 0.5, 1.0, 0.5, 0.1);
		corridor.playSound(null, destination, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1.0f, 0.8f);
	}

	private static BlockPos cellEntryPoint(BlockPos doorPos) {
		long hash = mix(doorPos.asLong());
		int cellX = (int) (hash & 0xFFFF) - 32768;
		int cellZ = (int) ((hash >>> 16) & 0xFFFF) - 32768;
		int worldX = cellX * 16 + 8;
		int worldZ = cellZ * 16 + 8;
		return new BlockPos(worldX, CorridorChunkGenerator.FLOOR_Y + 1, worldZ);
	}

	private static long mix(long h) {
		h ^= (h >>> 33);
		h *= 0xFF51AFD7ED558CCDL;
		h ^= (h >>> 33);
		h *= 0xC4CEB9FE1A85EC53L;
		h ^= (h >>> 33);
		return h;
	}
}
