package potatowolfie.web.entity.client;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;

public class WebEntityModelLayers {

    public static final EntityModelLayer SPIDER_WEB =
            new EntityModelLayer(Identifier.of(Web.MOD_ID, "spider_web"), "main");

    public static final EntityModelLayer SPIDER_WEB_FLYING =
            new EntityModelLayer(Identifier.of(Web.MOD_ID, "spider_web_flying"), "main");

    public static final EntityModelLayer BABY_SPIDER =
            new EntityModelLayer(Identifier.of(Web.MOD_ID, "baby_spider"), "main");
}