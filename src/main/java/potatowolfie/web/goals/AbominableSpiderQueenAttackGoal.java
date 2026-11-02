package potatowolfie.web.goals;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import potatowolfie.web.entity.abominable_spider_queen.AbominableSpiderQueenEntity;
import potatowolfie.web.entity.spider_web.SpiderWebEntity;
import potatowolfie.web.interfaces.WebSpiderInterface;

public class AbominableSpiderQueenAttackGoal extends Goal {
    private enum Phase { WEB_SHOTS, DASH_POSITION, DASH_WINDUP, DASHING, DASH_RECOVERY, CHASE }

    private static final int WEB_SHOTS_PER_CYCLE = 3;
    private static final int WEB_SHOT_GAP_TICKS = 12;
    private static final int WEB_PHASE_TIMEOUT = 240;
    private static final double WEB_MIN_RANGE = 7.0;
    private static final double WEB_MAX_RANGE = 13.0;

    private static final double DASH_MIN_DISTANCE = 8.0;
    private static final double DASH_MAX_DISTANCE = 12.0;
    private static final int DASH_POSITION_TIMEOUT = 100;
    private static final int DASH_WINDUP_TICKS = 16;
    private static final int DASH_MAX_TICKS = 18;
    private static final double DASH_SPEED = 0.9;
    private static final double DASH_KNOCKBACK = 1.8;
    private static final int DASH_RECOVERY_TICKS = 20;

    private static final int CHASE_TICKS = 300;
    private static final int BITE_COOLDOWN_TICKS = 20;

    private final AbominableSpiderQueenEntity queen;
    private final WebSpiderInterface webSpider;

    private Phase phase = Phase.WEB_SHOTS;
    private int phaseTimer;
    private int shotsFired;
    private int shotCooldown;
    private int biteCooldown;
    private int dashTicks;
    private boolean dashHit;
    private Vec3 dashDirection = Vec3.ZERO;

    public AbominableSpiderQueenAttackGoal(AbominableSpiderQueenEntity queen) {
        this.queen = queen;
        this.webSpider = (WebSpiderInterface) queen;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = queen.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        queen.setAggressive(true);
        startWebPhase();
    }

    @Override
    public void stop() {
        queen.setAggressive(false);
        queen.getNavigation().stop();
    }

    private void setPhase(Phase next) {
        this.phase = next;
        this.phaseTimer = 0;
    }

    private void startWebPhase() {
        shotsFired = 0;
        shotCooldown = 10;
        setPhase(Phase.WEB_SHOTS);
    }

    private void startChase() {
        setPhase(Phase.CHASE);
    }

    @Override
    public void tick() {
        LivingEntity target = queen.getTarget();
        if (target == null) return;

        phaseTimer++;
        if (shotCooldown > 0) shotCooldown--;
        if (biteCooldown > 0) biteCooldown--;

        double dist = queen.distanceTo(target);
        boolean canSee = queen.getSensing().hasLineOfSight(target);
        queen.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (phase != Phase.DASHING && phase != Phase.DASH_WINDUP) {
            tryBite(target);
        }

        switch (phase) {
            case WEB_SHOTS -> tickWebShots(target, dist, canSee);
            case DASH_POSITION -> tickDashPosition(target, dist, canSee);
            case DASH_WINDUP -> tickDashWindup(target);
            case DASHING -> tickDashing(target);
            case DASH_RECOVERY -> {
                queen.getNavigation().stop();
                if (phaseTimer >= DASH_RECOVERY_TICKS) startChase();
            }
            case CHASE -> tickChase(target);
        }
    }

    private void tickWebShots(LivingEntity target, double dist, boolean canSee) {
        boolean doneShooting = shotsFired >= WEB_SHOTS_PER_CYCLE && !webSpider.isShootingWeb();
        if (doneShooting || phaseTimer > WEB_PHASE_TIMEOUT || SpiderWebEntity.isEntityTrapped(target)) {
            if (queen.canDash()) {
                setPhase(Phase.DASH_POSITION);
            } else {
                startChase();
            }
            return;
        }

        if (dist < WEB_MIN_RANGE) {
            retreatFrom(target, 1.2);
        } else if (dist > WEB_MAX_RANGE) {
            approach(target, 1.0);
        } else {
            strafe(target);
        }

        if (shotsFired < WEB_SHOTS_PER_CYCLE && canSee && shotCooldown <= 0 && !webSpider.isShootingWeb()
                && dist >= 5.0 && dist <= 20.0) {
            webSpider.shootWeb(target);
            shotsFired++;
            shotCooldown = 20 + WEB_SHOT_GAP_TICKS;
        }
    }

