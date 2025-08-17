package potatowolfie.web;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;
import potatowolfie.web.datagen.*;
import potatowolfie.web.world.feature.WebConfiguredFeatures;
import potatowolfie.web.world.feature.WebPlacedFeatures;
import potatowolfie.web.world.gen.WebWorldGeneration;

public class WebDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(WebBlockTagProvider::new);
		pack.addProvider(WebItemTagProvider::new);
		pack.addProvider(WebLootTableGenerator::new);
		pack.addProvider(WebModelProvider::new);
		pack.addProvider(WebRecipeGenerator::new);
		pack.addProvider(WebRegistryDataGenerator::new);
		pack.addProvider(WebWorldGenerator::new);
	}

	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, WebConfiguredFeatures::bootstrap);
		registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, WebPlacedFeatures::bootstrap);
	}

}
