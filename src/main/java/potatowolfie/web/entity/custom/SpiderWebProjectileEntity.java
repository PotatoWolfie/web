package potatowolfie.web.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import potatowolfie.web.entity.WebEntities;

public class SpiderWebProjectileEntity extends ThrownItemEntity {

    public SpiderWebProjectileEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public SpiderWebProjectileEntity(World world, LivingEntity owner) {
        super(WebEntities.SPIDER_WEB_FLYING, world);
    }

    public SpiderWebProjectileEntity(World world, double x, double y, double z) {
        super(WebEntities.SPIDER_WEB_FLYING, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);

        // TODO: Logic

        if (!this.getWorld().isClient) {
            this.discard();
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);

        // TODO: Logic

        if (!this.getWorld().isClient) {
            this.discard();
        }
    }
}