    private void tickDashPosition(LivingEntity target, double dist, boolean canSee) {
        if (phaseTimer > DASH_POSITION_TIMEOUT) {
            startChase();
            return;
        }
        if (dist < DASH_MIN_DISTANCE) {
            retreatFrom(target, 1.3);
        } else if (dist > DASH_MAX_DISTANCE || !canSee) {
            approach(target, 1.2);
        } else {
            queen.getNavigation().stop();
            queen.playSound(SoundEvents.SPIDER_AMBIENT, 2.0F, 0.5F);
            setPhase(Phase.DASH_WINDUP);
        }
    }

    private void tickDashWindup(LivingEntity target) {
        queen.getNavigation().stop();
        if (phaseTimer >= DASH_WINDUP_TICKS) {
            Vec3 flat = target.position().subtract(queen.position()).multiply(1.0, 0.0, 1.0);
            dashDirection = flat.lengthSqr() < 1.0E-4 ? Vec3.ZERO : flat.normalize();
            dashTicks = 0;
            dashHit = false;
            queen.playSound(SoundEvents.SPIDER_HURT, 2.0F, 0.6F);
            setPhase(Phase.DASHING);
        }
    }

    private void tickDashing(LivingEntity target) {
        queen.getNavigation().stop();
        dashTicks++;

        Vec3 motion = queen.getDeltaMovement();
        queen.setDeltaMovement(dashDirection.x * DASH_SPEED, motion.y, dashDirection.z * DASH_SPEED);

        float yaw = (float) (Math.atan2(-dashDirection.x, dashDirection.z) * (180.0 / Math.PI));
        queen.setYRot(yaw);
        queen.setYBodyRot(yaw);
        queen.setYHeadRot(yaw);

        boolean touching = queen.isWithinMeleeAttackRange(target)
                || queen.getBoundingBox().inflate(0.6).intersects(target.getBoundingBox());

        if (!dashHit && touching) {
            dashHit = true;
            if (bite(target)) {
                DamageSource source = queen.damageSources().mobAttack(queen);
                Vec3 dir = target.position().subtract(queen.position()).multiply(1.0, 0.0, 1.0).normalize();
                target.knockback(DASH_KNOCKBACK, -dir.x, -dir.z, source,
                        (float) queen.getAttributeValue(Attributes.ATTACK_DAMAGE));
            }
        }

        if (dashHit || dashTicks >= DASH_MAX_TICKS || queen.horizontalCollision) {
            queen.setDeltaMovement(0.0, queen.getDeltaMovement().y, 0.0);
            setPhase(Phase.DASH_RECOVERY);
        }
    }

    private void tickChase(LivingEntity target) {
        if (phaseTimer >= CHASE_TICKS) {
            startWebPhase();
            return;
        }
        approach(target, 1.25);
    }

    private void tryBite(LivingEntity target) {
        if (biteCooldown <= 0 && queen.isWithinMeleeAttackRange(target)) {
            if (bite(target)) {
                biteCooldown = BITE_COOLDOWN_TICKS;
            }
        }
    }

    private boolean bite(LivingEntity target) {
        return queen.level() instanceof ServerLevel level && queen.doHurtTarget(level, target);
    }

    private void approach(LivingEntity target, double speed) {
        queen.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), speed);
    }

    private void retreatFrom(LivingEntity target, double speed) {
        Vec3 away = queen.position().subtract(target.position()).multiply(1.0, 0.0, 1.0).normalize();
        Vec3 goal = queen.position().add(away.scale(8.0));
        queen.getNavigation().moveTo(goal.x, goal.y, goal.z, speed);
    }

    private void strafe(LivingEntity target) {
        Vec3 toTarget = target.position().subtract(queen.position()).multiply(1.0, 0.0, 1.0).normalize();
        Vec3 side = new Vec3(-toTarget.z, 0.0, toTarget.x);
        double sign = Math.sin((phaseTimer + queen.getId()) * 0.08) > 0 ? 1.0 : -1.0;
        Vec3 goal = queen.position().add(side.scale(3.0 * sign));
        queen.getNavigation().moveTo(goal.x, goal.y, goal.z, 0.9);
    }
}