package potatowolfie.web;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import potatowolfie.web.block.WebBlockEntities;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.client.WebEntityModelLayers;
import potatowolfie.web.entity.custom.BabySpiderEntity;
import potatowolfie.web.item.WebItems;
import potatowolfie.web.world.feature.WebFeatures;
import potatowolfie.web.world.gen.WebBiomeModifications;
import potatowolfie.web.world.gen.WebWorldGeneration;

public class Web implements ModInitializer {
	public static final String MOD_ID = "web";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final TagKey<EntityType<?>> WEB_IMMUNE_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE,
			Identifier.of(MOD_ID, "web_immune"));

	@Override
	public void onInitialize() {
		WebBlocks.registerModBlocks();
		WebItems.registerModItems();
		WebEntities.registerModEntities();
		WebBlockEntities.registerBlockEntities();
		WebEntityModelLayers.registerModelLayers();
		WebFeatures.registerFeatures();
		WebWorldGeneration.init();
		WebBiomeModifications.init();

		FabricDefaultAttributeRegistry.register(WebEntities.BABY_SPIDER, BabySpiderEntity.createSpiderAttributes());
	}
}