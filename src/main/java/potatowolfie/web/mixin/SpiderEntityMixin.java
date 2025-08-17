package potatowolfie.web.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.entity.custom.SpiderWebProjectileEntity;
import potatowolfie.web.goals.WebShootingSpiderAttackGoal;
import potatowolfie.web.interfaces.WebSpiderInterface;

import java.lang.reflect.Field;

@Mixin(SpiderEntity.class)
public class SpiderEntityMixin implements WebSpiderInterface {
    @Unique
    private int webCooldown = 0;
    @Unique
    private int combatTimer = 0;
    @Unique
    private boolean hasShootWeb = false;
    @Unique
    private boolean inCombat = false;

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void addWebShootingGoal(CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity) (Object) this;

        try {
            Field goalSelectorField = MobEntity.class.getDeclaredField("goalSelector");
            goalSelectorField.setAccessible(true);
            GoalSelector goalSelector = (GoalSelector) goalSelectorField.get(spider);

            goalSelector.getGoals().removeIf(goal -> goal.getGoal() instanceof MeleeAttackGoal);
            goalSelector.add(4, new WebShootingSpiderAttackGoal(spider, this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (webCooldown > 0) {
            webCooldown--;
        }

        if (inCombat && combatTimer > 0) {
            combatTimer--;
            if (combatTimer <= 0) {
                inCombat = false;
                hasShootWeb = false;
            }
        }
    }

    @Override
    @Unique
    public void shootWeb(LivingEntity target) {
        SpiderEntity spider = (SpiderEntity) (Object) this;
        World world = spider.getWorld();

        SpiderWebProjectileEntity webProjectile = new SpiderWebProjectileEntity(world, spider);
        webProjectile.setPosition(spider.getX(), spider.getEyeY() - 0.1, spider.getZ());

        Vec3d spiderPos = new Vec3d(spider.getX(), spider.getEyeY(), spider.getZ());
        Vec3d targetPos = new Vec3d(target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ());

        double deltaX = targetPos.x - spiderPos.x;
        double deltaY = targetPos.y - spiderPos.y;
        double deltaZ = targetPos.z - spiderPos.z;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double launchAngle = Math.toRadians(15);
        double minSpeed = 1.5;
        double maxSpeed = 2.5;

        double gravity = 0.03;
        double requiredSpeed = Math.sqrt((horizontalDistance * gravity) / Math.sin(2 * launchAngle));

        double speed = Math.max(minSpeed, Math.min(maxSpeed, requiredSpeed));

        if (requiredSpeed > maxSpeed || requiredSpeed < minSpeed) {
            double sinValue = (horizontalDistance * gravity) / (speed * speed);
            if (sinValue <= 1.0) {
                launchAngle = Math.asin(sinValue) / 2;
            }
        }

        double horizontalSpeed = speed * Math.cos(launchAngle);
        double verticalSpeed = speed * Math.sin(launchAngle);

        double timeToTarget = horizontalDistance / horizontalSpeed;
        verticalSpeed += deltaY / timeToTarget;

        double normalizedX = deltaX / horizontalDistance;
        double normalizedZ = deltaZ / horizontalDistance;

        double spread = 0.015;
        double randomX = (spider.getRandom().nextDouble() - 0.5) * spread;
        double randomY = (spider.getRandom().nextDouble() - 0.5) * spread * 0.3;
        double randomZ = (spider.getRandom().nextDouble() - 0.5) * spread;

        webProjectile.setVelocity(
                normalizedX * horizontalSpeed + randomX,
                verticalSpeed + randomY,
                normalizedZ * horizontalSpeed + randomZ
        );

        spider.playSound(SoundEvents.ENTITY_SPIDER_AMBIENT, 1.0f, 0.6f + spider.getRandom().nextFloat() * 0.4f);

        world.spawnEntity(webProjectile);

        notifySpidersOfWebShot(target);
    }

    @Unique
    private void notifySpidersOfWebShot(LivingEntity target) {
        SpiderEntity spider = (SpiderEntity) (Object) this;
        spider.getWorld().getEntitiesByClass(SpiderEntity.class,
                target.getBoundingBox().expand(32),
                otherSpider -> otherSpider.getTarget() == target && otherSpider != spider
        ).forEach(otherSpider -> {
            if (otherSpider instanceof WebSpiderInterface webSpider) {
                webSpider.setInCombat(true);
                webSpider.setCombatTimer(140);
                webSpider.setHasShootWeb(true);
            }
        });
    }

    @Override
    @Unique
    public int getWebCooldown() { return webCooldown; }

    @Override
    @Unique
    public void setWebCooldown(int cooldown) { this.webCooldown = cooldown; }

    @Override
    @Unique
    public boolean hasShootWeb() { return hasShootWeb; }

    @Override
    @Unique
    public void setHasShootWeb(boolean hasShot) { this.hasShootWeb = hasShot; }

    @Override
    @Unique
    public boolean isInCombat() { return inCombat; }

    @Override
    @Unique
    public void setInCombat(boolean combat) { this.inCombat = combat; }

    @Override
    @Unique
    public int getCombatTimer() { return combatTimer; }

    @Override
    @Unique
    public void setCombatTimer(int timer) { this.combatTimer = timer; }
}