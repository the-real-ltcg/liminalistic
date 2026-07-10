package com.ltcg.liminalistic.world;

import com.ltcg.liminalistic.LiminalisticMod;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

public class DoorState extends SavedData {
	public static final SavedDataType<DoorState> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(LiminalisticMod.MOD_ID, "doors"),
			DoorState::new,
			RecordCodecBuilder.create(instance -> instance.group(
					BlockPos.CODEC.listOf().fieldOf("doors").forGetter(state -> state.doors)
			).apply(instance, DoorState::new)),
			DataFixTypes.LEVEL);

	private final List<BlockPos> doors;

	public DoorState() {
		this(new ArrayList<>());
	}

	private DoorState(List<BlockPos> doors) {
		this.doors = new ArrayList<>(doors);
	}

	public List<BlockPos> getDoors() {
		return doors;
	}

	public void addDoor(BlockPos pos) {
		doors.add(pos.immutable());
		setDirty();
	}

	public void removeDoor(BlockPos pos) {
		doors.remove(pos);
		setDirty();
	}

	public void sealAll() {
		doors.clear();
		setDirty();
	}
}
