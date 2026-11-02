package potatowolfie.web.entity.spider_web;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;

public class PoisonSpiderWebRenderer extends SpiderWebRenderer {

    private static final Identifier POISON_TEXTURE =
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "textures/entity/webs/poisonous_spider_web.png");

    public PoisonSpiderWebRenderer(EntityRendererProvider.Context context) {
        super(context, POISON_TEXTURE);
    }
}