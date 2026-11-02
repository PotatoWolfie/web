package potatowolfie.web.entity.spider_web_projectile;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;

public class PoisonSpiderWebProjectileRenderer extends SpiderWebProjectileRenderer {

    private static final Identifier POISON_TEXTURE =
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "textures/entity/webs/poisonous_spider_web_flying.png");

    public PoisonSpiderWebProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, POISON_TEXTURE);
    }
}