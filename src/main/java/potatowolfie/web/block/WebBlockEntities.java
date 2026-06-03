package potatowolfie.web.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import potatowolfie.web.entity.custom.SpiderWebBlockEntity;

public class WebBlockEntities {

    public static final BlockEntityType<SpiderWebBlockEntity> SPIDER_WEB_BLOCK_ENTITY =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath("web", "spider_web_block_entity"),
                    FabricBlockEntityTypeBuilder.create(SpiderWebBlockEntity::new, WebBlocks.SPIDER_WEB_BLOCK).build()
            );

    public static void registerBlockEntities() {
    }
}