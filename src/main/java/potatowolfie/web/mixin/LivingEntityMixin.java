package potatowolfie.web.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import potatowolfie.web.Web;
import potatowolfie.web.block.custom.SpiderWebBlock;
import potatowolfie.web.entity.custom.SpiderWebEntity;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Unique
    private static final TagKey<EntityType<?>> WEB_IMMUNE_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE,
            Identifier.of(Web.MOD_ID, "web_immune"));

    @Unique
    private static boolean isWebImmune(LivingEntity entity) {
        return entity.getType().isIn(WEB_IMMUNE_TAG);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void preventWebMovement(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;

        if (isWebImmune(entity)) {
            return;
        }

        if (SpiderWebEntity.shouldPreventMovement(entity)) {
            ci.cancel();
            entity.setVelocity(0, 0, 0);
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void preventWebJumping(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;

        if (isWebImmune(entity)) {
            return;
        }

        if (SpiderWebEntity.shouldPreventJumping(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "isClimbing", at = @At("HEAD"), cancellable = true)
    private void checkSpiderWebClimbing(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        BlockPos pos = entity.getBlockPos();
        BlockState state = entity.getWorld().getBlockState(pos);

        if (state.getBlock() instanceof SpiderWebBlock spiderWebBlock) {
            SpiderWebBlock.WebType webType = state.get(SpiderWebBlock.WEB_TYPE);
            if (webType != SpiderWebBlock.WebType.GROUND) {
                double centerX = pos.getX() + 0.5;
                double centerZ = pos.getZ() + 0.5;
                double entityX = entity.getX();
                double entityZ = entity.getZ();
                double distanceFromCenter = Math.sqrt(Math.pow(entityX - centerX, 2) + Math.pow(entityZ - centerZ, 2));

                if (distanceFromCenter <= 0.4) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}