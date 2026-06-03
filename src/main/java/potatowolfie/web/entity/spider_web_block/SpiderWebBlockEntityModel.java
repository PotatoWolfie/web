package potatowolfie.web.entity.spider_web_block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SpiderWebBlockEntityModel {
	private final ModelPart web;

	public SpiderWebBlockEntityModel(ModelPart modelPart) {
		this.web = modelPart.getChild("web");
	}

	public static LayerDefinition getTexturedModelData() {
		MeshDefinition modelData = new MeshDefinition();
		PartDefinition modelPartData = modelData.getRoot();
		PartDefinition web = modelPartData.addOrReplaceChild("web", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.163F, 33.3647F, -0.0993F, 0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = web.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-31.0F, -31.85F, 1.0F, 32.0F, 32.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.837F, 27.6353F, -0.9007F, 0.6507F, -0.1978F, 0.6245F));

		return LayerDefinition.create(modelData, 128, 128);
	}

	public ModelPart getWeb() {
		return this.web;
	}

	public void render(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay) {
		web.render(matrices, vertexConsumer, light, overlay);
	}
}