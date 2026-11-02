package potatowolfie.web.entity.spider_web_block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import potatowolfie.web.block.custom.SpiderWebBlock;
import potatowolfie.web.entity.client.WebEntityModelLayers;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

@Environment(EnvType.CLIENT)
public class SpiderWebBlockEntityRenderer implements BlockEntityRenderer<SpiderWebBlockEntity, SpiderWebBlockEntityRenderer.SpiderWebRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("web", "textures/entity/webs/spider_web.png");
    private final SpiderWebBlockEntityModel model;

    public SpiderWebBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new SpiderWebBlockEntityModel(context.bakeLayer(WebEntityModelLayers.SPIDER_WEB_BLOCK));
    }

    @Override
    public SpiderWebRenderState createRenderState() {
        return new SpiderWebRenderState();
    }

    @Override
    public void submit(final SpiderWebRenderState state,
                       final PoseStack poseStack,
                       final SubmitNodeCollector submitNodeCollector,
                       final CameraRenderState camera) {

        if (!state.shouldRender) {
            return;
        }

        poseStack.pushPose();

        switch (state.facing) {
            case NORTH -> {
                poseStack.mulPose(new Matrix4f().rotation(Axis.XP.rotationDegrees(90.0F)));
                poseStack.translate(0.5, -0.5, -0.5);
            }
            case SOUTH -> {
                poseStack.mulPose(new Matrix4f().rotation(Axis.XP.rotationDegrees(-90.0F)));
                poseStack.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(-180.0F)));
                poseStack.translate(-0.5, -1.5, -0.5);
            }
            case WEST -> {
                poseStack.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(-90.0F)));
                poseStack.mulPose(new Matrix4f().rotation(Axis.XN.rotationDegrees(90.0F)));
                poseStack.translate(0.5, -0.5, 0.5);
            }
            case EAST -> {
                poseStack.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(90.0F)));
                poseStack.mulPose(new Matrix4f().rotation(Axis.XN.rotationDegrees(90.0F)));
                poseStack.translate(-0.5, -1.5, 0.5);
            }
            default -> {
                poseStack.mulPose(new Matrix4f().rotation(Axis.XP.rotationDegrees(180.0F)));
                poseStack.translate(0.0, -1.5, 0.0);
                poseStack.translate(0.45, 0, -0.5);
            }
        }

        submitNodeCollector.submitModelPart(
                this.model.getWeb(),
                poseStack,
                RenderTypes.armorCutoutNoCull(TEXTURE),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                null
        );

        poseStack.popPose();
    }

    @Override
    public void extractRenderState(final SpiderWebBlockEntity entity,
                                   final SpiderWebRenderState state,
                                   final float tickProgress,
                                   final Vec3 cameraPos,
                                   @Nullable final ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay);
        state.shouldRender = entity.getBlockState().getValue(SpiderWebBlock.WEB_TYPE) == SpiderWebBlock.WebType.GROUND;
        state.facing = entity.getBlockState().getValue(FACING);
    }

    public static class SpiderWebRenderState extends BlockEntityRenderState {
        public boolean shouldRender;
        public Direction facing = Direction.UP;
    }
}