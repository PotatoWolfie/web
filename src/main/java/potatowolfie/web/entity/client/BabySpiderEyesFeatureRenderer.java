package potatowolfie.web.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;

@Environment(EnvType.CLIENT)
public class BabySpiderEyesFeatureRenderer extends EyesFeatureRenderer<BabySpiderRenderState, BabySpiderModel> {
    private static final RenderLayer SKIN = RenderLayer.getEyes(Identifier.of(Web.MOD_ID, "textures/entity/spider/baby_spider_eyes.png"));

    public BabySpiderEyesFeatureRenderer(FeatureRendererContext<BabySpiderRenderState, BabySpiderModel> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public RenderLayer getEyesTexture() {
        return SKIN;
    }
}