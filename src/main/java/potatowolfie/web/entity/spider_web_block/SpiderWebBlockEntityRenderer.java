package potatowolfie.web.entity.spider_web_block;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import potatowolfie.web.block.custom.SpiderWebBlock;
import potatowolfie.web.entity.client.WebEntityModelLayers;
import potatowolfie.web.entity.custom.SpiderWebBlockEntity;

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
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0, -1.5, 0.0);
        poseStack.scale(1.0f, 1.0f, 1.0f);
        poseStack.translate(0.45, 0, -0.5);

        submitNodeCollector.submitModelPart(
                this.model.getWeb(),
                poseStack,
                RenderTypes.armorCutoutNoCull(TEXTURE),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                null,
                -1,
                state.breakProgress,
                0
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
    }

    public static class SpiderWebRenderState extends BlockEntityRenderState {
        public boolean shouldRender;
    }
}