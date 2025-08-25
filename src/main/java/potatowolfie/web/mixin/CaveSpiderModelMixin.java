package potatowolfie.web.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.animation.SpiderAnimations;
import potatowolfie.web.entity.client.CaveSpiderEntityRenderState;

@Environment(EnvType.CLIENT)
@Mixin(SpiderEntityModel.class)
public class CaveSpiderModelMixin {

    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart rightHindLeg;
    @Shadow @Final private ModelPart leftHindLeg;
    @Shadow @Final private ModelPart rightMiddleFrontLeg;
    @Shadow @Final private ModelPart leftMiddleFrontLeg;
    @Shadow @Final private ModelPart rightFrontLeg;
    @Shadow @Final private ModelPart leftFrontLeg;

    @Unique
    private Animation spiderIdleAnimation;
    @Unique
    private Animation spiderWalkingAnimation;
    @Unique
    private Animation spiderShootingAnimation;
    @Unique
    private Animation caveSpiderIdleAnimation;
    @Unique
    private Animation caveSpiderWalkingAnimation;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initAllSpiderAnimations(ModelPart root, CallbackInfo ci) {
        this.spiderIdleAnimation = SpiderAnimations.SPIDER_IDLE.createAnimation(root);
        this.spiderWalkingAnimation = SpiderAnimations.SPIDER_WALK.createAnimation(root);
        this.spiderShootingAnimation = SpiderAnimations.SPIDER_SHOOT.createAnimation(root);
        this.caveSpiderIdleAnimation = SpiderAnimations.SPIDER_IDLE.createAnimation(root);
        this.caveSpiderWalkingAnimation = SpiderAnimations.SPIDER_WALK.createAnimation(root);
    }

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;)V", at = @At("TAIL"))
    private void applyAllSpiderAnimations(LivingEntityRenderState renderState, CallbackInfo ci) {
        // Handle cave spider animations
        if (renderState instanceof CaveSpiderEntityRenderState caveSpiderRenderState) {
            if (this.caveSpiderIdleAnimation != null && caveSpiderRenderState.idleAnimationState.isRunning()) {
                this.caveSpiderIdleAnimation.apply(caveSpiderRenderState.idleAnimationState, renderState.age, 1.0F);
            }

            if (this.caveSpiderWalkingAnimation != null && caveSpiderRenderState.walkingAnimationState.isRunning()) {
                this.caveSpiderWalkingAnimation.apply(caveSpiderRenderState.walkingAnimationState, renderState.age, 1.0F);
            }
        }
    }
}