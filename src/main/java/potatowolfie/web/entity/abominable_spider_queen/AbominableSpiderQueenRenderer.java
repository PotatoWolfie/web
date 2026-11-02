package potatowolfie.web.entity.abominable_spider_queen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.entity.client.WebEntityModelLayers;

@Environment(EnvType.CLIENT)
public class AbominableSpiderQueenRenderer extends SpiderRenderer<AbominableSpiderQueenEntity> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "textures/entity/spider/abominable_spider_queen.png");

    public AbominableSpiderQueenRenderer(EntityRendererProvider.Context context) {
        super(context, WebEntityModelLayers.ABOMINABLE_SPIDER_QUEEN);
        this.layers.clear();
        this.addLayer(new AbominableSpiderQueenEyesFeatureRenderer(this));
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}