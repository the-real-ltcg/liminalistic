package com.ltcg.liminalistic;

import com.ltcg.liminalistic.registry.ModBlocks;
import com.ltcg.liminalistic.registry.ModDimensions;
import com.ltcg.liminalistic.registry.ModEffects;
import com.ltcg.liminalistic.registry.ModItemGroup;
import com.ltcg.liminalistic.registry.ModItems;
import com.ltcg.liminalistic.world.DoorManager;
import com.ltcg.liminalistic.world.PresenceManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiminalisticMod implements ModInitializer {
	public static final String MOD_ID = "liminalistic";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Liminalistic loading...");
		ModBlocks.init();
		ModItems.init();
		ModEffects.init();
		ModItemGroup.init();
		ModDimensions.init();
		DoorManager.init();
		PresenceManager.init();
	}
}
