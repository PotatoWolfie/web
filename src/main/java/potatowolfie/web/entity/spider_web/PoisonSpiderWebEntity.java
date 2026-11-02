package potatowolfie.web.entity.spider_web;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class PoisonSpiderWebEntity extends SpiderWebEntity {

    private static final int POISON_DURATION_TICKS = 5 * 20; // 5 seconds
    private static final int POISON_REFRESH_AT_OR_BELOW = 76;

    public PoisonSpiderWebEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    protected void applyWebEffects() {
        List<LivingEntity> inside = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(0.1),
                entity -> entity.isAlive() && !isWebImmune(entity)
        );

        for (LivingEntity entity : inside) {
            MobEffectInstance current = entity.getEffect(MobEffects.POISON);
            if (current == null || current.getDuration() <= POISON_REFRESH_AT_OR_BELOW) {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION_TICKS, 0));
            }
        }
    }
}