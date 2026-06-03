package potatowolfie.web.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.entity.spider_web_block.SpiderWebBlockEntityModel;

@Environment(EnvType.CLIENT)
public class WebEntityModelLayers {
    public static final ModelLayerLocation SPIDER_WEB =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_web"), "main");

    public static final ModelLayerLocation SPIDER_WEB_FLYING =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_web_flying"), "main");

    public static final ModelLayerLocation BABY_SPIDER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Web.MOD_ID, "baby_spider"), "main");

    public static final ModelLayerLocation SPIDER_WEB_BLOCK =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_web_block"), "main");

    public static void registerModelLayers() {
        ModelLayerRegistry.registerModelLayer(
                WebEntityModelLayers.SPIDER_WEB_BLOCK,
                SpiderWebBlockEntityModel::getTexturedModelData
        );
    }
}