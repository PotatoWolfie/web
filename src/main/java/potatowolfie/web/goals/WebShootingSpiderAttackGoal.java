package potatowolfie.web.goals;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import potatowolfie.web.Web;
import potatowolfie.web.entity.custom.SpiderWebEntity;
import potatowolfie.web.interfaces.WebSpiderInterface;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class WebShootingSpiderAttackGoal extends Goal {
    private final SpiderEntity spider;
    private final WebSpiderInterface webSpider;
    private final double baseSpeed;
    private LivingEntity target;
    private int attackCooldown = 0;

    private static final TagKey<EntityType<?>> WEB_IMMUNE_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE,
            Identifier.of(Web.MOD_ID, "web_immune"));

    private SpiderState currentState = SpiderState.POSITIONING;
    private int stateTimer = 0;
    private int targetLostTimer = 0;
    private int postWebChaseTimer = 0;
    private boolean hasWebSuccessfullyHit = false;
    private SpiderRole assignedRole = SpiderRole.WEBBER;
    private int roleAssignmentTimer = 0;
    private List<SpiderEntity> lastKnownGroup = new ArrayList<>();

    private int webAttempts = 0;
    private int maxWebAttempts = 3;
    private boolean isSoloChaseMode = false;
    private int soloChaseTimer = 0;
    private int maxSoloChaseTime = 300;

    public enum SpiderState {
        POSITIONING,
        SHOOTING,
        CHARGING,
        ATTACKING,
        COOLDOWN,
        POST_WEB_CHASE,
        RETREATING
    }

    public enum SpiderRole {
        WEBBER,
        CHASER,
        FLEXIBLE,
        BACKUP_WEBBER,
        BACKUP_CHASER
    }

    private static boolean isWebImmune(LivingEntity entity) {
        return entity.getType().isIn(WEB_IMMUNE_TAG);
    }

    public WebShootingSpiderAttackGoal(SpiderEntity spider, WebSpiderInterface webSpider) {
        this.spider = spider;
        this.webSpider = webSpider;
        this.baseSpeed = 1.0;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = this.spider.getTarget();
        if (target == null || !target.isAlive() || isWebImmune(target)) {
            return false;
        }

        this.target = target;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = this.spider.getTarget();
        if (target == null || !target.isAlive() || isWebImmune(target)) {
            return false;
        }

        double distance = this.spider.squaredDistanceTo(target);
        if (distance > 1024.0) {
            return false;
        }

        boolean targetTrapped = isTargetTouchingWeb(target);
        if (!targetTrapped) {
            targetLostTimer++;
            if (targetLostTimer > 60) {
                return false;
            }
        } else {
            targetLostTimer = 0;
        }

        return true;
    }

    @Override
    public void stop() {
        this.target = null;
        this.attackCooldown = 0;
        this.currentState = SpiderState.POSITIONING;
        this.stateTimer = 0;
        this.targetLostTimer = 0;
        this.postWebChaseTimer = 0;
        this.hasWebSuccessfullyHit = false;
        this.webAttempts = 0;
        this.isSoloChaseMode = false;
        this.soloChaseTimer = 0;
        webSpider.setHasShootWeb(false);
        this.spider.setTarget(null);
    }

    private List<SpiderEntity> getNearbySpiders() {
        Box searchBox = Box.of(this.spider.getEntityPos(), 64, 32, 64);
        return this.spider.getEntityWorld().getEntitiesByClass(
                SpiderEntity.class,
                searchBox,
                spider -> spider.isAlive() && spider != this.spider && spider.getTarget() != null
        );
    }

    private void assignRoles() {
        roleAssignmentTimer++;
        if (roleAssignmentTimer < 20) return;
        roleAssignmentTimer = 0;

        List<SpiderEntity> nearbySpiders = getNearbySpiders();
        int groupSize = nearbySpiders.size() + 1;

        nearbySpiders.add(this.spider);
        nearbySpiders.sort((a, b) -> Integer.compare(a.getId(), b.getId()));

        int thisSpiderIndex = nearbySpiders.indexOf(this.spider);

        switch (groupSize) {
            case 1:
                assignedRole = SpiderRole.WEBBER;
                break;
            case 2:
                if (thisSpiderIndex == 0) {
                    assignedRole = SpiderRole.WEBBER;
                } else {
                    assignedRole = SpiderRole.CHASER;
                }
                break;
            case 3:
                if (thisSpiderIndex == 0) {
                    assignedRole = SpiderRole.WEBBER;
                } else if (thisSpiderIndex == 1) {
                    assignedRole = SpiderRole.CHASER;
                } else {
                    assignedRole = SpiderRole.FLEXIBLE;
                }
                break;
            default:
                int webberCount = (groupSize + 1) / 2;
                int chaserCount = groupSize - webberCount;

                if (thisSpiderIndex < webberCount) {
                    assignedRole = thisSpiderIndex % 2 == 0 ? SpiderRole.WEBBER : SpiderRole.BACKUP_WEBBER;
                } else {
                    int chaserIndex = thisSpiderIndex - webberCount;
                    assignedRole = chaserIndex % 2 == 0 ? SpiderRole.CHASER : SpiderRole.BACKUP_CHASER;
                }
                break;
        }

        lastKnownGroup = new ArrayList<>(nearbySpiders);
    }

    private boolean shouldUseWebBehavior() {
        if (lastKnownGroup.size() == 1) {
            return !isSoloChaseMode;
        }

        switch (assignedRole) {
            case WEBBER:
            case BACKUP_WEBBER:
                return true;
            case CHASER:
            case BACKUP_CHASER:
                return false;
            case FLEXIBLE:
                if (hasWebSuccessfullyHit) {
                    return this.spider.getRandom().nextFloat() < 0.3f;
                } else {
                    return this.spider.getRandom().nextFloat() < 0.7f;
                }
            default:
                return true;
        }
    }

    private double getMovementSpeed(LivingEntity target) {
        return (assignedRole == SpiderRole.CHASER || assignedRole == SpiderRole.BACKUP_CHASER)
                ? baseSpeed * 1.3 : baseSpeed;
    }

    private double getChargingSpeed(LivingEntity target) {
        return (assignedRole == SpiderRole.CHASER || assignedRole == SpiderRole.BACKUP_CHASER)
                ? baseSpeed * 1.5 : baseSpeed;
    }

    private boolean isTargetTouchingWeb(LivingEntity target) {
        return SpiderWebEntity.isEntityTrapped(target) && !isWebImmune(target);
    }

    @Override
    public void tick() {
        LivingEntity target = this.spider.getTarget();
        if (target == null || isWebImmune(target)) return;

        assignRoles();

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        if (postWebChaseTimer > 0) {
            postWebChaseTimer--;
        }

        if (lastKnownGroup.size() == 1) {
            if (isSoloChaseMode) {
                soloChaseTimer++;
                if (soloChaseTimer >= maxSoloChaseTime) {
                    isSoloChaseMode = false;
                    soloChaseTimer = 0;
                    webAttempts = 0;
                }
            }
        } else {
            isSoloChaseMode = false;
            soloChaseTimer = 0;
            webAttempts = 0;
        }

        stateTimer++;
        double actualDistance = this.spider.distanceTo(target);
        boolean canSeeTarget = this.spider.getVisibilityCache().canSee(target);
        boolean targetIsTouchingWeb = isTargetTouchingWeb(target);

        if (assignedRole == SpiderRole.CHASER || assignedRole == SpiderRole.BACKUP_CHASER ||
                (lastKnownGroup.size() == 1 && isSoloChaseMode)) {
            handleChaserBehavior(target, actualDistance, targetIsTouchingWeb);
            return;
        }

        if (currentState == SpiderState.POST_WEB_CHASE) {
            handlePostWebChase(target, actualDistance, targetIsTouchingWeb);
            return;
        }

        if (targetIsTouchingWeb && !hasWebSuccessfullyHit && shouldUseWebBehavior()) {
            hasWebSuccessfullyHit = true;
            postWebChaseTimer = 40 + this.spider.getRandom().nextInt(40);
            transitionToState(SpiderState.POST_WEB_CHASE);
            return;
        }

        if (!targetIsTouchingWeb) {
            hasWebSuccessfullyHit = false;
        }

        if (!targetIsTouchingWeb && (currentState == SpiderState.CHARGING || currentState == SpiderState.ATTACKING)) {
            if (shouldUseWebBehavior()) {
                transitionToState(SpiderState.POSITIONING);
            } else if (assignedRole == SpiderRole.WEBBER && lastKnownGroup.size() == 3) {
                transitionToState(SpiderState.RETREATING);
            } else {
                transitionToState(SpiderState.POSITIONING);
            }
        }

        switch (currentState) {
            case POSITIONING:
                handlePositioning(target, actualDistance, canSeeTarget, targetIsTouchingWeb);
                break;
            case SHOOTING:
                handleShooting(target, actualDistance, canSeeTarget, targetIsTouchingWeb);
                break;
            case CHARGING:
                handleCharging(target, actualDistance, targetIsTouchingWeb);
                break;
            case ATTACKING:
                handleAttacking(target, actualDistance, targetIsTouchingWeb);
                break;
            case COOLDOWN:
                handleCooldown(target, actualDistance, targetIsTouchingWeb);
                break;
            case RETREATING:
                handleRetreating(target, actualDistance, targetIsTouchingWeb);
                break;
        }
    }

    private void handleChaserBehavior(LivingEntity target, double distance, boolean targetIsTouchingWeb) {
        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);
        double currentSpeed = getChargingSpeed(target);

        this.spider.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), currentSpeed);

        if (distance <= 3.5) {
            if (attackCooldown <= 0) {
                float damage = 4.0f;
                boolean damageDealt = target.damage((ServerWorld) this.spider.getEntityWorld(),
                        this.spider.getDamageSources().mobAttack(this.spider), damage);

                if (damageDealt) {
                    Vec3d targetPos = target.getEntityPos();
                    Vec3d spiderPos = this.spider.getEntityPos();
                    Vec3d direction = targetPos.subtract(spiderPos).normalize();

                    double knockbackStrength = 0.5;
                    target.takeKnockback(knockbackStrength, -direction.x, -direction.z);

                    this.spider.playSound(net.minecraft.sound.SoundEvents.ENTITY_SPIDER_HURT, 1.0f, 1.2f);
                    attackCooldown = 15;
                }
            }
        }
    }

    private void handlePostWebChase(LivingEntity target, double distance, boolean targetIsTouchingWeb) {
        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);
        double currentSpeed = getChargingSpeed(target);

        this.spider.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), currentSpeed);

        if (distance <= 3.5 && attackCooldown <= 0) {
            float damage = 4.0f;
            boolean damageDealt = target.damage((ServerWorld) this.spider.getEntityWorld(),
                    this.spider.getDamageSources().mobAttack(this.spider), damage);

            if (damageDealt) {
                Vec3d targetPos = target.getEntityPos();
                Vec3d spiderPos = this.spider.getEntityPos();
                Vec3d direction = targetPos.subtract(spiderPos).normalize();

                double knockbackStrength = 0.4;
                target.takeKnockback(knockbackStrength, -direction.x, -direction.z);

                this.spider.playSound(net.minecraft.sound.SoundEvents.ENTITY_SPIDER_HURT, 1.0f, 1.2f);
                attackCooldown = 12;
            }
        }

        if (postWebChaseTimer <= 0) {
            transitionToState(SpiderState.POSITIONING);
        }
    }

    private void handleRetreating(LivingEntity target, double distance, boolean targetIsTouchingWeb) {
        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);

        if (targetIsTouchingWeb) {
            transitionToState(SpiderState.CHARGING);
            return;
        }

        Vec3d spiderPos = this.spider.getEntityPos();
        Vec3d targetPos = target.getEntityPos();
        Vec3d awayDirection = spiderPos.subtract(targetPos).normalize();
        Vec3d retreatPos = spiderPos.add(awayDirection.multiply(8.0));

        this.spider.getNavigation().startMovingTo(retreatPos.x, retreatPos.y, retreatPos.z, baseSpeed);

        if (stateTimer > 60 || distance > 12.0) {
            transitionToState(SpiderState.POSITIONING);
        }
    }

    private void handlePositioning(LivingEntity target, double distance, boolean canSeeTarget, boolean targetIsTouchingWeb) {
        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);
        double currentSpeed = getMovementSpeed(target);

        if (targetIsTouchingWeb) {
            transitionToState(SpiderState.CHARGING);
            return;
        }

        if (distance < 5.0) {
            Vec3d spiderPos = this.spider.getEntityPos();
            Vec3d targetPos = target.getEntityPos();
            Vec3d awayDirection = spiderPos.subtract(targetPos).normalize();

            Vec3d strafeDirection = new Vec3d(-awayDirection.z, 0, awayDirection.x);
            if ((stateTimer + this.spider.getId()) % 20 > 10) {
                strafeDirection = strafeDirection.multiply(-1);
            }

            Vec3d evasiveDirection = awayDirection.multiply(2.0).add(strafeDirection.multiply(1.5));
            Vec3d retreatPos = spiderPos.add(evasiveDirection.multiply(3.0));

            this.spider.getNavigation().startMovingTo(retreatPos.x, retreatPos.y, retreatPos.z, currentSpeed);
        } else if (distance >= 8.0) {
            Vec3d spiderPos = this.spider.getEntityPos();
            Vec3d targetPos = target.getEntityPos();
            Vec3d directionToTarget = targetPos.subtract(spiderPos).normalize();

            Vec3d strafeDirection = new Vec3d(-directionToTarget.z, 0, directionToTarget.x);
            double weavePhase = (stateTimer + this.spider.getId()) * 0.15;
            Vec3d weaveDirection = strafeDirection.multiply(Math.sin(weavePhase) * 1.5);

            Vec3d approachDirection = directionToTarget.multiply(2.0).add(weaveDirection);
            Vec3d approachPos = spiderPos.add(approachDirection.multiply(2.0));

            this.spider.getNavigation().startMovingTo(approachPos.x, approachPos.y, approachPos.z, currentSpeed);
        } else if (canSeeTarget && distance >= 5.0 && distance <= 10.0 && shouldUseWebBehavior()) {
            transitionToState(SpiderState.SHOOTING);
        }

        if (stateTimer > 20 && shouldUseWebBehavior()) {
            transitionToState(SpiderState.SHOOTING);
        }
    }

    private void handleShooting(LivingEntity target, double distance, boolean canSeeTarget, boolean targetIsTouchingWeb) {
        if (!shouldUseWebBehavior()) {
            handleChaserBehavior(target, distance, targetIsTouchingWeb);
            return;
        }

        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);
        double currentSpeed = getMovementSpeed(target);

        if (targetIsTouchingWeb) {
            transitionToState(SpiderState.CHARGING);
            return;
        }

        if (distance < 5.0) {
            transitionToState(SpiderState.POSITIONING);
            return;
        }

        if (distance >= 5.0 && distance <= 15.0) {
            Vec3d spiderPos = this.spider.getEntityPos();
            Vec3d targetPos = target.getEntityPos();
            Vec3d directionToTarget = targetPos.subtract(spiderPos).normalize();

            double optimalRange = 8.0;
            double currentRange = spiderPos.distanceTo(targetPos);

            Vec3d strafeDirection = new Vec3d(-directionToTarget.z, 0, directionToTarget.x);
            double strafePhase = (stateTimer + this.spider.getId()) * 0.1;
            if (Math.sin(strafePhase) > 0) {
                strafeDirection = strafeDirection.multiply(-1);
            }

            Vec3d rangeAdjustment = Vec3d.ZERO;
            if (currentRange < optimalRange - 1.0) {
                rangeAdjustment = directionToTarget.multiply(-2.0);
            } else if (currentRange > optimalRange + 2.0) {
                rangeAdjustment = directionToTarget.multiply(1.5);
            }

            Vec3d moveDirection = strafeDirection.multiply(3.0).add(rangeAdjustment);
            Vec3d moveTarget = spiderPos.add(moveDirection);

            this.spider.getNavigation().startMovingTo(moveTarget.x, moveTarget.y, moveTarget.z, currentSpeed);
        }

        if (canSeeTarget && distance >= 5.0 && distance < 15.0 && stateTimer >= 5) {
            List<SpiderEntity> nearbySpiders = getNearbySpiders();
            int cooldown = nearbySpiders.size() > 5 ? 20 : 10;

            webSpider.shootWeb(target);
            webSpider.setHasShootWeb(true);
            webSpider.setWebCooldown(cooldown);

            if (lastKnownGroup.size() == 1) {
                webAttempts++;
                if (webAttempts >= maxWebAttempts) {
                    isSoloChaseMode = true;
                    soloChaseTimer = 0;
                    webAttempts = 0;
                }
            }
        }
    }

    private void handleCharging(LivingEntity target, double distance, boolean targetIsTouchingWeb) {
        if (!targetIsTouchingWeb) {
            if (shouldUseWebBehavior()) {
                transitionToState(SpiderState.POSITIONING);
            } else if (assignedRole == SpiderRole.WEBBER && lastKnownGroup.size() == 3) {
                transitionToState(SpiderState.RETREATING);
            } else {
                transitionToState(SpiderState.POSITIONING);
            }
            return;
        }

        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);
        double chargingSpeed = getChargingSpeed(target);

        this.spider.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), chargingSpeed);

        double attackDistance = targetIsTouchingWeb ? 4.5 : 4.0;
        int maxChargeTime = targetIsTouchingWeb ? 40 : 30;

        if (distance <= attackDistance || stateTimer > maxChargeTime) {
            transitionToState(SpiderState.ATTACKING);
        }
    }

    private void handleAttacking(LivingEntity target, double distance, boolean targetIsTouchingWeb) {
        if (!targetIsTouchingWeb) {
            if (shouldUseWebBehavior()) {
                transitionToState(SpiderState.POSITIONING);
            } else if (assignedRole == SpiderRole.WEBBER && lastKnownGroup.size() == 3) {
                transitionToState(SpiderState.RETREATING);
            } else {
                transitionToState(SpiderState.POSITIONING);
            }
            return;
        }

        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);

        if (this.spider.getVelocity().y > 0.1) {
            this.spider.setVelocity(this.spider.getVelocity().x, -0.1, this.spider.getVelocity().z);
        }

        double attackRange = targetIsTouchingWeb ? 4.0 : 3.0;
        double currentSpeed = getMovementSpeed(target);

        if (distance <= attackRange) {
            this.spider.getNavigation().stop();

            Vec3d targetPos = target.getEntityPos();
            Vec3d spiderPos = this.spider.getEntityPos();
            Vec3d direction = targetPos.subtract(spiderPos).normalize();

            double yaw = Math.atan2(-direction.x, direction.z) * (180.0 / Math.PI);
            this.spider.setYaw((float) yaw);
            this.spider.setBodyYaw((float) yaw);
            this.spider.setHeadYaw((float) yaw);

            if (attackCooldown <= 0) {
                float damage = 4.0f;
                boolean damageDealt = target.damage((ServerWorld) this.spider.getEntityWorld(),
                        this.spider.getDamageSources().mobAttack(this.spider), damage);

                if (damageDealt) {
                    double knockbackStrength = targetIsTouchingWeb ? 0.6 : 0.4;
                    target.takeKnockback(knockbackStrength, -direction.x, -direction.z);

                    this.spider.playSound(net.minecraft.sound.SoundEvents.ENTITY_SPIDER_HURT, 1.0f, 1.2f);

                    attackCooldown = targetIsTouchingWeb ? 8 : 12;
                }
            }
        } else if (distance > attackRange && distance <= 8.0) {
            this.spider.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), currentSpeed);
        }

        int maxAttackTime = targetIsTouchingWeb ? 100 : 80;
        if (stateTimer >= maxAttackTime) {
            transitionToState(SpiderState.COOLDOWN);
        }
    }

    private void handleCooldown(LivingEntity target, double distance, boolean targetIsTouchingWeb) {
        this.spider.getLookControl().lookAt(target, 30.0f, 30.0f);
        double currentSpeed = getMovementSpeed(target);

        if (targetIsTouchingWeb) {
            transitionToState(SpiderState.CHARGING);
            return;
        }

        if (distance >= 8.0 && shouldUseWebBehavior()) {
            this.spider.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), currentSpeed);
        } else {
            this.spider.getNavigation().stop();
        }

        if (stateTimer >= 40) {
            transitionToState(SpiderState.POSITIONING);
        }
    }

    private void transitionToState(SpiderState newState) {
        this.currentState = newState;
        this.stateTimer = 0;
    }
}