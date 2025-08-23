package potatowolfie.web.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;

@Environment(EnvType.CLIENT)
public class BabySpiderEyesFeatureRenderer extends FeatureRenderer<BabySpiderRenderState, BabySpiderModel> {
    private static final RenderLayer TEXTURE = RenderLayer.getEntityTranslucentEmissiveNoOutline(
            Identifier.of(Web.MOD_ID, "textures/entity/spider/baby_spider_eyes.png")
    );

    public BabySpiderEyesFeatureRenderer(FeatureRendererContext<BabySpiderRenderState, BabySpiderModel> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light,
                       BabySpiderRenderState babySpiderRenderState, float f, float g) {
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(TEXTURE);
        BabySpiderModel babySpiderModel = this.getContextModel();

        babySpiderModel.render(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}