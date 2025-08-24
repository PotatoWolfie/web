package potatowolfie.web.mixin;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
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

@Mixin(SpiderEntity.class)
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
    public final AnimationState spiderIdleAnimationState = new AnimationState();
    @Unique
    public final AnimationState spiderWalkingAnimationState = new AnimationState();
    @Unique
    public final AnimationState spiderShootingAnimationState = new AnimationState();

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
    private static final TrackedData<Integer> SPIDER_DATA_ID_STATE =
            DataTracker.registerData(SpiderEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Unique
    private SpiderState spiderState = SpiderState.IDLE;
    @Unique
    private SpiderState previousSpiderState = SpiderState.IDLE;

    @Unique
    private static final TagKey<EntityType<?>> WEB_IMMUNE_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE,
            Identifier.of(Web.MOD_ID, "web_immune"));

    @Unique
    private static boolean isWebImmune(LivingEntity entity) {
        return entity.getType().isIn(WEB_IMMUNE_TAG);
    }

    @Unique
    private boolean isCaveSpider() {
        return (Object) this instanceof CaveSpiderEntity;
    }

    // Helper method to get goalSelector field with correct names
    @Unique
    private static Field getGoalSelectorField() throws NoSuchFieldException {
        NoSuchFieldException lastException = null;

        // Try all possible field names for goalSelector
        String[] possibleNames = {
                "goalSelector",     // Deobfuscated name (dev environment)
                "field_6201",       // Correct intermediary name from Yarn docs
                "bO"                // Obfuscated name pattern
        };

        for (String name : possibleNames) {
            try {
                return MobEntity.class.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                lastException = e;
                // Continue trying other names
            }
        }

        // If we get here, none of the names worked
        throw new NoSuchFieldException("Could not find goalSelector field with any of the tried names: " + String.join(", ", possibleNames));
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void addSpiderAnimationDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity) (Object) this;

        if (!(spider instanceof CaveSpiderEntity)) {
            builder.add(SPIDER_DATA_ID_STATE, SpiderState.IDLE.ordinal());
        }
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void addWebShootingGoal(CallbackInfo ci) {
        if (goalsInitialized) return;
        goalsInitialized = true;

        if (isCaveSpider()) return;

        SpiderEntity spider = (SpiderEntity) (Object) this;

        try {
            // Use the helper method with correct field names
            Field goalSelectorField = getGoalSelectorField();
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
            Web.LOGGER.error("Failed to initialize spider goals: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Inject(method = "initialize", at = @At("RETURN"))
    private void spawnWithBabyRider(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        if (isCaveSpider()) return;

        SpiderEntity spider = (SpiderEntity) (Object) this;
        Random random = world.getRandom();

        if (random.nextInt(100) < 3) {
            BabySpiderEntity babySpider = WebEntities.BABY_SPIDER.create(spider.getWorld(), SpawnReason.JOCKEY);
            if (babySpider != null) {
                babySpider.refreshPositionAndAngles(
                        spider.getX(),
                        spider.getY(),
                        spider.getZ(),
                        spider.getYaw(),
                        0.0F
                );
                babySpider.initialize(world, difficulty, spawnReason, null);
                babySpider.startRiding(spider);
                spider.getWorld().spawnEntity(babySpider);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (isCaveSpider()) return;

        SpiderEntity spider = (SpiderEntity) (Object) this;
        World world = spider.getWorld();

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
            boolean isMoving = spider.getVelocity().horizontalLength() > 0.01;

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

        if (world.isClient()) {
            updateSpiderAnimations(spider);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void syncStateFromDataTracker(CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity) (Object) this;
        World world = spider.getWorld();

        if (world != null && world.isClient()) {
            try {
                int stateValue = spider.getDataTracker().get(SPIDER_DATA_ID_STATE);
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
        SpiderEntity spider = (SpiderEntity) (Object) this;

        switch (spiderState) {
            case SHOOTING:
                if (stateTimer == 17 && shootTarget != null && !spider.getWorld().isClient()) {
                    performWebShot(shootTarget);
                }
                if (stateTimer >= 20) {
                    shootTarget = null;
                    boolean isMoving = spider.getVelocity().horizontalLength() > 0.01;
                    setSpiderState(isMoving ? SpiderState.WALKING : SpiderState.IDLE);
                }
                break;
            case WALKING:
            case IDLE:
                boolean isMoving = spider.getVelocity().horizontalLength() > 0.01;
                if (isMoving && spiderState != SpiderState.WALKING) {
                    setSpiderState(SpiderState.WALKING);
                } else if (!isMoving && spiderState != SpiderState.IDLE) {
                    setSpiderState(SpiderState.IDLE);
                }
                break;
        }
    }

    @Unique
    private void updateSpiderAnimations(SpiderEntity spider) {
        if (this.spiderState == SpiderState.SHOOTING) {
            if (!isSpiderShootingAnimationRunning) {
                this.spiderIdleAnimationState.stop();
                this.spiderWalkingAnimationState.stop();
                this.isSpiderIdleAnimationRunning = false;
                this.isSpiderWalkingAnimationRunning = false;
                this.spiderIdleAnimationTimeout = 0;

                this.spiderShootingAnimationState.start(spider.age);
                this.isSpiderShootingAnimationRunning = true;
            }
        } else if (this.spiderState == SpiderState.WALKING) {
            if (!isSpiderWalkingAnimationRunning) {
                this.spiderIdleAnimationState.stop();
                this.spiderShootingAnimationState.stop();
                this.isSpiderIdleAnimationRunning = false;
                this.isSpiderShootingAnimationRunning = false;
                this.spiderIdleAnimationTimeout = 0;

                this.spiderWalkingAnimationState.start(spider.age);
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
                    this.spiderIdleAnimationState.start(spider.age);
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
        SpiderEntity spider = (SpiderEntity) (Object) this;
        World world = spider.getWorld();

        if (world == null || this.spiderState == newState || isSpiderChangingState) {
            return;
        }

        isSpiderChangingState = true;
        this.previousSpiderState = this.spiderState;
        this.spiderState = newState;
        this.stateTimer = 0;

        if (!world.isClient()) {
            spider.getDataTracker().set(SPIDER_DATA_ID_STATE, newState.ordinal());
        }

        isSpiderChangingState = false;
    }

    @Override
    @Unique
    public void shootWeb(LivingEntity target) {
        if (isCaveSpider()) return;
        if (isWebImmune(target)) return;

        SpiderEntity spider = (SpiderEntity) (Object) this;
        World world = spider.getWorld();

        shootTarget = target;
        setSpiderState(SpiderState.SHOOTING);

        if (world != null && world.isClient()) {
            this.spiderShootingAnimationState.start(spider.age);
            this.isSpiderShootingAnimationRunning = true;

            this.spiderIdleAnimationState.stop();
            this.spiderWalkingAnimationState.stop();
            this.isSpiderIdleAnimationRunning = false;
            this.isSpiderWalkingAnimationRunning = false;
        }
    }

    @Unique
    @Override
    public void onTrackedDataSet(TrackedData<?> data, CallbackInfo ci) {
        web_1_21_6_7$onDataTrackerSync(data, ci);
    }

    @Unique
    private void performWebShot(LivingEntity target) {
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

    @Override
    @Unique
    public boolean isShootingWeb() { return spiderState == SpiderState.SHOOTING; }

    @Override
    @Unique
    public int getShootAnimationTicks() { return stateTimer; }

    @Override
    @Unique
    public AnimationState web_1_21_6_7$getIdleAnimationState() {
        return spiderIdleAnimationState;
    }

    @Override
    @Unique
    public AnimationState web_1_21_6_7$getWalkingAnimationState() {
        return spiderWalkingAnimationState;
    }

    @Override
    @Unique
    public AnimationState web_1_21_6_7$getShootingAnimationState() {
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
    public void web_1_21_6_7$onDataTrackerSync(TrackedData<?> data, CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity) (Object) this;

        if (spider instanceof CaveSpiderEntity) {
            return;
        }

        World world = spider.getWorld();

        if (SPIDER_DATA_ID_STATE.equals(data) && world != null && world.isClient()) {
            try {
                SpiderState newState = SpiderState.values()[spider.getDataTracker().get(SPIDER_DATA_ID_STATE)];
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
    public void web_1_21_6_7$writeCustomData(WriteView nbt) {
        SpiderEntity spider = (SpiderEntity) (Object) this;

        if (!(spider instanceof CaveSpiderEntity)) {
            nbt.putString("SpiderState", spiderState.name());
        }
    }

    @Unique
    @Override
    public void web_1_21_6_7$readCustomData(ReadView nbt) {
        SpiderEntity spider = (SpiderEntity) (Object) this;

        if (spider instanceof CaveSpiderEntity) {
            return;
        }

        String stateString = nbt.getString("SpiderState", "IDLE");
        try {
            SpiderState loadedState = SpiderState.valueOf(stateString);
            this.spiderState = loadedState;

            World world = spider.getWorld();

            if (world != null && !world.isClient()) {
                spider.getDataTracker().set(SPIDER_DATA_ID_STATE, loadedState.ordinal());
            }
        } catch (IllegalArgumentException e) {
            this.spiderState = SpiderState.IDLE;
        }
    }
}