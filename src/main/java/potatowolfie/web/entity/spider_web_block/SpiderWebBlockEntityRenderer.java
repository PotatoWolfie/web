package potatowolfie.web.entity.spider_web_block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import potatowolfie.web.block.custom.SpiderWebBlock;
import potatowolfie.web.entity.client.WebEntityModelLayers;
import potatowolfie.web.entity.custom.SpiderWebBlockEntity;

@Environment(EnvType.CLIENT)
public class SpiderWebBlockEntityRenderer implements BlockEntityRenderer<SpiderWebBlockEntity, SpiderWebBlockEntityRenderer.SpiderWebRenderState> {
    private static final Identifier TEXTURE = Identifier.of("web", "textures/entity/webs/spider_web.png");
    private final SpiderWebBlockEntityModel model;

    public SpiderWebBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new SpiderWebBlockEntityModel(context.getLayerModelPart(WebEntityModelLayers.SPIDER_WEB_BLOCK));
    }

    @Override
    public SpiderWebRenderState createRenderState() {
        return new SpiderWebRenderState();
    }

    @Override
    public void updateRenderState(SpiderWebBlockEntity entity, SpiderWebRenderState state, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderState.updateBlockEntityRenderState(entity, state, crumblingOverlay);
        state.shouldRender = entity.getCachedState().get(SpiderWebBlock.WEB_TYPE) == SpiderWebBlock.WebType.GROUND;
    }

    @Override
    public void render(SpiderWebRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (!state.shouldRender) {
            return;
        }

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
        matrices.translate(0.0, -1.5, 0.0);
        matrices.scale(1.0f, 1.0f, 1.0f);
        matrices.translate(0.45, 0, -0.5);

        queue.submitModelPart(
                this.model.getWeb(),
                matrices,
                RenderLayers.entityCutoutNoCull(TEXTURE),
                state.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                null,
                false,
                false,
                -1,
                state.crumblingOverlay,
                0
        );

        matrices.pop();
    }

    public static class SpiderWebRenderState extends BlockEntityRenderState {
        public boolean shouldRender;
    }
}