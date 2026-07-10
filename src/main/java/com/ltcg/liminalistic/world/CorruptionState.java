package com.ltcg.liminalistic.world;

import com.ltcg.liminalistic.LiminalisticMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class CorruptionState extends SavedData {
	public static final int MAX_STAGE = 5;

	public static final SavedDataType<CorruptionState> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(LiminalisticMod.MOD_ID, "corruption"),
			CorruptionState::new,
			RecordCodecBuilder.create(instance -> instance.group(
					Codec.INT.fieldOf("doors_triggered").forGetter(state -> state.doorsTriggered),
					Codec.INT.fieldOf("rooms_visited").forGetter(state -> state.roomsVisited),
					Codec.LONG.fieldOf("time_in_corridor_ticks").forGetter(state -> state.timeInCorridorTicks),
					Codec.INT.fieldOf("lore_books_found").forGetter(state -> state.loreBooksFound)
			).apply(instance, CorruptionState::new)),
			DataFixTypes.LEVEL);

	private static volatile int cachedStage = 0;

	private int doorsTriggered;
	private int roomsVisited;
	private long timeInCorridorTicks;
	private int loreBooksFound;

	public CorruptionState() {
		this(0, 0, 0L, 0);
	}

	private CorruptionState(int doorsTriggered, int roomsVisited, long timeInCorridorTicks, int loreBooksFound) {
		this.doorsTriggered = doorsTriggered;
		this.roomsVisited = roomsVisited;
		this.timeInCorridorTicks = timeInCorridorTicks;
		this.loreBooksFound = loreBooksFound;
		cachedStage = computeStage();
	}

	public static CorruptionState get(ServerLevel overworld) {
		CorruptionState state = overworld.getDataStorage().computeIfAbsent(TYPE);
		cachedStage = state.computeStage();
		return state;
	}

	public static int currentStage() {
		return cachedStage;
	}

	public int stage() {
		return computeStage();
	}

	public void onDoorTriggered() {
		doorsTriggered++;
		refresh();
	}

	public void onRoomVisited() {
		roomsVisited++;
		refresh();
	}

	public void onCorridorTick(int playersPresent) {
		if (playersPresent <= 0) {
			return;
		}
		timeInCorridorTicks += playersPresent;
		refresh();
	}

	public void onLoreBookFound() {
		loreBooksFound++;
		refresh();
	}

	public boolean isRitualUnlocked() {
		return computeStage() >= MAX_STAGE;
	}

	public void reset() {
		doorsTriggered = 0;
		roomsVisited = 0;
		timeInCorridorTicks = 0L;
		loreBooksFound = 0;
		refresh();
	}

	public void debugSetStage(int stage) {
		int clamped = Math.max(0, Math.min(MAX_STAGE, stage));
		doorsTriggered = clamped * 2;
		roomsVisited = 0;
		timeInCorridorTicks = 0L;
		loreBooksFound = 0;
		refresh();
	}

	private void refresh() {
		cachedStage = computeStage();
		setDirty();
	}

	private int computeStage() {
		int score = (doorsTriggered / 2) + (roomsVisited / 10) + (int) (timeInCorridorTicks / 12000L) + loreBooksFound;
		return Math.min(MAX_STAGE, score);
	}
}
