package potatowolfie.web.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.entity.custom.SpiderWebProjectileEntity;

public class SpiderWebProjectileRenderer extends ArrowRenderer<SpiderWebProjectileEntity, ArrowRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Web.MOD_ID, "textures/entity/webs/spider_web_flying.png");
    private final SpiderWebProjectileModel model;

    public SpiderWebProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SpiderWebProjectileModel(context.bakeLayer(WebEntityModelLayers.SPIDER_WEB_FLYING));
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    public void extractRenderState(final SpiderWebProjectileEntity entity,
                                   final ArrowRenderState state,
                                   final float tickDelta) {

        if (entity == null || state == null) {
            return;
        }

        super.extractRenderState(entity, state, tickDelta);
    }

    @Override
    public void submit(final ArrowRenderState renderState,
                       final PoseStack matrices,
                       final SubmitNodeCollector queue,
                       final CameraRenderState cameraRenderState) {

        matrices.pushPose();

        matrices.mulPose(Axis.YP.rotationDegrees(renderState.yRot - 90.0F));
        matrices.mulPose(Axis.ZP.rotationDegrees(renderState.xRot));

        matrices.scale(1.0F, 1.0F, 1.0F);
        matrices.translate(-0.156F, -1.1875F, 0.0F);

        queue.submitModel(
                this.model,
                renderState,
                matrices,
                RenderTypes.armorCutoutNoCull(this.getTextureLocation(renderState)),
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                renderState.outlineColor,
                null
        );

        super.submit(renderState, matrices, queue, cameraRenderState);

        matrices.popPose();
    }

    @Override
    public Identifier getTextureLocation(ArrowRenderState state) {
        return TEXTURE;
    }
}