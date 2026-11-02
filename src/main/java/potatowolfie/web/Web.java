package potatowolfie.web;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import potatowolfie.web.block.WebBlockEntities;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.config.WebConfig;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.baby_spider.BabySpiderEntity;
import potatowolfie.web.item.WebItems;
import potatowolfie.web.particle.WebParticles;
import potatowolfie.web.sound.WebSounds;
import potatowolfie.web.world.structure.WebStructurePieceTypes;
import potatowolfie.web.world.structure.WebStructureTypes;
import potatowolfie.web.world.feature.WebFeatures;
import potatowolfie.web.world.feature.WebPlacementModifierTypes;
import potatowolfie.web.world.gen.WebBiomeModifications;
import potatowolfie.web.world.gen.WebWorldGeneration;

public class Web implements ModInitializer {
	public static final String MOD_ID = "web";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final TagKey<EntityType<?>> WEB_IMMUNE_TAG = TagKey.create(Registries.ENTITY_TYPE,
			Identifier.fromNamespaceAndPath(MOD_ID, "web_immune"));

	@Override
	public void onInitialize() {
		WebBlocks.registerModBlocks();
		WebItems.registerModItems();
		WebEntities.registerModEntities();
		WebBlockEntities.registerBlockEntities();
		WebFeatures.registerFeatures();
		WebSounds.registerSounds();
		WebParticles.registerModParticles();
		WebWorldGeneration.init();
		WebBiomeModifications.init();
		WebStructureTypes.registerStructureTypes();
		WebStructurePieceTypes.registerStructurePieceTypes();
		WebConfig.load();
		WebPlacementModifierTypes.init();

		DispenserBlock.registerProjectileBehavior(WebItems.SPIDER_WEB);

		FabricDefaultAttributeRegistry.register(WebEntities.BABY_SPIDER, BabySpiderEntity.createSpiderAttributes());
	}
}