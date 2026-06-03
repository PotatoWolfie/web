package potatowolfie.web.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.monster.spider.Spider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import potatowolfie.web.entity.client.SpiderEntityRenderState;
import potatowolfie.web.interfaces.SpiderAnimationInterface;

@Environment(EnvType.CLIENT)
@Mixin(SpiderRenderer.class)
public class SpiderRendererMixin {

    @Inject(method = "createRenderState", at = @At("RETURN"), cancellable = true)
    private void createCustomRenderState(CallbackInfoReturnable<LivingEntityRenderState> cir) {
        cir.setReturnValue(new SpiderEntityRenderState());
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/spider/Spider;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void updateSpiderAnimations(Spider spiderEntity, LivingEntityRenderState renderState, float f, CallbackInfo ci) {
        if (spiderEntity instanceof SpiderAnimationInterface animatedSpider && renderState instanceof SpiderEntityRenderState spiderRenderState) {
            spiderRenderState.idleAnimationState.copyFrom(animatedSpider.web_1_21_6_7$getIdleAnimationState());
            spiderRenderState.walkingAnimationState.copyFrom(animatedSpider.web_1_21_6_7$getWalkingAnimationState());
            spiderRenderState.shootingAnimationState.copyFrom(animatedSpider.web_1_21_6_7$getShootingAnimationState());
        }
    }
}