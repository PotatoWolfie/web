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
import potatowolfie.web.animation.BabySpiderAnimations;
import potatowolfie.web.animation.SpiderAnimations;
import potatowolfie.web.entity.client.SpiderEntityRenderState;

@Environment(EnvType.CLIENT)
@Mixin(SpiderEntityModel.class)
public class SpiderModelMixin {

    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart rightHindLeg;
    @Shadow @Final private ModelPart leftHindLeg;
    @Shadow @Final private ModelPart rightMiddleFrontLeg;
    @Shadow @Final private ModelPart leftMiddleFrontLeg;
    @Shadow @Final private ModelPart rightFrontLeg;
    @Shadow @Final private ModelPart leftFrontLeg;

    @Unique
    private Animation idleAnimation;
    @Unique
    private Animation walkingAnimation;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initAnimations(ModelPart root, CallbackInfo ci) {
        this.idleAnimation = SpiderAnimations.SPIDER_IDLE.createAnimation(root);
        this.walkingAnimation = SpiderAnimations.SPIDER_WALK.createAnimation(root);
    }

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;)V", at = @At("TAIL"))
    private void applyCustomAnimations(LivingEntityRenderState renderState, CallbackInfo ci) {
        if (renderState instanceof SpiderEntityRenderState spiderRenderState) {
            if (this.idleAnimation != null && spiderRenderState.idleAnimationState.isRunning()) {
                this.idleAnimation.apply(spiderRenderState.idleAnimationState, renderState.age, 1.0F);
            }

            if (this.walkingAnimation != null && spiderRenderState.walkingAnimationState.isRunning()) {
                this.walkingAnimation.apply(spiderRenderState.walkingAnimationState, renderState.age, 1.0F);
            }
        }
    }
}