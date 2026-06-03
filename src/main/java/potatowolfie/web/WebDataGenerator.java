package potatowolfie.web;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import potatowolfie.web.datagen.*;
import potatowolfie.web.world.feature.WebConfiguredFeatures;
import potatowolfie.web.world.feature.WebPlacedFeatures;

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
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		registryBuilder.add(Registries.CONFIGURED_FEATURE, WebConfiguredFeatures::bootstrap);
		registryBuilder.add(Registries.PLACED_FEATURE, WebPlacedFeatures::bootstrap);
	}

}
