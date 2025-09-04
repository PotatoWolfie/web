package potatowolfie.web;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import potatowolfie.web.block.WebBlockEntities;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.client.*;

public class WebClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        WebEntityModelLayers.registerModelLayers();

        EntityModelLayerRegistry.registerModelLayer(WebEntityModelLayers.SPIDER_WEB, SpiderWebModel::getTexturedModelData);
        EntityRendererRegistry.register(WebEntities.SPIDER_WEB, SpiderWebRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(WebEntityModelLayers.SPIDER_WEB_FLYING, SpiderWebProjectileModel::getTexturedModelData);
        EntityRendererRegistry.register(WebEntities.SPIDER_WEB_FLYING, SpiderWebProjectileRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(WebEntityModelLayers.BABY_SPIDER, BabySpiderModel::getTexturedModelData);
        EntityRendererRegistry.register(WebEntities.BABY_SPIDER, BabySpiderRenderer::new);


        BlockRenderLayerMap.putBlock(WebBlocks.SPIDER_EGG_SHELLS, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(WebBlocks.SPIDER_WEB_BLOCK, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(WebBlocks.SPIDER_GRASS, BlockRenderLayer.CUTOUT);

        BlockEntityRendererFactories.register(WebBlockEntities.SPIDER_WEB_BLOCK_ENTITY, SpiderWebBlockEntityRenderer::new);
    }
}