package potatowolfie.web.entity.custom;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.ArmadilloEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import org.jetbrains.annotations.Nullable;

public class BabySpiderEntity extends HostileEntity {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkingAnimationState = new AnimationState();

    private int idleAnimationTimeout = 0;
    private boolean isIdleAnimationRunning = false;
    private boolean isWalkingAnimationRunning = false;
    private boolean animationStartedThisTick = false;

    public enum SpiderState {
        IDLE,
        WALKING
    }

    private static final TrackedData<Integer> DATA_ID_STATE =
            DataTracker.registerData(BabySpiderEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private SpiderState spiderState = SpiderState.IDLE;
    private SpiderState previousState = SpiderState.IDLE;
    private boolean isChangingState = false;

    private static final TrackedData<Byte> SPIDER_FLAGS;
    private static final float field_30498 = 0.1F;

    private static final TrackedData<Integer> AGE_TICKS;
    private static final TrackedData<Integer> MATURE_TIME;
    private static final int MIN_MATURE_TIME = 18000;
    private static final int MAX_MATURE_TIME = 26400;

    public BabySpiderEntity(EntityType<? extends BabySpiderEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new FleeEntityGoal<ArmadilloEntity>(this, ArmadilloEntity.class, 6.0F, 1.0, 1.2, (entity) -> {
            return !((ArmadilloEntity)entity).isNotIdle();
        }));
        this.goalSelector.add(3, new PounceAtTargetGoal(this, 0.4F));
        this.goalSelector.add(4, new AttackGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new TargetGoal<>(this, PlayerEntity.class));
        this.targetSelector.add(3, new TargetGoal<>(this, IronGolemEntity.class));
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SpiderNavigation(this, world) {
            @Override
            public void tick() {
                super.tick();
                if (this.isIdle() && this.entity.age % 20 == 0) {
                    this.stop();
                }
            }
        };
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SPIDER_FLAGS, (byte)0);
        builder.add(AGE_TICKS, 0);
        builder.add(MATURE_TIME, -1);
        builder.add(DATA_ID_STATE, SpiderState.IDLE.ordinal());
    }

    private void updateAnimations() {
        if (this.getWorld().isClient()) {
            if (this.spiderState == SpiderState.WALKING) {
                if (!isWalkingAnimationRunning) {
                    this.walkingAnimationState.start(this.age);
                    this.isWalkingAnimationRunning = true;
                    this.isIdleAnimationRunning = false;
                }
            }
            else if (this.spiderState == SpiderState.IDLE) {
                if (!isIdleAnimationRunning) {
                    --this.idleAnimationTimeout;
                    if (this.idleAnimationTimeout <= 0) {
                        this.idleAnimationTimeout = this.random.nextInt(40) + 80;
                        this.idleAnimationState.start(this.age);
                        this.isIdleAnimationRunning = true;
                        this.isWalkingAnimationRunning = false;
                    }
                }
            }

            if (this.spiderState != SpiderState.IDLE && isIdleAnimationRunning) {
                this.idleAnimationState.stop();
                this.isIdleAnimationRunning = false;
                this.idleAnimationTimeout = 0;
            }
            if (this.spiderState != SpiderState.WALKING && isWalkingAnimationRunning) {
                this.walkingAnimationState.stop();
                this.isWalkingAnimationRunning = false;
            }
        }
    }

    public SpiderState getSpiderState() {
        return spiderState;
    }

    public SpiderState getPreviousState() {
        return previousState;
    }

    public void setSpiderState(SpiderState newState) {
        if (this.spiderState != newState && !isChangingState) {
            isChangingState = true;

            this.previousState = this.spiderState;
            this.spiderState = newState;

            if (!this.getWorld().isClient()) {
                this.dataTracker.set(DATA_ID_STATE, newState.ordinal());
            } else {
                startStateAnimation(newState);
            }

            isChangingState = false;
        }
    }

    private void startStateAnimation(SpiderState state) {
        if (!this.getWorld().isClient() || animationStartedThisTick) return;

        animationStartedThisTick = true;

        switch (state) {
            case IDLE -> {
                stopAllAnimations();
                this.idleAnimationTimeout = this.random.nextInt(40) + 80;
                this.idleAnimationState.start(this.age);
                this.isIdleAnimationRunning = true;
                this.isWalkingAnimationRunning = false;
            }
            case WALKING -> {
                stopAllAnimations();
                this.walkingAnimationState.start(this.age);
                this.isWalkingAnimationRunning = true;
                this.isIdleAnimationRunning = false;
            }
        }
    }

    private void stopAllAnimations() {
        if (this.getWorld().isClient()) {
            idleAnimationState.stop();
            walkingAnimationState.stop();
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (DATA_ID_STATE.equals(data) && this.getWorld().isClient()) {
            SpiderState newState = SpiderState.values()[this.dataTracker.get(DATA_ID_STATE)];
            if (this.spiderState != newState && !isChangingState) {
                isChangingState = true;

                this.previousState = this.spiderState;
                this.spiderState = newState;

                startStateAnimation(newState);

                isChangingState = false;
            }
        }
        super.onTrackedDataSet(data);
    }

    private int climbingStateCooldown = 0;
    private int collisionCheckTicks = 0;
    private boolean stableHorizontalCollision = false;

    @Override
    public void tick() {
        if (this.isRemoved() || this.getWorld() == null) {
            return;
        }

        animationStartedThisTick = false;
        super.tick();

        if (!this.getWorld().isClient) {
            if (climbingStateCooldown <= 0) {
                boolean shouldClimb = this.horizontalCollision;
                if (shouldClimb != this.isClimbingWall()) {
                    this.setClimbingWall(shouldClimb);
                    climbingStateCooldown = 8;
                }
            } else {
                climbingStateCooldown--;
            }

            if (this.isOnGround() && !this.horizontalCollision && this.isClimbingWall()) {
                if (climbingStateCooldown <= 0) {
                    this.setClimbingWall(false);
                    climbingStateCooldown = 5;
                }
            }

            int currentAge = this.dataTracker.get(AGE_TICKS);
            this.dataTracker.set(AGE_TICKS, currentAge + 1);

            int matureTime = this.dataTracker.get(MATURE_TIME);
            if (matureTime > 0 && currentAge >= matureTime) {
                this.matureIntoSpider();
            }
        }

        try {
            updateAnimations();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        boolean isMoving = this.getVelocity().horizontalLength() > 0.01;

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

    private void matureIntoSpider() {
        if (this.getWorld().isClient) return;

        try {
            SpiderEntity adultSpider = EntityType.SPIDER.create(this.getWorld(), SpawnReason.CONVERSION);
            if (adultSpider != null) {
                adultSpider.refreshPositionAndAngles(
                        this.getX(), this.getY(), this.getZ(),
                        this.getYaw(), this.getPitch()
                );

                adultSpider.setVelocity(this.getVelocity());

                float healthPercentage = this.getHealth() / this.getMaxHealth();
                adultSpider.setHealth(adultSpider.getMaxHealth() * healthPercentage);

                for (StatusEffectInstance effect : this.getStatusEffects()) {
                    adultSpider.addStatusEffect(new StatusEffectInstance(effect));
                }

                if (this.hasCustomName()) {
                    adultSpider.setCustomName(this.getCustomName());
                    adultSpider.setCustomNameVisible(this.isCustomNameVisible());
                }

                if (this.hasPassengers()) {
                    for (Entity passenger : this.getPassengerList()) {
                        passenger.stopRiding();
                        passenger.startRiding(adultSpider);
                    }
                }

                this.getWorld().spawnEntity(adultSpider);

                this.discard();
            }
        } catch (Exception e) {
            System.err.println("Error during spider maturation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void forceMature() {
        if (!this.getWorld().isClient) {
            this.matureIntoSpider();
        }
    }

    public static DefaultAttributeContainer.Builder createSpiderAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 10.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.5)
                .add(EntityAttributes.JUMP_STRENGTH, 0.42);
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SPIDER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SPIDER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SPIDER_DEATH;
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.15F, 1.0F);
    }

    public boolean isClimbing() {
        return this.isClimbingWall();
    }

    public void slowMovement(BlockState state, Vec3d multiplier) {
        if (!state.isOf(Blocks.COBWEB)) {
            super.slowMovement(state, multiplier);
        }
    }

    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        return !effect.equals(StatusEffects.POISON) && super.canHaveStatusEffect(effect);
    }

    public boolean isClimbingWall() {
        return (this.dataTracker.get(SPIDER_FLAGS) & 1) != 0;
    }

    public void setClimbingWall(boolean climbing) {
        byte b = this.dataTracker.get(SPIDER_FLAGS);
        if (climbing) {
            b = (byte)(b | 1);
        } else {
            b &= -2;
        }

        this.dataTracker.set(SPIDER_FLAGS, b);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        EntityData result = super.initialize(world, difficulty, spawnReason, entityData);

        Random random = world.getRandom();
        int matureTime = MIN_MATURE_TIME + random.nextInt(MAX_MATURE_TIME - MIN_MATURE_TIME + 1);
        this.dataTracker.set(MATURE_TIME, matureTime);

        Random spawnRandom = world.getRandom();
        if (spawnRandom.nextInt(100) == 0) {
            SkeletonEntity skeletonEntity = EntityType.SKELETON.create(this.getWorld(), SpawnReason.JOCKEY);
            if (skeletonEntity != null) {
                skeletonEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0F);
                skeletonEntity.initialize(world, difficulty, spawnReason, null);
                skeletonEntity.startRiding(this);
            }
        }

        if (entityData == null) {
            entityData = new BabySpiderEntity.SpiderData();
            if (world.getDifficulty() == Difficulty.HARD && spawnRandom.nextFloat() < 0.05F * difficulty.getClampedLocalDifficulty()) {
                ((BabySpiderEntity.SpiderData)entityData).setEffect(spawnRandom);
            }
        }

        if (entityData instanceof BabySpiderEntity.SpiderData spiderData) {
            RegistryEntry<StatusEffect> registryEntry = spiderData.effect;
            if (registryEntry != null) {
                this.addStatusEffect(new StatusEffectInstance(registryEntry, -1));
            }
        }

        return result;
    }

    public Vec3d getVehicleAttachmentPos(Entity vehicle) {
        return vehicle.getWidth() <= this.getWidth() ? new Vec3d(0.0, 0.9125 * (double)this.getScale(), 0.0) : super.getVehicleAttachmentPos(vehicle);
    }

    @Override
    public void writeCustomData(WriteView nbt) {
        super.writeCustomData(nbt);
        nbt.putString("SpiderState", spiderState.name());
        nbt.putInt("AgeTicks", this.dataTracker.get(AGE_TICKS));
        nbt.putInt("MatureTime", this.dataTracker.get(MATURE_TIME));
    }

    @Override
    public void readCustomData(ReadView nbt) {
        super.readCustomData(nbt);
        String stateString = nbt.getString("SpiderState", "IDLE");
        if (!stateString.equals("IDLE")) {
            try {
                SpiderState loadedState = SpiderState.valueOf(stateString);
                this.spiderState = loadedState;
                if (!this.getWorld().isClient()) {
                    this.dataTracker.set(DATA_ID_STATE, loadedState.ordinal());
                }
            } catch (IllegalArgumentException e) {
                this.spiderState = SpiderState.IDLE;
            }
        }

        int ageTicks = nbt.getInt("AgeTicks", 0);
        if (ageTicks > 0) {
            this.dataTracker.set(AGE_TICKS, ageTicks);
        }

        int matureTime = nbt.getInt("MatureTime", -1);
        if (matureTime > 0) {
            this.dataTracker.set(MATURE_TIME, matureTime);
        }
    }

    static {
        SPIDER_FLAGS = DataTracker.registerData(BabySpiderEntity.class, TrackedDataHandlerRegistry.BYTE);
        AGE_TICKS = DataTracker.registerData(BabySpiderEntity.class, TrackedDataHandlerRegistry.INTEGER);
        MATURE_TIME = DataTracker.registerData(BabySpiderEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    private static class AttackGoal extends MeleeAttackGoal {
        public AttackGoal(BabySpiderEntity spider) {
            super(spider, 1.0, true);
        }

        public boolean canStart() {
            return super.canStart() && !this.mob.hasPassengers();
        }

        public boolean shouldContinue() {
            int lightLevel = this.mob.getWorld().getLightLevel(this.mob.getBlockPos());
            float f = lightLevel / 15.0F;
            if (f >= 0.5F && this.mob.getRandom().nextInt(100) == 0) {
                this.mob.setTarget(null);
                return false;
            } else {
                return super.shouldContinue();
            }
        }
    }

    private static class TargetGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {
        public TargetGoal(BabySpiderEntity spider, Class<T> targetEntityClass) {
            super(spider, targetEntityClass, true);
        }

        public boolean canStart() {
            int lightLevel = this.mob.getWorld().getLightLevel(this.mob.getBlockPos());
            float f = lightLevel / 15.0F;
            return !(f >= 0.5F) && super.canStart();
        }
    }

    public static class SpiderData implements EntityData {
        @Nullable
        public RegistryEntry<StatusEffect> effect;

        public SpiderData() {
        }

        public void setEffect(Random random) {
            int i = random.nextInt(5);
            if (i <= 1) {
                this.effect = StatusEffects.SPEED;
            } else if (i <= 2) {
                this.effect = StatusEffects.STRENGTH;
            } else if (i <= 3) {
                this.effect = StatusEffects.REGENERATION;
            } else if (i <= 4) {
                this.effect = StatusEffects.INVISIBILITY;
            }
        }
    }
}