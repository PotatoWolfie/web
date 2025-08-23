package potatowolfie.web.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.animation.SpiderWebAnimations;

// Made with Blockbench 4.12.5

public class SpiderWebModel extends EntityModel<SpiderWebRenderState> {
	public static final EntityModelLayer SPIDER_WEB = new EntityModelLayer(Identifier.of(Web.MOD_ID, "spider_web"), "main");
	private final ModelPart web;

	private final Animation webDieAnimation;

	public SpiderWebModel(ModelPart modelPart) {
		super(modelPart);
		this.web = modelPart.getChild("web");

		this.webDieAnimation = SpiderWebAnimations.WEB_DIE.createAnimation(modelPart);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData web = modelPartData.addChild("web", ModelPartBuilder.create(), ModelTransform.of(-0.163F, 33.3647F, -0.0993F, 0.0F, 0.0F, 0.0F));

		ModelPartData cube_r1 = web.addChild("cube_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-31.0F, -31.85F, 1.0F, 32.0F, 32.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(-0.837F, 27.6353F, -0.9007F, 0.6507F, -0.1978F, 0.6245F));

		return TexturedModelData.of(modelData, 128, 128);
	}

	public void setAngles(SpiderWebRenderState spiderWebRenderState) {
		super.setAngles(spiderWebRenderState);

		this.webDieAnimation.apply(spiderWebRenderState.webDieAnimationState, spiderWebRenderState.age);
	}

	public ModelPart getWeb() {
		return this.web;
	}
}