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
import potatowolfie.web.entity.client.CaveSpiderEntityRenderState;

@Environment(EnvType.CLIENT)
@Mixin(SpiderModel.class)
public class CaveSpiderModelMixin {

    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart rightHindLeg;
    @Shadow @Final private ModelPart leftHindLeg;
    @Shadow @Final private ModelPart rightMiddleFrontLeg;
    @Shadow @Final private ModelPart leftMiddleFrontLeg;
    @Shadow @Final private ModelPart rightFrontLeg;
    @Shadow @Final private ModelPart leftFrontLeg;

    @Unique
    private KeyframeAnimation spiderIdleAnimation;
    @Unique
    private KeyframeAnimation spiderWalkingAnimation;
    @Unique
    private KeyframeAnimation spiderShootingAnimation;
    @Unique
    private KeyframeAnimation caveSpiderIdleAnimation;
    @Unique
    private KeyframeAnimation caveSpiderWalkingAnimation;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initAllSpiderAnimations(ModelPart root, CallbackInfo ci) {
        this.spiderIdleAnimation = SpiderAnimations.SPIDER_IDLE.bake(root);
        this.spiderWalkingAnimation = SpiderAnimations.SPIDER_WALK.bake(root);
        this.spiderShootingAnimation = SpiderAnimations.SPIDER_SHOOT.bake(root);
        this.caveSpiderIdleAnimation = SpiderAnimations.SPIDER_IDLE.bake(root);
        this.caveSpiderWalkingAnimation = SpiderAnimations.SPIDER_WALK.bake(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)V", at = @At("TAIL"))
    private void applyAllSpiderAnimations(LivingEntityRenderState renderState, CallbackInfo ci) {
        if (renderState instanceof CaveSpiderEntityRenderState caveSpiderRenderState) {
            if (this.caveSpiderIdleAnimation != null && caveSpiderRenderState.idleAnimationState.isStarted()) {
                this.caveSpiderIdleAnimation.apply(caveSpiderRenderState.idleAnimationState, renderState.ageInTicks, 1.0F);
            }

            if (this.caveSpiderWalkingAnimation != null && caveSpiderRenderState.walkingAnimationState.isStarted()) {
                this.caveSpiderWalkingAnimation.apply(caveSpiderRenderState.walkingAnimationState, renderState.ageInTicks, 1.0F);
            }
        }
    }
}