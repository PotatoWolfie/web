package potatowolfie.web;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import potatowolfie.web.advancement.BurnTheNestHandler;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.custom.BabySpiderEntity;
import potatowolfie.web.item.WebItems;
import potatowolfie.web.world.feature.WebFeatures;
import potatowolfie.web.world.gen.WebBiomeModifications;
import potatowolfie.web.world.gen.WebWorldGeneration;

public class Web implements ModInitializer {
	public static final String MOD_ID = "web";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WebBlocks.registerModBlocks();
		WebItems.registerModItems();
		WebEntities.registerModEntities();

		FabricDefaultAttributeRegistry.register(WebEntities.BABY_SPIDER, BabySpiderEntity.createSpiderAttributes());
		LOGGER.info("Registered BabySpiderEntity attributes");

		WebFeatures.registerFeatures();
		WebWorldGeneration.init();
		WebBiomeModifications.init();
	}
}