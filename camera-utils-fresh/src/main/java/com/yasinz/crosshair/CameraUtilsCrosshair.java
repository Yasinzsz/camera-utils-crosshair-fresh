package com.yasinz.crosshair;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

public class CameraUtilsCrosshair implements ClientModInitializer {

	public static CrosshairConfig CONFIG;

	@Override
	public void onInitializeClient() {
		AutoConfig.register(
				CrosshairConfig.class,
				GsonConfigSerializer::new
		);

		CONFIG = AutoConfig
				.getConfigHolder(CrosshairConfig.class)
				.getConfig();

		AimAssist.register();
	}
}