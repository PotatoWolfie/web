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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import potatowolfie.web.Web;
import potatowolfie.web.block.custom.GelClimber;
import potatowolfie.web.block.custom.SpiderWebBlock;
import potatowolfie.web.entity.spider_web.SpiderWebEntity;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements GelClimber {

    @Unique
    private int web$gelStickTicks = 0;

    @Unique
    private int web$gelCountTick = -1;

    @Unique
    private int web$gelClimbTick = -1000;

    @Unique
    private int web$gelHoldTick = -1000;

    @Unique
    private static final double GEL_HOLD_DRAG = 0.85;

    @Unique
    private static final double GEL_HOLD_INPUT_BOOST = 1.4;

    @Unique
    private static final double GEL_WALL_DRAG = 0.85;

    @Unique
    private static final double GEL_WALL_AIR_INPUT_BOOST = 1.4;

    @Unique
    private static final double GEL_WALL_CLIMB_Y_MULTIPLIER = 0.85;

    @Unique
    private static final double GEL_WALL_MAX_SLIDE_SPEED = -0.067;

    @Override
    public int web$getStickTicks() {
        return this.web$gelStickTicks;
    }

    @Override
    public void web$setStickTicks(int ticks) {
        this.web$gelStickTicks = ticks;
    }

    @Override
    public int web$getLastCountTick() {
        return this.web$gelCountTick;
    }

    @Override
    public void web$setLastCountTick(int tick) {
        this.web$gelCountTick = tick;
    }

    @Override
    public void web$allowGelClimb() {
        this.web$gelClimbTick = ((LivingEntity) (Object) this).tickCount;
    }

    @Override
    public boolean web$canGelClimb() {
        return ((LivingEntity) (Object) this).tickCount - this.web$gelClimbTick <= 1;
    }

    @Override
    public void web$allowGelHold() {
        this.web$gelHoldTick = ((LivingEntity) (Object) this).tickCount;
    }

    @Override
    public boolean web$isGelHolding() {
        return ((LivingEntity) (Object) this).tickCount - this.web$gelHoldTick <= 2;
    }

    @Override
    public void web$releaseGelHold() {
        this.web$gelHoldTick = -1000;
    }

    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    private Vec3 web$gelBoostInput(Vec3 movementInput) {
        if (this.web$isGelHolding()) {
            return new Vec3(movementInput.x * GEL_HOLD_INPUT_BOOST, movementInput.y, movementInput.z * GEL_HOLD_INPUT_BOOST);
        }
        if (this.web$isGelWallClinging() && !((LivingEntity) (Object) this).onGround()) {
            return new Vec3(movementInput.x * GEL_WALL_AIR_INPUT_BOOST, movementInput.y, movementInput.z * GEL_WALL_AIR_INPUT_BOOST);
        }
        return movementInput;
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void web$gelHoldTravel(Vec3 movementInput, CallbackInfo ci) {
        if (this.web$isGelHolding()) {
            LivingEntity self = (LivingEntity) (Object) this;
            Vec3 delta = self.getDeltaMovement();
            self.setDeltaMovement(delta.x * GEL_HOLD_DRAG, 0.0, delta.z * GEL_HOLD_DRAG);
            self.resetFallDistance();
        } else if (this.web$isGelWallClinging()) {
            LivingEntity self = (LivingEntity) (Object) this;
            Vec3 delta = self.getDeltaMovement();
            double y = delta.y > 0.0
                    ? delta.y * GEL_WALL_CLIMB_Y_MULTIPLIER
                    : Math.max(delta.y, GEL_WALL_MAX_SLIDE_SPEED);
            self.setDeltaMovement(delta.x * GEL_WALL_DRAG, y, delta.z * GEL_WALL_DRAG);
        }
    }

    @Unique
    private boolean web$isGelWallClinging() {
        return this.web$canGelClimb() && !this.web$isGelHolding();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void web$resetGelStickOnGround(CallbackInfo ci) {
        if (((LivingEntity) (Object) this).onGround()) {
            this.web$gelStickTicks = 0;
        }
    }

    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    private void web$gelClimbable(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!cir.getReturnValueZ() && !self.isSpectator() && !self.isFallFlying() && !this.web$isGelHolding() && this.web$canGelClimb()) {
            cir.setReturnValue(true);
        }
    }

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
    private static final double HANGING_WEB_CLIMB_SPEED_MULTIPLIER = 0.4;

    @Unique
    private static boolean web_isClimbingHangingWeb(LivingEntity entity) {
        BlockPos pos = entity.blockPosition();
        BlockState state = entity.level().getBlockState(pos);

        if (state.getBlock() instanceof SpiderWebBlock) {
            SpiderWebBlock.WebType webType = state.getValue(SpiderWebBlock.WEB_TYPE);
            if (webType != SpiderWebBlock.WebType.GROUND) {
                double centerX = pos.getX() + 0.5;
                double centerZ = pos.getZ() + 0.5;
                double distanceFromCenter = Math.sqrt(
                        Math.pow(entity.getX() - centerX, 2) + Math.pow(entity.getZ() - centerZ, 2));
                return distanceFromCenter <= 0.4;
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
                double speedFactor = SpiderWebEntity.PLAYER_WEB_SPEED_MULTIPLIER;

                return new Vec3(movementInput.x * speedFactor, movementInput.y, movementInput.z * speedFactor);
            }
        }

        return movementInput;
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void slowHangingWebClimb(Vec3 movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;

        if (isWebImmune(entity)) {
            return;
        }

        if (entity.onClimbable() && web_isClimbingHangingWeb(entity)) {
            Vec3 delta = entity.getDeltaMovement();
            entity.setDeltaMovement(
                    delta.x,
                    delta.y * HANGING_WEB_CLIMB_SPEED_MULTIPLIER,
                    delta.z
            );
        }
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

        if (web_isClimbingHangingWeb(entity)) {
            cir.setReturnValue(true);
        }
    }
}