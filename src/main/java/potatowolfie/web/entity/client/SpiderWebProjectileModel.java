package potatowolfie.web.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.ProjectileEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

// Made with Blockbench 4.12.5

public class SpiderWebProjectileModel extends EntityModel<ProjectileEntityRenderState> {
	private final ModelPart bb_main;

	public SpiderWebProjectileModel(ModelPart root) {
		super(root);
        this.bb_main = root.getChild("bb_main");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create(), ModelTransform.origin(0.0F, 24.0F, 0.0F));

		ModelPartData cube_r1 = bb_main.addChild("cube_r1", ModelPartBuilder.create().uv(0, 10).cuboid(-2.5F, -2.5F, -5.0F, 5.0F, 5.0F, 10.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -2.5F, 0.0F, 0.0F, -1.5708F, 0.0F));
		return TexturedModelData.of(modelData, 32, 32);
	}

	@Override
	public void setAngles(ProjectileEntityRenderState state) {
	}

	public void renderWithTexture(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		bb_main.render(matrices, vertexConsumer, light, overlay, color);
	}
}