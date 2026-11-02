package potatowolfie.web.entity.abominable_spider_queen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;

@Environment(EnvType.CLIENT)
public class AbominableSpiderQueenEyesFeatureRenderer extends EyesLayer<LivingEntityRenderState, SpiderModel> {
    private static final RenderType SKIN = RenderTypes.eyes(
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "textures/entity/spider/abominable_spider_queen_eyes.png"));

    public AbominableSpiderQueenEyesFeatureRenderer(RenderLayerParent<LivingEntityRenderState, SpiderModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return SKIN;
    }
}