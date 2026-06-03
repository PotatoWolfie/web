package potatowolfie.web.entity.client;

import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.animation.SpiderWebAnimations;

// Made with Blockbench 4.12.5

public class SpiderWebModel extends EntityModel<SpiderWebRenderState> {
	public static final ModelLayerLocation SPIDER_WEB = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_web"), "main");
	private final ModelPart web;

	private final KeyframeAnimation webDieAnimation;

	public SpiderWebModel(ModelPart modelPart) {
		super(modelPart);
		this.web = modelPart.getChild("web");

		this.webDieAnimation = SpiderWebAnimations.WEB_DIE.bake(modelPart);
	}

	public static LayerDefinition getTexturedModelData() {
		MeshDefinition modelData = new MeshDefinition();
		PartDefinition modelPartData = modelData.getRoot();
		PartDefinition web = modelPartData.addOrReplaceChild("web", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.163F, 33.3647F, -0.0993F, 0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = web.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-31.0F, -31.85F, 1.0F, 32.0F, 32.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.837F, 27.6353F, -0.9007F, 0.6507F, -0.1978F, 0.6245F));

		return LayerDefinition.create(modelData, 128, 128);
	}

	@Override
	public void setupAnim(SpiderWebRenderState spiderWebRenderState) {
		super.setupAnim(spiderWebRenderState);

		this.webDieAnimation.apply(spiderWebRenderState.webDieAnimationState, spiderWebRenderState.ageInTicks);
	}

	public ModelPart getWeb() {
		return this.web;
	}
}