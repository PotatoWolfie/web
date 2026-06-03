package potatowolfie.web.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.item.WebItems;
import potatowolfie.web.sound.WebSounds;

public class SpiderWebProjectileEntity extends AbstractArrow {

    public SpiderWebProjectileEntity(Level world, LivingEntity owner) {
        super(WebEntities.SPIDER_WEB_FLYING, world);
        if (owner != null) {
            this.setOwner(owner);
            this.setPos(owner.getX(), owner.getEyeY() - 0.3, owner.getZ());

            if (!world.isClientSide()) {
                world.playSound(null, owner.getX(), owner.getEyeY(), owner.getZ(),
                        WebSounds.WEB_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    public SpiderWebProjectileEntity(Level world, double x, double y, double z) {
        super(WebEntities.SPIDER_WEB_FLYING, world);
        this.setPos(x, y, z);
    }

    public SpiderWebProjectileEntity(EntityType<? extends SpiderWebProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 velocity = this.getDeltaMovement();
        if (velocity != null && velocity.lengthSqr() > 0.0001) {
            this.setRot(
                    (float)(Math.atan2(velocity.x, velocity.z) * 180.0 / Math.PI),
                    (float)(Math.atan2(velocity.y, Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)) * 180.0 / Math.PI)
            );
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!this.level().isClientSide()) {
            Vec3 pos = this.position();
            this.level().playSound(null, pos.x, pos.y, pos.z,
                    WebSounds.WEB_LAND, SoundSource.NEUTRAL, 0.8F, 1.0F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);

        if (blockHitResult == null) {
            return;
        }

        Level world = this.level();
        if (world != null && !world.isClientSide()) {
            Vec3 pos = this.position();
            world.playSound(null, pos.x, pos.y, pos.z,
                    WebSounds.WEB_LAND, SoundSource.BLOCKS, 0.8F, 1.0F);

            BlockPos hitPos = blockHitResult.getBlockPos();
            Direction hitSide = blockHitResult.getDirection();

            if (hitPos != null && hitSide != null) {
                BlockPos spawnPos = hitPos.relative(hitSide);
                spawnSpiderWebEntityAt(spawnPos);
            }
            this.discard();
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(WebItems.SPIDER_WEB);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.EMPTY;
    }

    private void spawnSpiderWebEntity() {
        BlockPos currentPos = this.blockPosition();
        if (currentPos != null) {
            spawnSpiderWebEntityAt(currentPos);
        }
    }

    private void spawnSpiderWebEntityAt(BlockPos pos) {
        if (pos == null) {
            return;
        }

        Level world = this.level();
        if (world == null) {
            return;
        }

        if (world.getBlockState(pos).canBeReplaced()) {
            SpiderWebEntity spiderWebEntity = new SpiderWebEntity(WebEntities.SPIDER_WEB, world);
            if (spiderWebEntity != null) {
                spiderWebEntity.setPos(pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F);

                spiderWebEntity.setOwner(this.getOwner());

                world.addFreshEntity(spiderWebEntity);
            }
        }
    }
}