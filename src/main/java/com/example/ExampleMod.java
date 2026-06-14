package com.example;

import com.example.entities.ModEntities;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "modid";
//To Do make it so you need a pearl per allay, raise allay health, give creative in inventory, animation for transformation, visual items, make it so if mob got hit they would attack
	// make amytest heal them, change drop chances for pearl

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModEntities.registerAttributes();
	}
}