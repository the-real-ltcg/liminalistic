package com.ltcg.liminalistic.command;

import com.ltcg.liminalistic.world.CorruptionState;
import com.ltcg.liminalistic.world.DoorState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.Level;

public final class LiminalisticCommand {
	private static final PermissionCheck PERMISSION = new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);
	private static final int DOOR_SUMMON_DISTANCE = 2;

	private LiminalisticCommand() {
	}

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
	}

	private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("liminalistic")
				.requires(Commands.hasPermission(PERMISSION))
				.then(Commands.literal("corruption")
						.then(Commands.literal("set")
								.then(Commands.argument("stage", IntegerArgumentType.integer(0, CorruptionState.MAX_STAGE))
										.executes(ctx -> setCorruption(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "stage"))))))
				.then(Commands.literal("door")
						.then(Commands.literal("summon")
								.executes(ctx -> summonDoor(ctx.getSource())))));
	}

	private static int setCorruption(CommandSourceStack source, int stage) {
		ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
		if (overworld == null) {
			source.sendFailure(Component.literal("The Overworld is not loaded."));
			return 0;
		}

		CorruptionState.get(overworld).debugSetStage(stage);
		source.sendSuccess(() -> Component.literal("Corruption stage set to " + stage + "."), true);
		return 1;
	}

	private static int summonDoor(CommandSourceStack source) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		if (player.level().dimension() != Level.OVERWORLD) {
			source.sendFailure(Component.literal("You must be in the Overworld to summon a door."));
			return 0;
		}

		ServerLevel overworld = (ServerLevel) player.level();
		BlockPos doorPos = player.blockPosition().relative(player.getDirection(), DOOR_SUMMON_DISTANCE);
		overworld.getDataStorage().computeIfAbsent(DoorState.TYPE).addDoor(doorPos);

		source.sendSuccess(() -> Component.literal("A door has opened " + DOOR_SUMMON_DISTANCE + " blocks ahead of you."), true);
		return 1;
	}
}
