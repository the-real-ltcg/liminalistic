package com.ltcg.liminalistic.block;

import com.ltcg.liminalistic.registry.ModItems;
import com.ltcg.liminalistic.world.CorruptionState;
import com.ltcg.liminalistic.world.DoorManager;
import com.ltcg.liminalistic.world.DoorState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Set;

public class CorruptedCoreBlock extends Block {
	private static final int REQUIRED_FRAGMENTS = 3;

	public CorruptedCoreBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!(level instanceof ServerLevel corridor) || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}
		if (stack.getItem() != ModItems.SIGIL_FRAGMENT) {
			return InteractionResult.PASS;
		}

		ServerLevel overworld = corridor.getServer().getLevel(Level.OVERWORLD);
		if (overworld == null) {
			return InteractionResult.PASS;
		}

		CorruptionState corruption = CorruptionState.get(overworld);
		if (!corruption.isRitualUnlocked()) {
			serverPlayer.sendSystemMessage(Component.literal("The core does not stir. Not yet."));
			return InteractionResult.FAIL;
		}

		if (stack.getCount() < REQUIRED_FRAGMENTS) {
			serverPlayer.sendSystemMessage(Component.literal("The core needs " + REQUIRED_FRAGMENTS + " fragments."));
			return InteractionResult.FAIL;
		}

		stack.shrink(REQUIRED_FRAGMENTS);
		completeRitual(overworld, corridor, pos, serverPlayer);
		return InteractionResult.SUCCESS;
	}

	private static void completeRitual(ServerLevel overworld, ServerLevel corridor, BlockPos corePos, ServerPlayer player) {
		CorruptionState.get(overworld).reset();
		overworld.getDataStorage().computeIfAbsent(DoorState.TYPE).sealAll();

		BlockPos returnPoint = DoorManager.getReturnPoint(player.getUUID());
		if (returnPoint == null) {
			returnPoint = BlockPos.ZERO;
		}

		corridor.sendParticles(ParticleTypes.REVERSE_PORTAL, corePos.getX() + 0.5, corePos.getY() + 1.0, corePos.getZ() + 0.5, 120, 1.5, 1.5, 1.5, 0.1);

		player.teleportTo(overworld, returnPoint.getX() + 0.5, returnPoint.getY(), returnPoint.getZ() + 0.5,
				Set.of(Relative.X, Relative.Y, Relative.Z), player.getYRot(), player.getXRot(), false);

		player.sendSystemMessage(Component.literal("The corridor collapses behind you."));
		overworld.playSound(null, returnPoint, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.5f);
	}
}
