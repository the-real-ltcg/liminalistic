package com.ltcg.liminalistic.registry;

import com.ltcg.liminalistic.LiminalisticMod;
import com.ltcg.liminalistic.effect.UneaseMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class ModEffects {
	public static final Holder<MobEffect> UNEASE = register("unease",
			new UneaseMobEffect(MobEffectCategory.HARMFUL, 0x2b1b3d));

	private ModEffects() {
	}

	public static void init() {
	}

	private static Holder<MobEffect> register(String path, MobEffect effect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
				Identifier.fromNamespaceAndPath(LiminalisticMod.MOD_ID, path), effect);
	}
}
