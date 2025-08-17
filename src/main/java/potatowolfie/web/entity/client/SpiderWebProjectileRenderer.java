package potatowolfie.web.entity.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.ProjectileEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.Colors;
import potatowolfie.web.Web;
import potatowolfie.web.entity.custom.SpiderWebProjectileEntity;

public class SpiderWebProjectileRenderer extends EntityRenderer<SpiderWebProjectileEntity, ProjectileEntityRenderState> {

    private static final Identifier TEXTURE = Identifier.of(Web.MOD_ID, "textures/entity/webs/spider_web_flying.png");
    private final SpiderWebProjectileModel model;

    public SpiderWebProjectileRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new SpiderWebProjectileModel(context.getPart(WebEntityModelLayers.SPIDER_WEB_FLYING));
    }

    @Override
    public ProjectileEntityRenderState createRenderState() {
        return new ProjectileEntityRenderState();
    }

    @Override
    public void updateRenderState(SpiderWebProjectileEntity entity, ProjectileEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
    }

    @Override
    public void render(ProjectileEntityRenderState state, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.yaw - 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(state.pitch));

        matrices.scale(1.0F, 1.0F, 1.0F);

        int color = Colors.WHITE;
        this.model.renderWithTexture(matrices,
                vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE)),
                light,
                OverlayTexture.DEFAULT_UV,
                color);

        matrices.pop();
        super.render(state, matrices, vertexConsumers, light);
    }

    public Identifier getTexture(ProjectileEntityRenderState state) {
        return TEXTURE;
    }
}