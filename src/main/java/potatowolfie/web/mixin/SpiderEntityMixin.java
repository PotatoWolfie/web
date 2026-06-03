package potatowolfie.web.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import potatowolfie.web.Web;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.custom.BabySpiderEntity;
import potatowolfie.web.entity.custom.SpiderWebProjectileEntity;
import potatowolfie.web.enums.SpiderState;
import potatowolfie.web.goals.WebShootingSpiderAttackGoal;
import potatowolfie.web.interfaces.SpiderAnimationInterface;
import potatowolfie.web.interfaces.WebSpiderInterface;

import java.lang.reflect.Field;
import java.util.Iterator;

@Mixin(Spider.class)
public class SpiderEntityMixin implements WebSpiderInterface, SpiderAnimationInterface {

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
    private int stateTimer = 0;
    @Unique
    private LivingEntity shootTarget = null;

    @Unique
    public AnimationState spiderIdleAnimationState;
    @Unique
    public AnimationState spiderWalkingAnimationState;
    @Unique
    public AnimationState spiderShootingAnimationState;

    @Unique
    private int spiderIdleAnimationTimeout = 0;
    @Unique
    private boolean isSpiderIdleAnimationRunning = false;
    @Unique
    private boolean isSpiderWalkingAnimationRunning = false;
    @Unique
    private boolean isSpiderShootingAnimationRunning = false;
    @Unique
    private boolean isSpiderChangingState = false;
    @Unique
    private boolean spiderAnimationStartedThisTick = false;

    @Unique
    private static final EntityDataAccessor<Integer> SPIDER_DATA_ID_STATE =
            SynchedEntityData.defineId(Spider.class, EntityDataSerializers.INT);

    @Unique
    private SpiderState spiderState = SpiderState.IDLE;
    @Unique
    private SpiderState previousSpiderState = SpiderState.IDLE;

