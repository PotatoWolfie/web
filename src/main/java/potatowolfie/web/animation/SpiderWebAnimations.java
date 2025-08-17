package potatowolfie.web.animation;

import net.minecraft.client.render.entity.animation.*;

/**
 * Made with Blockbench 4.12.5
 * @author PotatoWolfie
 */

public class SpiderWebAnimations {
	public static final AnimationDefinition WEB_DIE = AnimationDefinition.Builder.create(3.0F)
			.addBoneAnimation("web", new Transformation(Transformation.Targets.MOVE_ORIGIN,
					new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
					new Keyframe(2.0F, AnimationHelper.createTranslationalVector(0.0F, 3.25F, 0.0F), Transformation.Interpolations.CUBIC),
					new Keyframe(3.0F, AnimationHelper.createTranslationalVector(0.0F, 0.5F, 0.0F), Transformation.Interpolations.LINEAR)
			))
			.addBoneAnimation("web", new Transformation(Transformation.Targets.SCALE,
					new Keyframe(0.0F, AnimationHelper.createScalingVector(1.0F, 1.0F, 1.0F), Transformation.Interpolations.LINEAR),
					new Keyframe(3.0F, AnimationHelper.createScalingVector(1.0F, 0.3F, 1.0F), Transformation.Interpolations.LINEAR)
			))
			.build();
}