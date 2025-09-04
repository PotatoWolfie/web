package potatowolfie.web.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.item.WebItems;
import potatowolfie.web.sound.WebSounds;

public class SpiderWebProjectileEntity extends PersistentProjectileEntity {

    public SpiderWebProjectileEntity(World world, LivingEntity owner) {
        super(WebEntities.SPIDER_WEB_FLYING, world);
        if (owner != null) {
            this.setOwner(owner);
            this.setPosition(owner.getX(), owner.getEyeY() - 0.3, owner.getZ());

            if (!world.isClient()) {
                world.playSound(null, owner.getX(), owner.getEyeY(), owner.getZ(),
                        WebSounds.WEB_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    public SpiderWebProjectileEntity(World world, double x, double y, double z) {
        super(WebEntities.SPIDER_WEB_FLYING, world);
        this.setPosition(x, y, z);
    }

    public SpiderWebProjectileEntity(EntityType<? extends SpiderWebProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        Vec3d velocity = this.getVelocity();
        if (velocity != null && velocity.lengthSquared() > 0.0001) {
            this.setRotation(
                    (float)(Math.atan2(velocity.x, velocity.z) * 180.0 / Math.PI),
                    (float)(Math.atan2(velocity.y, Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)) * 180.0 / Math.PI)
            );
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!this.getWorld().isClient()) {
            Vec3d pos = this.getPos();
            this.getWorld().playSound(null, pos.x, pos.y, pos.z,
                    WebSounds.WEB_LAND, SoundCategory.NEUTRAL, 0.8F, 1.0F);
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);

        if (blockHitResult == null) {
            return;
        }

        World world = this.getWorld();
        if (world != null && !world.isClient) {
            Vec3d pos = this.getPos();
            world.playSound(null, pos.x, pos.y, pos.z,
                    WebSounds.WEB_LAND, SoundCategory.BLOCKS, 0.8F, 1.0F);

            BlockPos hitPos = blockHitResult.getBlockPos();
            Direction hitSide = blockHitResult.getSide();

            if (hitPos != null && hitSide != null) {
                BlockPos spawnPos = hitPos.offset(hitSide);
                spawnSpiderWebEntityAt(spawnPos);
            }
            this.discard();
        }
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(WebItems.SPIDER_WEB);
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.INTENTIONALLY_EMPTY;
    }

    private void spawnSpiderWebEntity() {
        BlockPos currentPos = this.getBlockPos();
        if (currentPos != null) {
            spawnSpiderWebEntityAt(currentPos);
        }
    }

    private void spawnSpiderWebEntityAt(BlockPos pos) {
        if (pos == null) {
            return;
        }

        World world = this.getWorld();
        if (world == null) {
            return;
        }

        if (world.getBlockState(pos).isReplaceable()) {
            SpiderWebEntity spiderWebEntity = new SpiderWebEntity(WebEntities.SPIDER_WEB, world);
            if (spiderWebEntity != null) {
                spiderWebEntity.setPosition(pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F);
                world.spawnEntity(spiderWebEntity);
            }
        }
    }
}