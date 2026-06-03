package potatowolfie.web.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.animation.SpiderAnimations;
import potatowolfie.web.entity.client.SpiderEntityRenderState;

@Environment(EnvType.CLIENT)
@Mixin(SpiderModel.class)
public class SpiderModelMixin {

    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart rightHindLeg;
    @Shadow @Final private ModelPart leftHindLeg;
    @Shadow @Final private ModelPart rightMiddleFrontLeg;
    @Shadow @Final private ModelPart leftMiddleFrontLeg;
    @Shadow @Final private ModelPart rightFrontLeg;
    @Shadow @Final private ModelPart leftFrontLeg;

    @Unique
    private KeyframeAnimation idleAnimation;
    @Unique
    private KeyframeAnimation walkingAnimation;
    @Unique
    private KeyframeAnimation shootingAnimation;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initAnimations(ModelPart root, CallbackInfo ci) {
        this.idleAnimation = SpiderAnimations.SPIDER_IDLE.bake(root);
        this.walkingAnimation = SpiderAnimations.SPIDER_WALK.bake(root);
        this.shootingAnimation = SpiderAnimations.SPIDER_SHOOT.bake(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)V", at = @At("TAIL"))
    private void applyCustomAnimations(LivingEntityRenderState renderState, CallbackInfo ci) {
        if (renderState instanceof SpiderEntityRenderState spiderRenderState) {
            if (this.idleAnimation != null && spiderRenderState.idleAnimationState.isStarted()) {
                this.idleAnimation.apply(spiderRenderState.idleAnimationState, renderState.ageInTicks, 1.0F);
            }

            if (this.walkingAnimation != null && spiderRenderState.walkingAnimationState.isStarted()) {
                this.walkingAnimation.apply(spiderRenderState.walkingAnimationState, renderState.ageInTicks, 1.0F);
            }

            if (this.shootingAnimation != null && spiderRenderState.shootingAnimationState.isStarted()) {
                this.shootingAnimation.apply(spiderRenderState.shootingAnimationState, renderState.ageInTicks, 1.0F);
            }
        }
    }
}