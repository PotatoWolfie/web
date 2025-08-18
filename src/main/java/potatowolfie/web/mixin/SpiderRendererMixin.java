package potatowolfie.web.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.mob.SpiderEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import potatowolfie.web.entity.client.SpiderEntityRenderState;
import potatowolfie.web.interfaces.SpiderAnimationInterface;

@Environment(EnvType.CLIENT)
@Mixin(SpiderEntityRenderer.class)
public class SpiderRendererMixin {

    @Inject(method = "createRenderState", at = @At("RETURN"), cancellable = true)
    private void createCustomRenderState(CallbackInfoReturnable<LivingEntityRenderState> cir) {
        cir.setReturnValue(new SpiderEntityRenderState());
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/mob/SpiderEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void updateSpiderAnimations(SpiderEntity spiderEntity, LivingEntityRenderState renderState, float f, CallbackInfo ci) {
        if (spiderEntity instanceof SpiderAnimationInterface animatedSpider && renderState instanceof SpiderEntityRenderState spiderRenderState) {
            spiderRenderState.idleAnimationState.copyFrom(animatedSpider.web_1_21_6_7$getIdleAnimationState());
            spiderRenderState.walkingAnimationState.copyFrom(animatedSpider.web_1_21_6_7$getWalkingAnimationState());
        }
    }
}