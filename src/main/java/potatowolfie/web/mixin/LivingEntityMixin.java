package potatowolfie.web.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
    private static final TagKey<EntityType<?>> WEB_IMMUNE_TAG = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "web_immune"));

    @Unique
    private static boolean isWebImmune(LivingEntity entity) {
        return entity.is(WEB_IMMUNE_TAG);
    }

    @Unique
    private static boolean web_shouldPreventMovement(LivingEntity entity) {
        if (isWebImmune(entity)) {
            return false;
        }

        if (SpiderWebEntity.shouldPreventMovement(entity)) {
            return true;
        }

        if (entity.level().isClientSide()) {
            java.util.List<SpiderWebEntity> webs = entity.level().getEntitiesOfClass(
                    SpiderWebEntity.class,
                    entity.getBoundingBox().inflate(0.5)
            );
            for (SpiderWebEntity web : webs) {
                if (web.isTrapping()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private static boolean web_shouldPreventJumping(LivingEntity entity) {
        if (isWebImmune(entity)) {
            return false;
        }

        if (SpiderWebEntity.shouldPreventJumping(entity)) {
            return true;
        }

        if (entity.level().isClientSide()) {
            java.util.List<SpiderWebEntity> webs = entity.level().getEntitiesOfClass(
                    SpiderWebEntity.class,
                    entity.getBoundingBox().inflate(0.5)
            );
            for (SpiderWebEntity web : webs) {
                if (web.isTrapping()) {
                    return true;
                }
            }
        }
        return false;
    }

    @org.spongepowered.asm.mixin.injection.ModifyVariable(
            method = "travel",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Vec3 scaleWebMovementInput(Vec3 movementInput) {
        LivingEntity entity = (LivingEntity)(Object)this;

        if (SpiderWebEntity.shouldPreventMovement(entity)) {
            boolean isPlayerOwnedWeb = false;

            if (!entity.level().isClientSide()) {
                SpiderWebEntity web = SpiderWebEntity.getWebTrappingEntity(entity);
                if (web != null && web.isOwnerPlayer()) {
                    isPlayerOwnedWeb = true;
                }
            }
            else {
                java.util.List<SpiderWebEntity> webs = entity.level().getEntitiesOfClass(
                        SpiderWebEntity.class,
                        entity.getBoundingBox().inflate(0.5)
                );
                for (SpiderWebEntity web : webs) {
                    if (web.isTrapping() && web.isOwnerPlayer()) {
                        isPlayerOwnedWeb = true;
                        break;
                    }
                }
            }

            if (isPlayerOwnedWeb) {
                double speedFactor = potatowolfie.web.entity.custom.SpiderWebEntity.PLAYER_WEB_SPEED_MULTIPLIER;

                return new Vec3(movementInput.x * speedFactor, movementInput.y, movementInput.z * speedFactor);
            }
        }

        return movementInput;
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void preventWebMovement(Vec3 movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;

        if (web_shouldPreventMovement(entity)) {
            boolean isPlayerOwnedWeb = false;

            if (!entity.level().isClientSide()) {
                SpiderWebEntity web = SpiderWebEntity.getWebTrappingEntity(entity);
                if (web != null && web.isOwnerPlayer()) {
                    isPlayerOwnedWeb = true;
                }
            } else {
                java.util.List<SpiderWebEntity> webs = entity.level().getEntitiesOfClass(
                        SpiderWebEntity.class,
                        entity.getBoundingBox().inflate(0.5)
                );
                for (SpiderWebEntity web : webs) {
                    if (web.isTrapping() && web.isOwnerPlayer()) {
                        isPlayerOwnedWeb = true;
                        break;
                    }
                }
            }

            if (isPlayerOwnedWeb) {
                return;
            }

            ci.cancel();

            if (entity instanceof Player) {
                entity.setDeltaMovement(0, entity.getDeltaMovement().y * 0.5, 0);
                entity.setSpeed(0.0f);
                entity.needsSync = true;
            } else {
                entity.setDeltaMovement(0, 0, 0);
            }
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void preventWebJumping(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;

        if (web_shouldPreventJumping(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
    private void checkSpiderWebClimbing(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        BlockPos pos = entity.blockPosition();
        BlockState state = entity.level().getBlockState(pos);

        if (state.getBlock() instanceof SpiderWebBlock spiderWebBlock) {
            SpiderWebBlock.WebType webType = state.getValue(SpiderWebBlock.WEB_TYPE);
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