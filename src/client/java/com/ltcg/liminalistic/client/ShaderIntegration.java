package com.ltcg.liminalistic.client;

import com.ltcg.liminalistic.registry.ModDimensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.config.IrisConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Switches the active Iris shaderpack to the bundled corridor shader while inside the Infinite
 * Corridor, and restores whatever the player had active beforehand on the way out. Built against
 * Iris's public {@code api.v0} package where possible; selecting a shaderpack by name is not part
 * of that stable API, so this also calls {@link IrisConfig#setShaderPackName}, which mirrors
 * exactly what Iris's own shaderpack-selection screen does internally.
 */
public final class ShaderIntegration {
	private static final Logger LOGGER = LoggerFactory.getLogger("liminalistic-shaders");
	private static final String BUNDLED_PACK_RESOURCE = "/liminalistic_bundled/hyshaders_horror_lite.zip";
	private static final String BUNDLED_OPTIONS_RESOURCE = "/liminalistic_bundled/hyshaders_horror_lite.zip.txt";
	private static final String PACK_NAME = "hyshaders_horror_lite.zip";

	private static ResourceKey<Level> previousDimension;
	private static String savedPackName;
	private static boolean savedShadersEnabled;
	private static boolean stateSaved = false;

	private ShaderIntegration() {
	}

	public static void init() {
		extractBundledShaderpack();
		ClientTickEvents.END_CLIENT_TICK.register(ShaderIntegration::onClientTick);
	}

	private static void extractBundledShaderpack() {
		try {
			Path shaderpacksDir = Iris.getShaderpacksDirectory();
			Files.createDirectories(shaderpacksDir);

			Path target = shaderpacksDir.resolve(PACK_NAME);
			if (!Files.exists(target)) {
				copyResource(BUNDLED_PACK_RESOURCE, target);
			}

			Path targetOptions = shaderpacksDir.resolve(PACK_NAME + ".txt");
			if (!Files.exists(targetOptions)) {
				copyResource(BUNDLED_OPTIONS_RESOURCE, targetOptions);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to extract the bundled corridor shaderpack", e);
		}
	}

	private static void copyResource(String resourcePath, Path target) throws IOException {
		try (InputStream in = ShaderIntegration.class.getResourceAsStream(resourcePath)) {
			if (in == null) {
				LOGGER.error("Bundled shaderpack resource missing: {}", resourcePath);
				return;
			}
			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void onClientTick(Minecraft client) {
		if (client.level == null) {
			return;
		}

		ResourceKey<Level> current = client.level.dimension();
		if (current == previousDimension) {
			return;
		}

		ResourceKey<Level> previous = previousDimension;
		previousDimension = current;

		if (current == ModDimensions.CORRIDOR_LEVEL) {
			enterCorridorShaders();
		} else if (previous == ModDimensions.CORRIDOR_LEVEL) {
			restorePreviousShaders();
		}
	}

	private static void enterCorridorShaders() {
		try {
			IrisConfig config = Iris.getIrisConfig();
			savedPackName = config.getShaderPackName().orElse(null);
			savedShadersEnabled = config.areShadersEnabled();
			stateSaved = true;

			config.setShaderPackName(PACK_NAME);
			IrisApi.getInstance().getConfig().setShadersEnabledAndApply(true);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to switch to the corridor shaderpack", e);
		}
	}

	private static void restorePreviousShaders() {
		if (!stateSaved) {
			return;
		}
		stateSaved = false;

		try {
			IrisConfig config = Iris.getIrisConfig();
			config.setShaderPackName(savedPackName);
			IrisApi.getInstance().getConfig().setShadersEnabledAndApply(savedShadersEnabled);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to restore the previous shaderpack", e);
		}
	}
}
