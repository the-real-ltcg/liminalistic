package com.ltcg.liminalistic.world;

import com.ltcg.liminalistic.lore.LoreBooks;
import com.ltcg.liminalistic.registry.ModDimensions;
import com.ltcg.liminalistic.registry.ModEffects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PresenceManager {
	private static final int DREAD_TICK_INTERVAL = 20;
	private static final int MAX_DREAD = 200;
	private static final int EFFECT_DURATION_TICKS = 140;
	private static final int GLIMPSE_LIFETIME_TICKS = 100;
	private static final double GLIMPSE_MIN_DISTANCE = 12.0;
	private static final double GLIMPSE_MAX_DISTANCE = 20.0;
	private static final int DARKNESS_PULSE_THRESHOLD = 150;
	private static final int DARKNESS_PULSE_CHANCE = 90;
	private static final int DARKNESS_PULSE_DURATION_TICKS = 60;

	private static final int LORE_BOOK_CHANCE = 300;

	private static final Map<UUID, Integer> dread = new HashMap<>();
	private static final Map<UUID, UUID> activeGlimpses = new HashMap<>();
	private static final Map<UUID, Long> glimpseExpiry = new HashMap<>();
	private static final Set<Long> visitedCells = new HashSet<>();
	private static final Map<UUID, Set<String>> foundLoreIds = new HashMap<>();

	private PresenceManager() {
	}

	public static void init() {
		ServerTickEvents.END_LEVEL_TICK.register(PresenceManager::onLevelTick);
	}

	private static void onLevelTick(ServerLevel level) {
		if (level.dimension() != ModDimensions.CORRIDOR_LEVEL) {
			return;
		}

		Set<UUID> present = new HashSet<>();
		for (ServerPlayer player : level.players()) {
			present.add(player.getUUID());
		}
		dread.keySet().removeIf(uuid -> !present.contains(uuid));

		if (level.getGameTime() % DREAD_TICK_INTERVAL == 0) {
			ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
			CorruptionState corruption = overworld != null ? CorruptionState.get(overworld) : null;

			if (corruption != null && !level.players().isEmpty()) {
				corruption.onCorridorTick(DREAD_TICK_INTERVAL);
			}

			for (ServerPlayer player : level.players()) {
				trackRoomVisit(player, corruption);
				tickDread(level, player, corruption);
			}
		}

		expireGlimpses(level);
	}

	private static void trackRoomVisit(ServerPlayer player, CorruptionState corruption) {
		long cellKey = player.chunkPosition().pack();
		if (visitedCells.add(cellKey) && corruption != null) {
			corruption.onRoomVisited();
		}
	}

	private static void tickDread(ServerLevel level, ServerPlayer player, CorruptionState corruption) {
		int current = dread.merge(player.getUUID(), 1, Integer::sum);
		if (current > MAX_DREAD) {
			current = MAX_DREAD;
			dread.put(player.getUUID(), current);
		}

		int amplifier = Math.min(4, current / 40);
		player.addEffect(new MobEffectInstance(ModEffects.UNEASE, EFFECT_DURATION_TICKS, amplifier, true, false));

		RandomSource random = level.getRandom();
		int soundChance = Math.max(20, 200 - current);
		if (random.nextInt(soundChance) == 0) {
			playDreadSound(level, player, random);
		}

		int glimpseChance = Math.max(150, 600 - current * 2);
		if (!activeGlimpses.containsKey(player.getUUID()) && random.nextInt(glimpseChance) == 0) {
			spawnGlimpse(level, player, random);
		}

		if (current > DARKNESS_PULSE_THRESHOLD && random.nextInt(DARKNESS_PULSE_CHANCE) == 0) {
			player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS, DARKNESS_PULSE_DURATION_TICKS, 0, true, false));
		}

		if (random.nextInt(LORE_BOOK_CHANCE) == 0) {
			maybeGrantLoreBook(player, random, corruption);
		}
	}

	private static void maybeGrantLoreBook(ServerPlayer player, RandomSource random, CorruptionState corruption) {
		Set<String> found = foundLoreIds.computeIfAbsent(player.getUUID(), uuid -> new HashSet<>());
		List<LoreBooks.Entry> unfound = LoreBooks.ENTRIES.stream()
				.filter(entry -> !found.contains(entry.id()))
				.toList();
		if (unfound.isEmpty()) {
			return;
		}

		LoreBooks.Entry entry = unfound.get(random.nextInt(unfound.size()));
		if (player.getInventory().add(LoreBooks.createBookStack(entry))) {
			found.add(entry.id());
			if (corruption != null) {
				corruption.onLoreBookFound();
			}
		}
	}

	private static void playDreadSound(ServerLevel level, ServerPlayer player, RandomSource random) {
		boolean heartbeat = random.nextBoolean();
		level.playSound(null, player.blockPosition(),
				heartbeat ? SoundEvents.WARDEN_HEARTBEAT : SoundEvents.WARDEN_NEARBY_CLOSE,
				SoundSource.AMBIENT, 0.6f, 0.7f + random.nextFloat() * 0.2f);
	}

	private static void spawnGlimpse(ServerLevel level, ServerPlayer player, RandomSource random) {
		double angle = Math.toRadians(player.getYRot()) + (random.nextDouble() - 0.5) * Math.PI * 0.6;
		double distance = GLIMPSE_MIN_DISTANCE + random.nextDouble() * (GLIMPSE_MAX_DISTANCE - GLIMPSE_MIN_DISTANCE);
		double x = player.getX() - Math.sin(angle) * distance;
		double z = player.getZ() + Math.cos(angle) * distance;

		EnderMan figure = EntityTypes.ENDERMAN.create(level, EntitySpawnReason.EVENT);
		if (figure == null) {
			return;
		}

		figure.setPos(x, player.getY(), z);
		figure.setNoAi(true);
		figure.setSilent(true);
		figure.setPersistenceRequired();

		if (!level.addFreshEntity(figure)) {
			return;
		}

		activeGlimpses.put(player.getUUID(), figure.getUUID());
		glimpseExpiry.put(figure.getUUID(), level.getGameTime() + GLIMPSE_LIFETIME_TICKS);
	}

	private static void expireGlimpses(ServerLevel level) {
		if (glimpseExpiry.isEmpty()) {
			return;
		}

		long now = level.getGameTime();
		glimpseExpiry.entrySet().removeIf(entry -> {
			if (entry.getValue() > now) {
				return false;
			}
			Entity entity = level.getEntityInAnyDimension(entry.getKey());
			if (entity != null) {
				entity.discard();
			}
			activeGlimpses.values().remove(entry.getKey());
			return true;
		});
	}
}
