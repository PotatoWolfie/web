package potatowolfie.web.entity.baby_spider;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;

@Environment(EnvType.CLIENT)
public class BabySpiderEyesFeatureRenderer extends EyesFeatureRenderer<BabySpiderRenderState, BabySpiderModel> {
    private static final RenderLayer SKIN = RenderLayers.eyes(Identifier.of(Web.MOD_ID, "textures/entity/spider/baby_spider_eyes.png"));

    public BabySpiderEyesFeatureRenderer(FeatureRendererContext<BabySpiderRenderState, BabySpiderModel> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public RenderLayer getEyesTexture() {
        return SKIN;
    }
}