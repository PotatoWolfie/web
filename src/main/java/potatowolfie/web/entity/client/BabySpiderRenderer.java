package potatowolfie.web.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.entity.custom.BabySpiderEntity;

@Environment(EnvType.CLIENT)
public class BabySpiderRenderer extends MobEntityRenderer<BabySpiderEntity, BabySpiderRenderState, BabySpiderModel> {
    private static final Identifier TEXTURE = Identifier.of(Web.MOD_ID, "textures/entity/spider/baby_spider.png");

    public BabySpiderRenderer(EntityRendererFactory.Context context) {
        super(context, new BabySpiderModel(context.getPart(WebEntityModelLayers.BABY_SPIDER)), 0.35F);
        this.addFeature(new BabySpiderEyesFeatureRenderer(this));
    }

    @Override
    protected float getLyingPositionRotationDegrees() {
        return 180.0F;
    }

    @Override
    public Identifier getTexture(BabySpiderRenderState state) {
        return TEXTURE;
    }

    @Override
    public void render(BabySpiderRenderState renderState, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light) {
        matrices.push();
        matrices.scale(0.75F, 0.75F, 0.75F);
        super.render(renderState, matrices, vertexConsumerProvider, light);
        matrices.pop();
    }

    @Override
    public BabySpiderRenderState createRenderState() {
        return new BabySpiderRenderState();
    }

    @Override
    public void updateRenderState(BabySpiderEntity spiderEntity, BabySpiderRenderState babySpiderRenderState, float f) {
        super.updateRenderState(spiderEntity, babySpiderRenderState, f);
        babySpiderRenderState.idleAnimationState.copyFrom(spiderEntity.idleAnimationState);
        babySpiderRenderState.walkingAnimationState.copyFrom(spiderEntity.walkingAnimationState);
    }
}