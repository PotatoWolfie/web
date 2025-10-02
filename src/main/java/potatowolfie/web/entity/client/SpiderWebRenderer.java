package potatowolfie.web.entity.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.entity.custom.SpiderWebEntity;

public class SpiderWebRenderer extends EntityRenderer<SpiderWebEntity, SpiderWebRenderState> {

    private static final Identifier TEXTURE = Identifier.of(Web.MOD_ID, "textures/entity/webs/spider_web.png");
    private final SpiderWebModel model;

    public SpiderWebRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new SpiderWebModel(context.getPart(SpiderWebModel.SPIDER_WEB));
    }

    @Override
    public void render(SpiderWebRenderState spiderWebRenderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
        matrices.push();
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
        matrices.translate(0.0, -1.5, 0.0);
        matrices.scale(1.0f, 1.0f, 1.0f);

        this.model.setAngles(spiderWebRenderState);

        queue.submitModel(
                this.model,
                spiderWebRenderState,
                matrices,
                RenderLayer.getEntityCutoutNoCull(this.getTexture(spiderWebRenderState)),
                spiderWebRenderState.light,
                OverlayTexture.DEFAULT_UV,
                spiderWebRenderState.outlineColor,
                null
        );

        matrices.pop();
    }

    public Identifier getTexture(SpiderWebRenderState spiderWebRenderState) {
        return TEXTURE;
    }

    public SpiderWebRenderState createRenderState() {
        return new SpiderWebRenderState();
    }

    public void updateRenderState(SpiderWebEntity spiderWebEntity, SpiderWebRenderState spiderWebRenderState, float f) {
        super.updateRenderState(spiderWebEntity, spiderWebRenderState, f);
        spiderWebRenderState.webDieAnimationState.copyFrom(spiderWebEntity.webDieAnimationState);
    }
}