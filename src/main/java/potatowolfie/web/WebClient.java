package potatowolfie.web;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import potatowolfie.web.block.WebBlockEntities;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.baby_spider.BabySpiderModel;
import potatowolfie.web.entity.baby_spider.BabySpiderRenderer;
import potatowolfie.web.entity.client.*;
import potatowolfie.web.entity.spider_web.SpiderWebModel;
import potatowolfie.web.entity.spider_web.SpiderWebRenderer;
import potatowolfie.web.entity.spider_web_block.SpiderWebBlockEntityRenderer;
import potatowolfie.web.entity.spider_web_projectile.SpiderWebProjectileModel;
import potatowolfie.web.entity.spider_web_projectile.SpiderWebProjectileRenderer;

public class WebClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        WebEntityModelLayers.registerModelLayers();

        ModelLayerRegistry.registerModelLayer(
                WebEntityModelLayers.SPIDER_WEB,
                SpiderWebModel::getTexturedModelData
        );
        EntityRendererRegistry.register(WebEntities.SPIDER_WEB, SpiderWebRenderer::new);

        ModelLayerRegistry.registerModelLayer(
                WebEntityModelLayers.SPIDER_WEB_FLYING,
                SpiderWebProjectileModel::getTexturedModelData
        );
        EntityRendererRegistry.register(WebEntities.SPIDER_WEB_FLYING, SpiderWebProjectileRenderer::new);

        ModelLayerRegistry.registerModelLayer(
                WebEntityModelLayers.BABY_SPIDER,
                BabySpiderModel::getTexturedModelData
        );
        EntityRendererRegistry.register(WebEntities.BABY_SPIDER, BabySpiderRenderer::new);

        BlockEntityRenderers.register(WebBlockEntities.SPIDER_WEB_BLOCK_ENTITY, SpiderWebBlockEntityRenderer::new);
    }
}