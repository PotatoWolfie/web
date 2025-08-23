package potatowolfie.web.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import potatowolfie.web.animation.BabySpiderAnimations;

// Made with Blockbench 4.12.5

@Environment(EnvType.CLIENT)
public class BabySpiderModel extends EntityModel<BabySpiderRenderState> {
    private final ModelPart spider;
    private final ModelPart body0;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart leg5;
    private final ModelPart leg6;
    private final ModelPart leg7;
    private final ModelPart body1;
    private final ModelPart head;
    private final Animation idlingAnimation;
    private final Animation walkingAnimation;

    public BabySpiderModel(ModelPart root) {
        super(root);
        this.spider = root.getChild("spider");
        this.body0 = this.spider.getChild("body0");
        this.leg0 = this.spider.getChild("leg0");
        this.leg1 = this.spider.getChild("leg1");
        this.leg2 = this.spider.getChild("leg2");
        this.leg3 = this.spider.getChild("leg3");
        this.leg4 = this.spider.getChild("leg4");
        this.leg5 = this.spider.getChild("leg5");
        this.leg6 = this.spider.getChild("leg6");
        this.leg7 = this.spider.getChild("leg7");
        this.body1 = this.spider.getChild("body1");
        this.head = this.spider.getChild("head");
        this.idlingAnimation = BabySpiderAnimations.SPIDER_IDLE.createAnimation(root);
        this.walkingAnimation = BabySpiderAnimations.SPIDER_WALK.createAnimation(root);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData spider = modelPartData.addChild("spider", ModelPartBuilder.create(), ModelTransform.origin(0.0F, 27.0F, 0.0F));

        ModelPartData body0 = spider.addChild("body0", ModelPartBuilder.create().uv(32, 20).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new Dilation(-1.0F)), ModelTransform.origin(0.0F, -9.0F, 0.0F));

        ModelPartData leg0 = spider.addChild("leg0", ModelPartBuilder.create().uv(32, 32).cuboid(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(-1.75F, -9.0F, 1.75F, 0.0F, 0.7854F, -0.7854F));

        ModelPartData leg1 = spider.addChild("leg1", ModelPartBuilder.create().uv(0, 36).cuboid(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(1.75F, -9.0F, 1.5F, 0.0F, -0.7854F, 0.7854F));

        ModelPartData leg2 = spider.addChild("leg2", ModelPartBuilder.create().uv(32, 32).cuboid(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(-1.75F, -9.0F, 0.75F, 0.0F, 0.2618F, -0.6109F));

        ModelPartData leg3 = spider.addChild("leg3", ModelPartBuilder.create().uv(0, 36).cuboid(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(1.75F, -9.0F, 0.5F, 0.0F, -0.2618F, 0.6109F));

        ModelPartData leg4 = spider.addChild("leg4", ModelPartBuilder.create().uv(32, 32).cuboid(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(-1.75F, -9.0F, -0.25F, 0.0F, -0.2618F, -0.6109F));

        ModelPartData leg5 = spider.addChild("leg5", ModelPartBuilder.create().uv(0, 36).cuboid(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(1.75F, -9.0F, -0.5F, 0.0F, 0.2618F, 0.6109F));

        ModelPartData leg6 = spider.addChild("leg6", ModelPartBuilder.create().uv(32, 32).cuboid(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(-1.75F, -9.0F, -1.25F, 0.0F, -0.7854F, -0.7854F));

        ModelPartData leg7 = spider.addChild("leg7", ModelPartBuilder.create().uv(0, 36).cuboid(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(1.75F, -9.0F, -1.5F, 0.0F, 0.7854F, 0.7854F));

        ModelPartData body1 = spider.addChild("body1", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -4.0F, 0.0F, 10.0F, 8.0F, 12.0F, new Dilation(-1.0F)), ModelTransform.origin(0.0F, -9.0F, 1.0F));

        ModelPartData head = spider.addChild("head", ModelPartBuilder.create().uv(0, 20).cuboid(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, new Dilation(-1.0F)), ModelTransform.origin(0.0F, -9.0F, -1.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(BabySpiderRenderState renderState) {
        super.setAngles(renderState);

        this.head.yaw = renderState.relativeHeadYaw * 0.017453292F;
        this.head.pitch = renderState.pitch * 0.017453292F;

        this.idlingAnimation.apply(renderState.idleAnimationState, renderState.age);
        this.walkingAnimation.apply(renderState.walkingAnimationState, renderState.age);

        float f = renderState.limbSwingAnimationProgress * 0.8F;
        float g = renderState.limbSwingAmplitude;

        float h = -(MathHelper.cos(f * 2.0F + 0.0F) * 0.4F) * g;
        float i = -(MathHelper.cos(f * 2.0F + 3.1415927F) * 0.4F) * g;
        float j = -(MathHelper.cos(f * 2.0F + 1.5707964F) * 0.4F) * g;
        float k = -(MathHelper.cos(f * 2.0F + 4.712389F) * 0.4F) * g;
        float l = Math.abs(MathHelper.sin(f + 0.0F) * 0.4F) * g;
        float m = Math.abs(MathHelper.sin(f + 3.1415927F) * 0.4F) * g;
        float n = Math.abs(MathHelper.sin(f + 1.5707964F) * 0.4F) * g;
        float o = Math.abs(MathHelper.sin(f + 4.712389F) * 0.4F) * g;

        this.leg0.yaw += h;
        this.leg1.yaw -= h;
        this.leg2.yaw += i;
        this.leg3.yaw -= i;
        this.leg4.yaw += j;
        this.leg5.yaw -= j;
        this.leg6.yaw += k;
        this.leg7.yaw -= k;

        this.leg0.roll += l;
        this.leg1.roll -= l;
        this.leg2.roll += m;
        this.leg3.roll -= m;
        this.leg4.roll += n;
        this.leg5.roll -= n;
        this.leg6.roll += o;
        this.leg7.roll -= o;
    }

    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        spider.render(matrices, vertexConsumer, light, overlay);
    }
}