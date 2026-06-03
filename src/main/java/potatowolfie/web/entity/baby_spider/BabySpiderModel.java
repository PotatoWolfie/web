package potatowolfie.web.entity.baby_spider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
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
    private final KeyframeAnimation idlingAnimation;
    private final KeyframeAnimation walkingAnimation;

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
        this.idlingAnimation = BabySpiderAnimations.SPIDER_IDLE.bake(root);
        this.walkingAnimation = BabySpiderAnimations.SPIDER_WALK.bake(root);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition spider = modelPartData.addOrReplaceChild("spider", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition leg0 = spider.addOrReplaceChild("leg0", CubeListBuilder.create().texOffs(24, 16).addBox(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, -5.75F, 1.0F, -0.0436F, 0.7844F, -0.8471F));

        PartDefinition leg1 = spider.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(24, 20).addBox(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, -5.75F, 1.0F, -0.0436F, -0.7844F, 0.8471F));

        PartDefinition leg2 = spider.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(24, 16).addBox(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, -5.75F, 0.75F, 0.0F, 0.2618F, -0.6109F));

        PartDefinition leg3 = spider.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(24, 20).addBox(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, -5.75F, 0.25F, 0.0F, -0.2618F, 0.6109F));

        PartDefinition leg4 = spider.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(24, 16).addBox(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, -5.75F, 0.0F, 0.0F, -0.2618F, -0.6109F));

        PartDefinition leg5 = spider.addOrReplaceChild("leg5", CubeListBuilder.create().texOffs(24, 20).addBox(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, -5.75F, -0.25F, 0.0F, 0.2618F, 0.6109F));

        PartDefinition leg6 = spider.addOrReplaceChild("leg6", CubeListBuilder.create().texOffs(24, 16).addBox(-11.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, -5.75F, -0.5F, 0.0436F, -0.7844F, -0.8471F));

        PartDefinition leg7 = spider.addOrReplaceChild("leg7", CubeListBuilder.create().texOffs(24, 20).addBox(-1.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, -5.75F, -0.75F, 0.0436F, 0.7844F, 0.8471F));

        PartDefinition head = spider.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, -3.0F, -7.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, -1.0F));

        PartDefinition body0 = spider.addOrReplaceChild("body0", CubeListBuilder.create().texOffs(24, 24).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

        PartDefinition body1 = spider.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.0F, 1.0F, 8.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 1.0F));
        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public void setupAnim(BabySpiderRenderState renderState) {
        super.setupAnim(renderState);

        this.head.yRot = renderState.yRot * 0.017453292F;
        this.head.xRot = renderState.xRot * 0.017453292F;

        this.idlingAnimation.apply(renderState.idleAnimationState, renderState.ageInTicks);
        this.walkingAnimation.apply(renderState.walkingAnimationState, renderState.ageInTicks);

        float f = renderState.walkAnimationPos * 0.8F;
        float g = renderState.walkAnimationSpeed;

        float h = -(Mth.cos(f * 2.0F + 0.0F) * 0.4F) * g;
        float i = -(Mth.cos(f * 2.0F + 3.1415927F) * 0.4F) * g;
        float j = -(Mth.cos(f * 2.0F + 1.5707964F) * 0.4F) * g;
        float k = -(Mth.cos(f * 2.0F + 4.712389F) * 0.4F) * g;
        float l = Math.abs(Mth.sin(f + 0.0F) * 0.4F) * g;
        float m = Math.abs(Mth.sin(f + 3.1415927F) * 0.4F) * g;
        float n = Math.abs(Mth.sin(f + 1.5707964F) * 0.4F) * g;
        float o = Math.abs(Mth.sin(f + 4.712389F) * 0.4F) * g;

        this.leg0.yRot += h;
        this.leg1.yRot -= h;
        this.leg2.yRot += i;
        this.leg3.yRot -= i;
        this.leg4.yRot += j;
        this.leg5.yRot -= j;
        this.leg6.yRot += k;
        this.leg7.yRot -= k;

        this.leg0.zRot += l;
        this.leg1.zRot -= l;
        this.leg2.zRot += m;
        this.leg3.zRot -= m;
        this.leg4.zRot += n;
        this.leg5.zRot -= n;
        this.leg6.zRot += o;
        this.leg7.zRot -= o;
    }

    public void render(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        spider.render(matrices, vertexConsumer, light, overlay);
    }
}