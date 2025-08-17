package potatowolfie.web.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import potatowolfie.web.interfaces.WebSpiderInterface;

import java.util.EnumSet;

public class WebShootingSpiderAttackGoal extends Goal {
    private final SpiderEntity spider;
    private final WebSpiderInterface webSpider;
    private final double speed;
    private final boolean pauseWhenMobIdle;
    private LivingEntity target;
    private int attackTime = -1;
    private int attackCooldown = 0;

    private SpiderState currentState = SpiderState.POSITIONING;
    private int stateTimer = 0;
    private boolean hasCompletedCycle = false;

    public enum SpiderState {
        POSITIONING,
        SHOOTING,
        CHARGING,
        ATTACKING,
        FLEEING,
        COOLDOWN
    }

    public WebShootingSpiderAttackGoal(SpiderEntity spider, WebSpiderInterface webSpider) {
        this.spider = spider;
        this.webSpider = webSpider;
        this.speed = 1.2;
        this.pauseWhenMobIdle = true;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = this.spider.getTarget();
        if (target == null) {
            return false;
        } else if (!target.isAlive()) {
            return false;
        } else {
            this.target = target;
            return true;
        }
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = this.spider.getTarget();
        if (target == null) {
            return false;
        } else if (!target.isAlive()) {
            return false;
        } else if (this.spider.squaredDistanceTo(target) > 1024.0) { // 32 blocks
            return false;
        } else {
            return !(this.pauseWhenMobIdle && this.spider.getNavigation().isIdle());
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.attackTime = -1;
        this.attackCooldown = 0;
        this.currentState = SpiderState.POSITIONING;
        this.stateTimer = 0;
        this.hasCompletedCycle = false;
        webSpider.setInCombat(false);
        webSpider.setHasShootWeb(false);
        webSpider.setCombatTimer(0);
    }

    @Override
    public void tick() {
        LivingEntity target = this.spider.getTarget();
        if (target == null) return;

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        stateTimer++;
        double actualDistance = this.spider.distanceTo(target);
        boolean canSeeTarget = this.spider.getVisibilityCache().canSee(target);

        switch (currentState) {
            case POSITIONING:
                handlePositioning(target, actualDistance, canSeeTarget);
                break;
            case SHOOTING:
                handleShooting(target, actualDistance, canSeeTarget);
                break;
            case CHARGING:
                handleCharging(target, actualDistance);
                break;
            case ATTACKING:
                handleAttacking(target, actualDistance);
                break;
            case FLEEING:
                handleFleeing(target, actualDistance);
                break;
            case COOLDOWN:
                handleCooldown(target, actualDistance);
                break;
        }
    }

    private void handlePositioning(LivingEntity target, double distance, boolean canSeeTarget) {
        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);

        if (distance >= 8.0 && distance <= 16.0 && canSeeTarget) {
            transitionToState(SpiderState.SHOOTING);
        } else if (distance < 8.0) {
            Vec3d direction = this.spider.getPos().subtract(target.getPos()).normalize();
            this.spider.getNavigation().startMovingTo(
                    this.spider.getX() + direction.x * 12,
                    this.spider.getY(),
                    this.spider.getZ() + direction.z * 12,
                    this.speed
            );
        } else if (distance > 16.0) {
            this.spider.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), this.speed);
        }

        if (stateTimer > 20) {
            transitionToState(SpiderState.SHOOTING);
        }
    }

    private void handleShooting(LivingEntity target, double distance, boolean canSeeTarget) {
        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);
        this.spider.getNavigation().stop();

        if (canSeeTarget && distance <= 20.0) {
            webSpider.shootWeb(target);
            webSpider.setHasShootWeb(true);
            webSpider.setInCombat(true);

            transitionToState(SpiderState.CHARGING);
        } else if (stateTimer > 5) {
            transitionToState(SpiderState.POSITIONING);
        }
    }

    private void handleCharging(LivingEntity target, double distance) {
        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);

        this.spider.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), this.speed * 2.8);

        if (distance <= 4.0 || stateTimer > 40) {
            transitionToState(SpiderState.ATTACKING);
        }
    }

    private void handleAttacking(LivingEntity target, double distance) {
        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);

        if (this.spider.getVelocity().y > 0.1) {
            this.spider.setVelocity(this.spider.getVelocity().x, -0.1, this.spider.getVelocity().z);
        }

        if (distance <= 4.0) {
            this.spider.getNavigation().stop();

            Vec3d targetPos = target.getPos();
            Vec3d spiderPos = this.spider.getPos();
            Vec3d direction = targetPos.subtract(spiderPos).normalize();

            double yaw = Math.atan2(-direction.x, direction.z) * (180.0 / Math.PI);
            this.spider.setYaw((float) yaw);
            this.spider.setBodyYaw((float) yaw);
            this.spider.setHeadYaw((float) yaw);

            if (attackCooldown <= 0) {
                float damage = 4.0f;
                target.damage((ServerWorld) this.spider.getWorld(), this.spider.getDamageSources().mobAttack(this.spider), damage);

                if (target instanceof LivingEntity livingTarget) {
                    livingTarget.takeKnockback(0.4, -direction.x, -direction.z);
                }

                this.spider.playSound(net.minecraft.sound.SoundEvents.ENTITY_SPIDER_HURT, 1.0f, 1.2f);
                attackCooldown = 8;
            }
        } else {
            this.spider.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), this.speed * 2.5);
        }

        if (stateTimer >= 140) {
            transitionToState(SpiderState.FLEEING);
        }
    }

    private void handleFleeing(LivingEntity target, double distance) {
        Vec3d direction = this.spider.getPos().subtract(target.getPos()).normalize();
        this.spider.getNavigation().startMovingTo(
                this.spider.getX() + direction.x * 16,
                this.spider.getY(),
                this.spider.getZ() + direction.z * 16,
                this.speed * 1.8
        );

        if (distance > 20.0 || stateTimer > 60) {
            transitionToState(SpiderState.COOLDOWN);
        }
    }

    private void handleCooldown(LivingEntity target, double distance) {
        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);

        if (distance < 10.0) {
            Vec3d direction = this.spider.getPos().subtract(target.getPos()).normalize();
            this.spider.getNavigation().startMovingTo(
                    this.spider.getX() + direction.x * 8,
                    this.spider.getY(),
                    this.spider.getZ() + direction.z * 8,
                    this.speed * 0.7
            );
        }

        if (stateTimer >= 160) {
            transitionToState(SpiderState.POSITIONING);
            hasCompletedCycle = true;
        }
    }

    private void transitionToState(SpiderState newState) {
        this.currentState = newState;
        this.stateTimer = 0;

        if (newState == SpiderState.POSITIONING || newState == SpiderState.COOLDOWN) {
            webSpider.setInCombat(false);
        }
    }
}