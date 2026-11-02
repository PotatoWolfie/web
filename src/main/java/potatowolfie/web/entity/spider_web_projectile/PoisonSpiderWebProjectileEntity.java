package potatowolfie.web.entity.spider_web_projectile;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.spider_web.PoisonSpiderWebEntity;
import potatowolfie.web.entity.spider_web.SpiderWebEntity;
import potatowolfie.web.particle.WebParticles;

public class PoisonSpiderWebProjectileEntity extends SpiderWebProjectileEntity {

    public PoisonSpiderWebProjectileEntity(Level world, LivingEntity owner) {
        super(WebEntities.POISON_SPIDER_WEB_FLYING, world, owner);
    }

    public PoisonSpiderWebProjectileEntity(Level world, double x, double y, double z) {
        super(WebEntities.POISON_SPIDER_WEB_FLYING, world);
        this.setPos(x, y, z);
    }

    public PoisonSpiderWebProjectileEntity(EntityType<? extends PoisonSpiderWebProjectileEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected SpiderWebEntity createWebEntity(Level world) {
        return new PoisonSpiderWebEntity(WebEntities.POISON_SPIDER_WEB, world);
    }

    @Override
    protected SimpleParticleType getTrailParticle() {
        return WebParticles.POISON_WEB_TRAIL;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }
}