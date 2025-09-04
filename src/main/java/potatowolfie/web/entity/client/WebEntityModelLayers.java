package potatowolfie.web.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;

@Environment(EnvType.CLIENT)
public class WebEntityModelLayers {
    public static final EntityModelLayer SPIDER_WEB =
            new EntityModelLayer(Identifier.of(Web.MOD_ID, "spider_web"), "main");

    public static final EntityModelLayer SPIDER_WEB_FLYING =
            new EntityModelLayer(Identifier.of(Web.MOD_ID, "spider_web_flying"), "main");

    public static final EntityModelLayer BABY_SPIDER =
            new EntityModelLayer(Identifier.of(Web.MOD_ID, "baby_spider"), "main");

    public static final EntityModelLayer SPIDER_WEB_BLOCK =
            new EntityModelLayer(Identifier.of(Web.MOD_ID, "spider_web_block"), "main");

    public static void registerModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(SPIDER_WEB_BLOCK, SpiderWebBlockEntityModel::getTexturedModelData);
    }
}