    @Unique
    private static final TagKey<EntityType<?>> WEB_IMMUNE_TAG = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "web_immune"));

    @Unique
    private static boolean isWebImmune(LivingEntity entity) {
        return entity.is(WEB_IMMUNE_TAG);
    }

    @Unique
    private boolean isCaveSpider() {
        return (Object) this instanceof CaveSpider;
    }

    @Unique
    private static Field getGoalSelectorField() throws NoSuchFieldException {
        NoSuchFieldException lastException = null;

        String[] possibleNames = {
                "goalSelector",
                "field_6201",
                "bO"
        };

        for (String name : possibleNames) {
            try {
                return Mob.class.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                lastException = e;
            }
        }

        throw new NoSuchFieldException("Could not find goalSelector field with any of the tried names: " + String.join(", ", possibleNames));
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initSpiderAnimationStates(EntityType<? extends Spider> entityType, Level level, CallbackInfo ci) {
        ensureAnimationStatesInitialized();
        if (this.spiderState == null) {
            this.spiderState = SpiderState.IDLE;
        }
        if (this.previousSpiderState == null) {
            this.previousSpiderState = SpiderState.IDLE;
        }
    }

    @Unique
    private void ensureAnimationStatesInitialized() {
        if (this.spiderIdleAnimationState == null) {
            this.spiderIdleAnimationState = new AnimationState();
        }
        if (this.spiderWalkingAnimationState == null) {
            this.spiderWalkingAnimationState = new AnimationState();
        }
        if (this.spiderShootingAnimationState == null) {
            this.spiderShootingAnimationState = new AnimationState();
        }
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void addSpiderAnimationDataTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        Spider spider = (Spider) (Object) this;

        if (!(spider instanceof CaveSpider)) {
            builder.define(SPIDER_DATA_ID_STATE, SpiderState.IDLE.ordinal());
        }
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addWebShootingGoal(CallbackInfo ci) {
        if (goalsInitialized) return;
        goalsInitialized = true;

        if (isCaveSpider()) return;

        Spider spider = (Spider) (Object) this;

        try {
            Field goalSelectorField = getGoalSelectorField();
            goalSelectorField.setAccessible(true);
            GoalSelector goalSelector = (GoalSelector) goalSelectorField.get(spider);

            Iterator<WrappedGoal> goalIterator = goalSelector.getAvailableGoals().iterator();
            while (goalIterator.hasNext()) {
                Goal goal = goalIterator.next().getGoal();
                if (goal instanceof MeleeAttackGoal || goal instanceof LeapAtTargetGoal) {
                    goalIterator.remove();
                }
            }

            goalSelector.addGoal(2, new AvoidEntityGoal<>(spider, Player.class, 3.5f, 1.0, 1.2));
            goalSelector.addGoal(2, new WebShootingSpiderAttackGoal(spider, this));

        } catch (Exception e) {
            Web.LOGGER.error("Failed to initialize spider goals: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void spawnWithBabyRider(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (isCaveSpider()) return;

        if (spawnReason == EntitySpawnReason.TRIAL_SPAWNER) return;

        Spider spider = (Spider) (Object) this;
        RandomSource random = world.getRandom();

        if (random.nextInt(100) < 3) {
            BabySpiderEntity babySpider = WebEntities.BABY_SPIDER.create(spider.level(), EntitySpawnReason.JOCKEY);
            if (babySpider != null) {
                babySpider.snapTo(
                        spider.getX(),
                        spider.getY(),
                        spider.getZ(),
                        spider.getYRot(),
                        0.0F
                );
                babySpider.finalizeSpawn(world, difficulty, spawnReason, null);
                babySpider.startRiding(spider);
                spider.level().addFreshEntity(babySpider);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (isCaveSpider()) return;

        if (this.spiderState == null) {
            this.spiderState = SpiderState.IDLE;
        }
        if (this.previousSpiderState == null) {
            this.previousSpiderState = SpiderState.IDLE;
        }

        Spider spider = (Spider) (Object) this;
        Level world = spider.level();

        if (spider.isRemoved() || world == null) {
            return;
        }

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

        handleStateTransitions();

        LivingEntity currentTarget = spider.getTarget();
        if (currentTarget != null && isWebImmune(currentTarget)) {
            spider.setTarget(null);
        }

        spiderAnimationStartedThisTick = false;

        if (spiderState != SpiderState.IDLE) {
            stateTimer++;
        } else {
            stateTimer = 0;
        }

        if (spiderState != SpiderState.SHOOTING && stateTimer == 0) {
            boolean isMoving = spider.getDeltaMovement().horizontalDistance() > 0.01;

            if (isMoving) {
                if (this.spiderState != SpiderState.WALKING) {
                    setSpiderState(SpiderState.WALKING);
                }
            } else {
                if (this.spiderState != SpiderState.IDLE) {
                    setSpiderState(SpiderState.IDLE);
                }
            }
        }

        if (world.isClientSide()) {
            updateSpiderAnimations(spider);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void syncStateFromDataTracker(CallbackInfo ci) {
        Spider spider = (Spider) (Object) this;
        Level world = spider.level();

        if (world != null && world.isClientSide()) {
            try {
                int stateValue = spider.getEntityData().get(SPIDER_DATA_ID_STATE);
                SpiderState newState = SpiderState.values()[stateValue];
                if (this.spiderState != newState) {
                    this.spiderState = newState;
                    this.stateTimer = 0;
                }
            } catch (Exception e) {
            }
        }
    }

    @Unique
    private void handleStateTransitions() {
        Spider spider = (Spider) (Object) this;

        switch (spiderState) {
            case SHOOTING:
                if (stateTimer == 17 && shootTarget != null && !spider.level().isClientSide()) {
                    performWebShot(shootTarget);
                }
                if (stateTimer >= 20) {
                    shootTarget = null;
                    boolean isMoving = spider.getDeltaMovement().horizontalDistance() > 0.01;
                    setSpiderState(isMoving ? SpiderState.WALKING : SpiderState.IDLE);
                }
                break;
            case WALKING:
            case IDLE:
                boolean isMoving = spider.getDeltaMovement().horizontalDistance() > 0.01;
                if (isMoving && spiderState != SpiderState.WALKING) {
                    setSpiderState(SpiderState.WALKING);
                } else if (!isMoving && spiderState != SpiderState.IDLE) {
                    setSpiderState(SpiderState.IDLE);
                }
                break;
        }
    }

    @Unique
    private void updateSpiderAnimations(Spider spider) {
        ensureAnimationStatesInitialized();

        if (this.spiderState == SpiderState.SHOOTING) {
            if (!isSpiderShootingAnimationRunning) {
                this.spiderIdleAnimationState.stop();
                this.spiderWalkingAnimationState.stop();
                this.isSpiderIdleAnimationRunning = false;
                this.isSpiderWalkingAnimationRunning = false;
                this.spiderIdleAnimationTimeout = 0;

                this.spiderShootingAnimationState.start(spider.tickCount);
                this.isSpiderShootingAnimationRunning = true;
            }
        } else if (this.spiderState == SpiderState.WALKING) {
            if (!isSpiderWalkingAnimationRunning) {
                this.spiderIdleAnimationState.stop();
                this.spiderShootingAnimationState.stop();
                this.isSpiderIdleAnimationRunning = false;
                this.isSpiderShootingAnimationRunning = false;
                this.spiderIdleAnimationTimeout = 0;

                this.spiderWalkingAnimationState.start(spider.tickCount);
                this.isSpiderWalkingAnimationRunning = true;
            }
        } else if (this.spiderState == SpiderState.IDLE) {
            if (!isSpiderIdleAnimationRunning) {
                --this.spiderIdleAnimationTimeout;
                if (this.spiderIdleAnimationTimeout <= 0) {
                    this.spiderWalkingAnimationState.stop();
                    this.spiderShootingAnimationState.stop();
                    this.isSpiderWalkingAnimationRunning = false;
                    this.isSpiderShootingAnimationRunning = false;

                    this.spiderIdleAnimationTimeout = spider.getRandom().nextInt(40) + 80;
                    this.spiderIdleAnimationState.start(spider.tickCount);
                    this.isSpiderIdleAnimationRunning = true;
                }
            }
        }

        if (isSpiderShootingAnimationRunning && this.spiderState != SpiderState.SHOOTING) {
            this.spiderShootingAnimationState.stop();
            this.isSpiderShootingAnimationRunning = false;
        }
    }

    @Unique
    private void setSpiderState(SpiderState newState) {
        Spider spider = (Spider) (Object) this;
        Level world = spider.level();

        if (world == null || this.spiderState == newState || isSpiderChangingState) {
            return;
        }

        isSpiderChangingState = true;
        this.previousSpiderState = this.spiderState;
        this.spiderState = newState;
        this.stateTimer = 0;

        if (!world.isClientSide()) {
            spider.getEntityData().set(SPIDER_DATA_ID_STATE, newState.ordinal());
        }

        isSpiderChangingState = false;
    }

    @Override
    @Unique
    public void shootWeb(LivingEntity target) {
        if (isCaveSpider()) return;
        if (isWebImmune(target)) return;

        Spider spider = (Spider) (Object) this;
        Level world = spider.level();

        shootTarget = target;
        setSpiderState(SpiderState.SHOOTING);

        ensureAnimationStatesInitialized();
        if (world != null && world.isClientSide()) {
            this.spiderShootingAnimationState.start(spider.tickCount);
            this.isSpiderShootingAnimationRunning = true;

            this.spiderIdleAnimationState.stop();
            this.spiderWalkingAnimationState.stop();
            this.isSpiderIdleAnimationRunning = false;
            this.isSpiderWalkingAnimationRunning = false;
        }
    }

    @Unique
    @Override
    public void onTrackedDataSet(EntityDataAccessor<?> data, CallbackInfo ci) {
        web_1_21_6_7$onDataTrackerSync(data, ci);
    }

    @Unique
    private void performWebShot(LivingEntity target) {
        Spider spider = (Spider) (Object) this;
        Level world = spider.level();

        SpiderWebProjectileEntity webProjectile = new SpiderWebProjectileEntity(world, spider);
        webProjectile.setPos(spider.getX(), spider.getEyeY() - 0.1, spider.getZ());

        Vec3 spiderPos = new Vec3(spider.getX(), spider.getEyeY(), spider.getZ());
        Vec3 targetVelocity = target.getDeltaMovement();

        double distance = spider.distanceTo(target);
        double timeToHit = distance / 1.5;

        Vec3 predictedTargetPos = new Vec3(
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

        webProjectile.setDeltaMovement(
                normalizedX * horizontalSpeed + randomX,
                verticalSpeed + randomY,
                normalizedZ * horizontalSpeed + randomZ
        );

        spider.playSound(SoundEvents.SPIDER_AMBIENT, 1.0f, 0.6f + spider.getRandom().nextFloat() * 0.4f);
        world.addFreshEntity(webProjectile);
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

    @Override
    @Unique
    public boolean isShootingWeb() { return spiderState == SpiderState.SHOOTING; }

    @Override
    @Unique
    public int getShootAnimationTicks() { return stateTimer; }

    @Override
    @Unique
    public AnimationState web_1_21_6_7$getIdleAnimationState() {
        ensureAnimationStatesInitialized();
        return spiderIdleAnimationState;
    }

    @Override
    @Unique
    public AnimationState web_1_21_6_7$getWalkingAnimationState() {
        ensureAnimationStatesInitialized();
        return spiderWalkingAnimationState;
    }

    @Override
    @Unique
    public AnimationState web_1_21_6_7$getShootingAnimationState() {
        ensureAnimationStatesInitialized();
        return spiderShootingAnimationState;
    }

    @Override
    @Unique
    public SpiderState web_1_21_6_7$getSpiderState() {
        return spiderState;
    }

    @Override
    @Unique
    public SpiderState web_1_21_6_7$getPreviousState() {
        return previousSpiderState;
    }

    @Override
    @Unique
    public void web_1_21_6_7$setSpiderState(SpiderState state) {
        setSpiderState(state);
    }

    @Unique
    @Override
    public void web_1_21_6_7$onDataTrackerSync(EntityDataAccessor<?> data, CallbackInfo ci) {
        Spider spider = (Spider) (Object) this;

        if (spider instanceof CaveSpider) {
            return;
        }

        Level world = spider.level();

        if (SPIDER_DATA_ID_STATE.equals(data) && world != null && world.isClientSide()) {
            try {
                SpiderState newState = SpiderState.values()[spider.getEntityData().get(SPIDER_DATA_ID_STATE)];
                if (this.spiderState != newState && !isSpiderChangingState) {
                    isSpiderChangingState = true;
                    this.previousSpiderState = this.spiderState;
                    this.spiderState = newState;
                    updateSpiderAnimations(spider);
                    isSpiderChangingState = false;
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Unique
    @Override
    public void web_1_21_6_7$writeCustomData(ValueOutput nbt) {
        Spider spider = (Spider) (Object) this;

        if (!(spider instanceof CaveSpider)) {
            nbt.putString("SpiderState", spiderState.name());
        }
    }

    @Unique
    @Override
    public void web_1_21_6_7$readCustomData(ValueInput nbt) {
        Spider spider = (Spider) (Object) this;

        if (spider instanceof CaveSpider) {
            return;
        }

        String stateString = nbt.getStringOr("SpiderState", "IDLE");
        try {
            SpiderState loadedState = SpiderState.valueOf(stateString != null ? stateString : "IDLE");
            this.spiderState = loadedState;

            Level world = spider.level();
            if (world != null && !world.isClientSide()) {
                spider.getEntityData().set(SPIDER_DATA_ID_STATE, loadedState.ordinal());
            }
        } catch (Exception e) {
            this.spiderState = SpiderState.IDLE;
        }
    }
}