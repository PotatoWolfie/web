package potatowolfie.web.interfaces;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.mob.CaveSpiderEntity;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public interface CaveSpiderRenderStateMixin {
    @Unique
    void createCustomRenderState(CallbackInfoReturnable<LivingEntityRenderState> cir);

    @Unique
    void updateCaveSpiderAnimations(CaveSpiderEntity caveSpiderEntity, LivingEntityRenderState renderState, float f, CallbackInfo ci);
}
