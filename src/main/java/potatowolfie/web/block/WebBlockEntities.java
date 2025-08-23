package potatowolfie.web.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import potatowolfie.web.entity.custom.SpiderWebBlockEntity;

public class WebBlockEntities {

    public static final BlockEntityType<SpiderWebBlockEntity> SPIDER_WEB_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of("web", "spider_web_block_entity"),
                    FabricBlockEntityTypeBuilder.create(SpiderWebBlockEntity::new, WebBlocks.SPIDER_WEB_BLOCK).build()
            );

    public static void registerBlockEntities() {
    }
}