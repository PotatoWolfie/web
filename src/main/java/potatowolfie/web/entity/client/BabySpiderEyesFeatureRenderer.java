package potatowolfie.web.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;

@Environment(EnvType.CLIENT)
public class BabySpiderEyesFeatureRenderer extends EyesLayer<BabySpiderRenderState, BabySpiderModel> {
    private static final RenderType SKIN = RenderTypes.eyes(Identifier.fromNamespaceAndPath(Web.MOD_ID, "textures/entity/spider/spiderling_eyes.png"));

    public BabySpiderEyesFeatureRenderer(RenderLayerParent<BabySpiderRenderState, BabySpiderModel> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public RenderType renderType() {
        return SKIN;
    }
}