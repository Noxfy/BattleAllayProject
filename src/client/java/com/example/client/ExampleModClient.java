package com.example.client;

import com.example.client.entities.BattleAllayEntityRenderer;
import com.example.client.entities.ModEntityModelLayers;
import com.example.entities.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class ExampleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ModEntityModelLayers.registerModelLayers();
		EntityRenderers.register(ModEntities.BATTLE_ALLAY, BattleAllayEntityRenderer::new);
	}
}