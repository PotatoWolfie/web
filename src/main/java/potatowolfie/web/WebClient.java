package potatowolfie.web;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import potatowolfie.web.block.WebBlockEntities;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.entity.abominable_spider_queen.AbominableSpiderQueenModel;
import potatowolfie.web.entity.abominable_spider_queen.AbominableSpiderQueenRenderer;
import potatowolfie.web.particle.WebParticles;
import potatowolfie.web.particle.client.SpiderSucculentCloudParticle;
import potatowolfie.web.particle.client.WebBurstParticle;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.baby_spider.BabySpiderModel;
import potatowolfie.web.entity.baby_spider.BabySpiderRenderer;
import potatowolfie.web.entity.client.*;
import potatowolfie.web.entity.spider_web.PoisonSpiderWebRenderer;
import potatowolfie.web.entity.spider_web.SpiderWebModel;
import potatowolfie.web.entity.spider_web.SpiderWebRenderer;
import potatowolfie.web.entity.spider_web_block.SpiderWebBlockEntityRenderer;
import potatowolfie.web.entity.spider_web_projectile.PoisonSpiderWebProjectileRenderer;
import potatowolfie.web.entity.spider_web_projectile.SpiderWebProjectileModel;
import potatowolfie.web.entity.spider_web_projectile.SpiderWebProjectileRenderer;
import potatowolfie.web.particle.client.WebTrailParticle;

import java.util.List;

public class WebClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        WebEntityModelLayers.registerModelLayers();

        BlockColorRegistry.register(List.of(BlockTintSources.grass()), WebBlocks.SPROUTING_DAMP_SOIL);

        ModelLayerRegistry.registerModelLayer(
                WebEntityModelLayers.SPIDER_WEB,
                SpiderWebModel::getTexturedModelData
        );
        EntityRendererRegistry.register(WebEntities.SPIDER_WEB, SpiderWebRenderer::new);
        EntityRendererRegistry.register(WebEntities.POISON_SPIDER_WEB, PoisonSpiderWebRenderer::new);

        ModelLayerRegistry.registerModelLayer(
                WebEntityModelLayers.SPIDER_WEB_FLYING,
                SpiderWebProjectileModel::getTexturedModelData
        );
        EntityRendererRegistry.register(WebEntities.SPIDER_WEB_FLYING, SpiderWebProjectileRenderer::new);
        EntityRendererRegistry.register(WebEntities.POISON_SPIDER_WEB_FLYING, PoisonSpiderWebProjectileRenderer::new);

        ModelLayerRegistry.registerModelLayer(
                WebEntityModelLayers.BABY_SPIDER,
                BabySpiderModel::getTexturedModelData
        );
        EntityRendererRegistry.register(WebEntities.BABY_SPIDER, BabySpiderRenderer::new);

        ModelLayerRegistry.registerModelLayer(
                WebEntityModelLayers.ABOMINABLE_SPIDER_QUEEN,
                AbominableSpiderQueenModel::getTexturedModelData
        );
        EntityRendererRegistry.register(WebEntities.ABOMINABLE_SPIDER_QUEEN, AbominableSpiderQueenRenderer::new);

        BlockEntityRenderers.register(WebBlockEntities.SPIDER_WEB_BLOCK_ENTITY, SpiderWebBlockEntityRenderer::new);

        ParticleProviderRegistry.getInstance().register(WebParticles.WEB_BURST, WebBurstParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(WebParticles.SPIDER_EGG_POP, WebBurstParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(WebParticles.SPIDER_SUCCULENT_CLOUD, SpiderSucculentCloudParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(WebParticles.WEB_TRAIL, WebTrailParticle.WhiteFactory::new);
        ParticleProviderRegistry.getInstance().register(WebParticles.POISON_WEB_TRAIL, WebTrailParticle.PoisonFactory::new);
    }
}