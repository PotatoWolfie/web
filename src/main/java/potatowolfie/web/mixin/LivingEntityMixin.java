package potatowolfie.web.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.entity.custom.SpiderWebEntity;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void preventWebMovement(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (SpiderWebEntity.shouldPreventMovement(entity)) {
            ci.cancel();
            entity.setVelocity(0, 0, 0);
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void preventWebJumping(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (SpiderWebEntity.shouldPreventJumping(entity)) {
            ci.cancel();
        }
    }
}