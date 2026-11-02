package potatowolfie.web.entity.spider_web;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import potatowolfie.web.Web;

public class SpiderWebRenderer extends EntityRenderer<SpiderWebEntity, SpiderWebRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Web.MOD_ID, "textures/entity/webs/spider_web.png");
    private final SpiderWebModel model;
    private final Identifier texture;

    public SpiderWebRenderer(EntityRendererProvider.Context context) {
        this(context, TEXTURE);
    }

    protected SpiderWebRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context);
        this.texture = texture;
        this.model = new SpiderWebModel(context.bakeLayer(SpiderWebModel.SPIDER_WEB));
    }

    @Override
    public void submit(SpiderWebRenderState spiderWebRenderState, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraRenderState) {
        matrices.pushPose();
        applyFacingTransform(matrices, spiderWebRenderState.facing);
        matrices.scale(1.0f, 1.0f, 1.0f);

        this.model.setupAnim(spiderWebRenderState);

        queue.submitModel(
                this.model,
                spiderWebRenderState,
                matrices,
                RenderTypes.armorCutoutNoCull(this.getTexture(spiderWebRenderState)),
                spiderWebRenderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                spiderWebRenderState.outlineColor
        );

        matrices.popPose();
    }

    private void applyFacingTransform(PoseStack matrices, Direction facing) {
        if (facing == null) {
            facing = Direction.DOWN;
        }

        switch (facing) {
            case UP:
                matrices.translate(0.0, -0.5, 0.0);
                break;
            case NORTH:
                matrices.mulPose(new Matrix4f().rotation(Axis.XP.rotationDegrees(90.0F)));
                matrices.translate(0.0, -1.0, -0.5);
                break;
            case SOUTH:
                matrices.mulPose(new Matrix4f().rotation(Axis.XP.rotationDegrees(-90.0F)));
                matrices.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(-180.0F)));
                matrices.translate(0.0, -1.0, -0.5);
                break;
            case WEST:
                matrices.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(-90.0F)));
                matrices.mulPose(new Matrix4f().rotation(Axis.XN.rotationDegrees(90.0F)));
                matrices.translate(0.0, -1.0, 0.5);
                break;
            case EAST:
                matrices.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(90.0F)));
                matrices.mulPose(new Matrix4f().rotation(Axis.XN.rotationDegrees(90.0F)));
                matrices.translate(0.0, -1.0, 0.5);
                break;
            case DOWN:
            default:
                matrices.mulPose(new Matrix4f().rotation(Axis.XP.rotationDegrees(180.0F)));
                matrices.translate(0.0, -1.5, 0.0);
                break;
        }
    }

    public Identifier getTexture(SpiderWebRenderState spiderWebRenderState) {
        return this.texture;
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
        state.facing = entity.getFacing();
    }
}