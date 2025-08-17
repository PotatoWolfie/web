package potatowolfie.web.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

// Made with Blockbench 4.12.5

@Environment(EnvType.CLIENT)
public class BabySpiderModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart spider;
    private final ModelPart body0;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightMiddleLeg;
    private final ModelPart leftMiddleLeg;
    private final ModelPart rightMiddleFrontLeg;
    private final ModelPart leftMiddleFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart body1;
    private final ModelPart head;

    public BabySpiderModel(ModelPart root) {
        super(root);
        this.spider = root.getChild("spider");
        this.body0 = this.spider.getChild("body0");
        this.rightHindLeg = this.spider.getChild("right_hind_leg");
        this.leftHindLeg = this.spider.getChild("left_hind_leg");
        this.rightMiddleLeg = this.spider.getChild("right_middle_hind_leg");
        this.leftMiddleLeg = this.spider.getChild("left_middle_hind_leg");
        this.rightMiddleFrontLeg = this.spider.getChild("right_middle_front_leg");
        this.leftMiddleFrontLeg = this.spider.getChild("left_middle_front_leg");
        this.rightFrontLeg = this.spider.getChild("right_front_leg");
        this.leftFrontLeg = this.spider.getChild("left_front_leg");
        this.body1 = this.spider.getChild("body1");
        this.head = this.spider.getChild("head");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData spider = modelPartData.addChild("spider", ModelPartBuilder.create(), ModelTransform.origin(0.0F, 28.0F, 0.0F));

        ModelPartData body0 = spider.addChild("body0", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new Dilation(-1.0F)), ModelTransform.origin(0.0F, -9.0F, 0.0F));

        ModelPartData leg0 = spider.addChild("right_hind_leg", ModelPartBuilder.create().uv(22, 0).cuboid(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(-1.75F, -9.0F, 1.75F, 0.0F, 0.7854F, -0.7854F));

        ModelPartData leg1 = spider.addChild("left_hind_leg", ModelPartBuilder.create().uv(18, 0).cuboid(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(1.75F, -9.0F, 1.5F, 0.0F, -0.7854F, 0.7854F));

        ModelPartData leg2 = spider.addChild("right_middle_hind_leg", ModelPartBuilder.create().uv(22, 0).cuboid(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(-1.75F, -9.0F, 0.75F, 0.0F, 0.2618F, -0.6109F));

        ModelPartData leg3 = spider.addChild("left_middle_hind_leg", ModelPartBuilder.create().uv(18, 0).cuboid(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(1.75F, -9.0F, 0.5F, 0.0F, -0.2618F, 0.6109F));

        ModelPartData leg4 = spider.addChild("right_middle_front_leg", ModelPartBuilder.create().uv(22, 0).cuboid(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(-1.75F, -9.0F, -0.25F, 0.0F, -0.2618F, -0.6109F));

        ModelPartData leg5 = spider.addChild("left_middle_front_leg", ModelPartBuilder.create().uv(18, 0).cuboid(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(1.75F, -9.0F, -0.5F, 0.0F, 0.2618F, 0.6109F));

        ModelPartData leg6 = spider.addChild("right_front_leg", ModelPartBuilder.create().uv(22, 0).cuboid(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(-1.75F, -9.0F, -1.25F, 0.0F, -0.7854F, -0.7854F));

        ModelPartData leg7 = spider.addChild("left_front_leg", ModelPartBuilder.create().uv(18, 0).cuboid(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(-0.25F)), ModelTransform.of(1.75F, -9.0F, -1.5F, 0.0F, 0.7854F, 0.7854F));

        ModelPartData body1 = spider.addChild("body1", ModelPartBuilder.create().uv(0, 12).cuboid(-5.0F, -4.0F, 0.0F, 10.0F, 8.0F, 12.0F, new Dilation(-1.0F)), ModelTransform.origin(0.0F, -9.0F, 1.0F));

        ModelPartData head = spider.addChild("head", ModelPartBuilder.create().uv(32, 4).cuboid(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, new Dilation(-1.0F)), ModelTransform.origin(0.0F, -9.0F, -1.0F));

        return TexturedModelData.of(modelData, 64, 32);
    }

    @Override
    public void setAngles(LivingEntityRenderState renderState) {
        super.setAngles(renderState);

        this.head.yaw = renderState.relativeHeadYaw * 0.017453292F;
        this.head.pitch = renderState.pitch * 0.017453292F;

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

        this.rightHindLeg.yaw += h;
        this.leftHindLeg.yaw -= h;
        this.rightMiddleLeg.yaw += i;
        this.leftMiddleLeg.yaw -= i;
        this.rightMiddleFrontLeg.yaw += j;
        this.leftMiddleFrontLeg.yaw -= j;
        this.rightFrontLeg.yaw += k;
        this.leftFrontLeg.yaw -= k;

        this.rightHindLeg.roll += l;
        this.leftHindLeg.roll -= l;
        this.rightMiddleLeg.roll += m;
        this.leftMiddleLeg.roll -= m;
        this.rightMiddleFrontLeg.roll += n;
        this.leftMiddleFrontLeg.roll -= n;
        this.rightFrontLeg.roll += o;
        this.leftFrontLeg.roll -= o;
    }

    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        spider.render(matrices, vertexConsumer, light, overlay);
    }
}