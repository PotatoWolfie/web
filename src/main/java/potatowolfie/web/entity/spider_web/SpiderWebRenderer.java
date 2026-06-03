package potatowolfie.web.entity.spider_web;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.entity.custom.SpiderWebEntity;

public class SpiderWebRenderer extends EntityRenderer<SpiderWebEntity, SpiderWebRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Web.MOD_ID, "textures/entity/webs/spider_web.png");
    private final SpiderWebModel model;

    public SpiderWebRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SpiderWebModel(context.bakeLayer(SpiderWebModel.SPIDER_WEB));
    }

    @Override
    public void submit(SpiderWebRenderState spiderWebRenderState, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraRenderState) {
        matrices.pushPose();
        matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0F));
        matrices.translate(0.0, -1.5, 0.0);
        matrices.scale(1.0f, 1.0f, 1.0f);

        this.model.setupAnim(spiderWebRenderState);

        queue.submitModel(
                this.model,
                spiderWebRenderState,
                matrices,
                RenderTypes.armorCutoutNoCull(this.getTexture(spiderWebRenderState)),
                spiderWebRenderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                spiderWebRenderState.outlineColor,
                null
        );

        matrices.popPose();
    }

    public Identifier getTexture(SpiderWebRenderState spiderWebRenderState) {
        return TEXTURE;
    }

    public SpiderWebRenderState createRenderState() {
        return new SpiderWebRenderState();
    }

    @Override
    public void extractRenderState(final SpiderWebEntity entity,
                                   final SpiderWebRenderState state,
                                   final float partialTicks) {

        super.extractRenderState(entity, state, partialTicks);
        state.webDieAnimationState.copyFrom(entity.webDieAnimationState);
    }
}