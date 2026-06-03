package potatowolfie.web.entity.baby_spider;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.entity.client.WebEntityModelLayers;
import potatowolfie.web.entity.custom.BabySpiderEntity;

@Environment(EnvType.CLIENT)
public class BabySpiderRenderer extends MobRenderer<BabySpiderEntity, BabySpiderRenderState, BabySpiderModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Web.MOD_ID, "textures/entity/spider/spiderling.png");

    public BabySpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new BabySpiderModel(context.bakeLayer(WebEntityModelLayers.BABY_SPIDER)), 0.35F);
        this.addLayer(new BabySpiderEyesFeatureRenderer(this));
    }

    @Override
    public Identifier getTextureLocation(BabySpiderRenderState state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0F;
    }

    @Override
    public void submit(final BabySpiderRenderState state,
                       final PoseStack poseStack,
                       final SubmitNodeCollector submitNodeCollector,
                       final CameraRenderState camera) {

        poseStack.pushPose();
        poseStack.scale(0.75F, 0.75F, 0.75F);

        super.submit(state, poseStack, submitNodeCollector, camera);

        poseStack.popPose();
    }

    @Override
    public BabySpiderRenderState createRenderState() {
        return new BabySpiderRenderState();
    }

    @Override
    public void extractRenderState(final BabySpiderEntity entity,
                                   final BabySpiderRenderState state,
                                   final float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkingAnimationState.copyFrom(entity.walkingAnimationState);
    }
}