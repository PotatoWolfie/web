package potatowolfie.web.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.Web;
import potatowolfie.web.entity.custom.SpiderWebProjectileEntity;
import potatowolfie.web.goals.WebShootingSpiderAttackGoal;
import potatowolfie.web.interfaces.WebSpiderInterface;

import java.lang.reflect.Field;
import java.util.Iterator;

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
    @Unique
    private boolean goalsInitialized = false;

    @Unique
    private static final TagKey<EntityType<?>> WEB_IMMUNE_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE,
            Identifier.of(Web.MOD_ID, "web_immune"));

    @Unique
    private static boolean isWebImmune(LivingEntity entity) {
        return entity.getType().isIn(WEB_IMMUNE_TAG);
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void addWebShootingGoal(CallbackInfo ci) {
        if (goalsInitialized) return;
        goalsInitialized = true;

        SpiderEntity spider = (SpiderEntity) (Object) this;

        try {
            Field goalSelectorField = MobEntity.class.getDeclaredField("goalSelector");
            goalSelectorField.setAccessible(true);
            GoalSelector goalSelector = (GoalSelector) goalSelectorField.get(spider);

            Iterator<PrioritizedGoal> goalIterator = goalSelector.getGoals().iterator();
            while (goalIterator.hasNext()) {
                Goal goal = goalIterator.next().getGoal();
                if (goal instanceof MeleeAttackGoal || goal instanceof PounceAtTargetGoal) {
                    goalIterator.remove();
                }
            }

            goalSelector.add(2, new FleeEntityGoal<>(spider, PlayerEntity.class, 3.5f, 1.0, 1.2));
            goalSelector.add(2, new WebShootingSpiderAttackGoal(spider, this));

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

        SpiderEntity spider = (SpiderEntity) (Object) this;
        LivingEntity currentTarget = spider.getTarget();
        if (currentTarget != null && isWebImmune(currentTarget)) {
            spider.setTarget(null);
        }
    }

    @Override
    @Unique
    public void shootWeb(LivingEntity target) {
        if (isWebImmune(target)) {
            return;
        }

        SpiderEntity spider = (SpiderEntity) (Object) this;
        World world = spider.getWorld();

        SpiderWebProjectileEntity webProjectile = new SpiderWebProjectileEntity(world, spider);
        webProjectile.setPosition(spider.getX(), spider.getEyeY() - 0.1, spider.getZ());

        Vec3d spiderPos = new Vec3d(spider.getX(), spider.getEyeY(), spider.getZ());
        Vec3d targetVelocity = target.getVelocity();

        double distance = spider.distanceTo(target);

        double timeToHit = distance / 1.5;

        Vec3d predictedTargetPos = new Vec3d(
                target.getX() + targetVelocity.x * timeToHit,
                target.getBlockY() + 0.9,
                target.getZ() + targetVelocity.z * timeToHit
        );

        double deltaX = predictedTargetPos.x - spiderPos.x;
        double deltaY = predictedTargetPos.y - spiderPos.y;
        double deltaZ = predictedTargetPos.z - spiderPos.z;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double launchAngle;
        double projectileSpeed = 2.0;

        if (distance <= 6.0) {
            launchAngle = Math.toRadians(10.0 + (distance / 6.0) * 10.0);
        } else if (distance <= 10.0) {
            launchAngle = Math.toRadians(20.0 + ((distance - 6.0) / 4.0) * 15.0);
        } else {
            launchAngle = Math.toRadians(35.0 + Math.min((distance - 10.0) / 6.0, 1.0) * 10.0);
        }

        double gravity = 0.05;

        double sinAngle = Math.sin(launchAngle);
        double cosAngle = Math.cos(launchAngle);

        double discriminant = (sinAngle * sinAngle) - (2.0 * gravity * deltaY / (projectileSpeed * projectileSpeed));
        if (discriminant >= 0) {
            double optimalSpeed = Math.sqrt((gravity * horizontalDistance * horizontalDistance) /
                    (horizontalDistance * Math.sin(2 * launchAngle) + 2 * deltaY * cosAngle * cosAngle));

            if (optimalSpeed > 0.5 && optimalSpeed < 3.0) {
                projectileSpeed = optimalSpeed;
            }
        }

        double horizontalSpeed = projectileSpeed * cosAngle;
        double verticalSpeed = projectileSpeed * sinAngle;

        double horizontalNormalizer = horizontalDistance == 0 ? 0 : 1.0 / horizontalDistance;
        double normalizedX = deltaX * horizontalNormalizer;
        double normalizedZ = deltaZ * horizontalNormalizer;

        double spread = 0.01;
        double randomX = (spider.getRandom().nextDouble() - 0.5) * spread;
        double randomY = (spider.getRandom().nextDouble() - 0.5) * spread * 0.5;
        double randomZ = (spider.getRandom().nextDouble() - 0.5) * spread;

        webProjectile.setVelocity(
                normalizedX * horizontalSpeed + randomX,
                verticalSpeed + randomY,
                normalizedZ * horizontalSpeed + randomZ
        );

        spider.playSound(SoundEvents.ENTITY_SPIDER_AMBIENT, 1.0f, 0.6f + spider.getRandom().nextFloat() * 0.4f);

        world.spawnEntity(webProjectile);
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