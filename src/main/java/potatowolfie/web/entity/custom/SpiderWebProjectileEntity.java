package potatowolfie.web.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.item.WebItems;

public class SpiderWebProjectileEntity extends PersistentProjectileEntity {

    public SpiderWebProjectileEntity(World world, LivingEntity owner) {
        super(WebEntities.SPIDER_WEB_FLYING, world);
        if (owner != null) {
            this.setOwner(owner);
            this.setPosition(owner.getX(), owner.getEyeY() - 0.3, owner.getZ());
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
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);

        if (blockHitResult == null) {
            return;
        }

        World world = this.getWorld();
        if (world != null && !world.isClient) {
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