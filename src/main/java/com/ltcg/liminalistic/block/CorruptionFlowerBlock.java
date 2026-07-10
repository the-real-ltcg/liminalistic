package com.ltcg.liminalistic.block;

import com.ltcg.liminalistic.world.CorruptionState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CorruptionFlowerBlock extends BushBlock {
	private static final int BASE_PARTICLES = 1;
	private static final int PARTICLES_PER_STAGE = 2;

	public CorruptionFlowerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		int stage = CorruptionState.currentStage();
		int count = BASE_PARTICLES + stage * PARTICLES_PER_STAGE;

		for (int i = 0; i < count; i++) {
			double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
			double y = pos.getY() + 0.4 + random.nextDouble() * 0.6;
			double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
			level.addParticle(ParticleTypes.ASH, x, y, z, 0.0, 0.01 + random.nextDouble() * 0.02, 0.0);
		}
	}
}
