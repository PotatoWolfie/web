package potatowolfie.web.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class SpiderWebBlockEntityModel {
	private final ModelPart web;

	public SpiderWebBlockEntityModel(ModelPart modelPart) {
		this.web = modelPart.getChild("web");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData web = modelPartData.addChild("web", ModelPartBuilder.create(), ModelTransform.of(-0.163F, 33.3647F, -0.0993F, 0.0F, 0.0F, 0.0F));

		ModelPartData cube_r1 = web.addChild("cube_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-31.0F, -31.85F, 1.0F, 32.0F, 32.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(-0.837F, 27.6353F, -0.9007F, 0.6507F, -0.1978F, 0.6245F));

		return TexturedModelData.of(modelData, 128, 128);
	}

	public ModelPart getWeb() {
		return this.web;
	}

	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay) {
		web.render(matrices, vertexConsumer, light, overlay);
	}
}