package potatowolfie.web.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import potatowolfie.web.block.custom.SpiderWebBlock;
import potatowolfie.web.entity.custom.SpiderWebBlockEntity;

@Environment(EnvType.CLIENT)
public class SpiderWebBlockEntityRenderer implements BlockEntityRenderer<SpiderWebBlockEntity> {
    private static final Identifier TEXTURE = Identifier.of("web", "textures/entity/webs/spider_web.png");
    private final SpiderWebBlockEntityModel model;

    public SpiderWebBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new SpiderWebBlockEntityModel(context.getLayerModelPart(WebEntityModelLayers.SPIDER_WEB_BLOCK));
    }

    @Override
    public void render(SpiderWebBlockEntity entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        if (entity.getCachedState().get(SpiderWebBlock.WEB_TYPE) != SpiderWebBlock.WebType.GROUND) {
            return;
        }

        matrices.push();
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
        matrices.translate(0.0, -1.5, 0.0);
        matrices.scale(1.0f, 1.0f, 1.0f);
        matrices.translate(0.45, 0, -0.5);


        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));

        model.renderForBlockEntity(matrices, vertexConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }
}