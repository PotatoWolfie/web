package potatowolfie.web.interfaces;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public interface CaveSpiderRenderStateMixin {
    @Unique
    void createCustomRenderState(CallbackInfoReturnable<LivingEntityRenderState> cir);

    @Unique
    void updateCaveSpiderAnimations(CaveSpider caveSpiderEntity, LivingEntityRenderState renderState, float f, CallbackInfo ci);
}
