package potatowolfie.web.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BabySpiderEntity extends Monster {
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

    private static final EntityDataAccessor<Integer> DATA_ID_STATE =
            SynchedEntityData.defineId(BabySpiderEntity.class, EntityDataSerializers.INT);

    private SpiderState spiderState = SpiderState.IDLE;
    private SpiderState previousState = SpiderState.IDLE;
    private boolean isChangingState = false;

    private static final EntityDataAccessor<Byte> SPIDER_FLAGS;
    private static final float field_30498 = 0.1F;

    private static final EntityDataAccessor<Integer> AGE_TICKS;
    private static final EntityDataAccessor<Integer> MATURE_TIME;
    private static final int MIN_MATURE_TIME = 18000;
    private static final int MAX_MATURE_TIME = 26400;

    public BabySpiderEntity(EntityType<? extends BabySpiderEntity> entityType, Level world) {
        super(entityType, world);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Armadillo>(this, Armadillo.class, 6.0F, 1.0, 1.2, (entity) -> {
            return !((Armadillo)entity).isScared();
        }));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new AttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new TargetGoal<>(this, Player.class));
        this.targetSelector.addGoal(3, new TargetGoal<>(this, IronGolem.class));
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new WallClimberNavigation(this, world) {
            @Override
            public void tick() {
                super.tick();
                if (this.isDone() && this.mob.tickCount % 20 == 0) {
                    this.stop();
                }
            }
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPIDER_FLAGS, (byte)0);
        builder.define(AGE_TICKS, 0);
        builder.define(MATURE_TIME, -1);
        builder.define(DATA_ID_STATE, SpiderState.IDLE.ordinal());
    }

    private void updateAnimations() {
        if (this.level().isClientSide()) {
            if (this.spiderState == SpiderState.WALKING) {
                if (!isWalkingAnimationRunning) {
                    this.walkingAnimationState.start(this.tickCount);
                    this.isWalkingAnimationRunning = true;
                    this.isIdleAnimationRunning = false;
                }
            }
            else if (this.spiderState == SpiderState.IDLE) {
                if (!isIdleAnimationRunning) {
                    --this.idleAnimationTimeout;
                    if (this.idleAnimationTimeout <= 0) {
                        this.idleAnimationTimeout = this.random.nextInt(40) + 80;
                        this.idleAnimationState.start(this.tickCount);
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

            if (!this.level().isClientSide()) {
                this.entityData.set(DATA_ID_STATE, newState.ordinal());
            } else {
                startStateAnimation(newState);
            }

            isChangingState = false;
        }
    }

    private void startStateAnimation(SpiderState state) {
        if (!this.level().isClientSide() || animationStartedThisTick) return;

        animationStartedThisTick = true;

        switch (state) {
            case IDLE -> {
                stopAllAnimations();
                this.idleAnimationTimeout = this.random.nextInt(40) + 80;
                this.idleAnimationState.start(this.tickCount);
                this.isIdleAnimationRunning = true;
                this.isWalkingAnimationRunning = false;
            }
            case WALKING -> {
                stopAllAnimations();
                this.walkingAnimationState.start(this.tickCount);
                this.isWalkingAnimationRunning = true;
                this.isIdleAnimationRunning = false;
            }
        }
    }

    private void stopAllAnimations() {
        if (this.level().isClientSide()) {
            idleAnimationState.stop();
            walkingAnimationState.stop();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (DATA_ID_STATE.equals(data) && this.level().isClientSide()) {
            SpiderState newState = SpiderState.values()[this.entityData.get(DATA_ID_STATE)];
            if (this.spiderState != newState && !isChangingState) {
                isChangingState = true;

                this.previousState = this.spiderState;
                this.spiderState = newState;

                startStateAnimation(newState);

                isChangingState = false;
            }
        }
        super.onSyncedDataUpdated(data);
    }

    private int climbingStateCooldown = 0;
    private int collisionCheckTicks = 0;
    private boolean stableHorizontalCollision = false;

    @Override
    public void tick() {
        if (this.isRemoved() || this.level() == null) {
            return;
        }

        animationStartedThisTick = false;
        super.tick();

        if (!this.level().isClientSide()) {
            this.setClimbingWall(this.horizontalCollision);

            if (this.getNavigation().isDone() && this.tickCount % 20 == 0) {
                this.getNavigation().stop();
            }

            int currentAge = this.entityData.get(AGE_TICKS);
            this.entityData.set(AGE_TICKS, currentAge + 1);

            int matureTime = this.entityData.get(MATURE_TIME);
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
    public void aiStep() {
        super.aiStep();

        boolean isMoving = this.getDeltaMovement().horizontalDistance() > 0.01;

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
        if (this.level().isClientSide()) return;

        try {
            Spider adultSpider = EntityTypes.SPIDER.create(this.level(), EntitySpawnReason.CONVERSION);
            if (adultSpider != null) {
                adultSpider.snapTo(
                        this.getX(), this.getY(), this.getZ(),
                        this.getYRot(), this.getXRot()
                );

                adultSpider.setDeltaMovement(this.getDeltaMovement());

                float healthPercentage = this.getHealth() / this.getMaxHealth();
                adultSpider.setHealth(adultSpider.getMaxHealth() * healthPercentage);

                for (MobEffectInstance effect : this.getActiveEffects()) {
                    adultSpider.addEffect(new MobEffectInstance(effect));
                }

                if (this.hasCustomName()) {
                    adultSpider.setCustomName(this.getCustomName());
                    adultSpider.setCustomNameVisible(this.isCustomNameVisible());
                }

                if (this.isVehicle()) {
                    for (Entity passenger : this.getPassengers()) {
                        passenger.stopRiding();
                        passenger.startRiding(adultSpider);
                    }
                }

                this.level().addFreshEntity(adultSpider);

                this.discard();
            }
        } catch (Exception e) {
            System.err.println("Error during spider maturation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void forceMature() {
        if (!this.level().isClientSide()) {
            this.matureIntoSpider();
        }
    }

    public static AttributeSupplier.Builder createSpiderAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.JUMP_STRENGTH, 0.42);
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SPIDER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    public boolean onClimbable() {
        return this.isClimbingWall();
    }

    public void makeStuckInBlock(BlockState state, Vec3 multiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, multiplier);
        }
    }

    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    public boolean isClimbingWall() {
        return (this.entityData.get(SPIDER_FLAGS) & 1) != 0;
    }

    public void setClimbingWall(boolean climbing) {
        byte b = this.entityData.get(SPIDER_FLAGS);
        if (climbing) {
            b = (byte)(b | 1);
        } else {
            b &= -2;
        }

        this.entityData.set(SPIDER_FLAGS, b);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData) {
        SpawnGroupData result = super.finalizeSpawn(world, difficulty, spawnReason, entityData);

        RandomSource random = world.getRandom();
        int matureTime = MIN_MATURE_TIME + random.nextInt(MAX_MATURE_TIME - MIN_MATURE_TIME + 1);
        this.entityData.set(MATURE_TIME, matureTime);

        RandomSource spawnRandom = world.getRandom();
        if (spawnRandom.nextInt(100) == 0) {
            Skeleton skeletonEntity = EntityTypes.SKELETON.create(this.level(), EntitySpawnReason.JOCKEY);
            if (skeletonEntity != null) {
                skeletonEntity.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                skeletonEntity.finalizeSpawn(world, difficulty, spawnReason, null);
                skeletonEntity.startRiding(this);
            }
        }

        if (entityData == null) {
            entityData = new SpiderData();
            if (world.getDifficulty() == Difficulty.HARD && spawnRandom.nextFloat() < 0.05F * difficulty.getSpecialMultiplier()) {
                ((SpiderData)entityData).setEffect(spawnRandom);
            }
        }

        if (entityData instanceof SpiderData spiderData) {
            Holder<MobEffect> registryEntry = spiderData.effect;
            if (registryEntry != null) {
                this.addEffect(new MobEffectInstance(registryEntry, -1));
            }
        }

        return result;
    }

    public Vec3 getVehicleAttachmentPoint(Entity vehicle) {
        return vehicle.getBbWidth() <= this.getBbWidth() ? new Vec3(0.0, 0.9125 * (double)this.getScale(), 0.0) : super.getVehicleAttachmentPoint(vehicle);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putString("SpiderState", spiderState.name());
        nbt.putInt("AgeTicks", this.entityData.get(AGE_TICKS));
        nbt.putInt("MatureTime", this.entityData.get(MATURE_TIME));
    }

    @Override
    public void readAdditionalSaveData(ValueInput nbt) {
        super.readAdditionalSaveData(nbt);
        String stateString = nbt.getStringOr("SpiderState", "IDLE");
        if (!stateString.equals("IDLE")) {
            try {
                SpiderState loadedState = SpiderState.valueOf(stateString);
                this.spiderState = loadedState;
                if (!this.level().isClientSide()) {
                    this.entityData.set(DATA_ID_STATE, loadedState.ordinal());
                }
            } catch (IllegalArgumentException e) {
                this.spiderState = SpiderState.IDLE;
            }
        }

        int ageTicks = nbt.getIntOr("AgeTicks", 0);
        if (ageTicks > 0) {
            this.entityData.set(AGE_TICKS, ageTicks);
        }

        int matureTime = nbt.getIntOr("MatureTime", -1);
        if (matureTime > 0) {
            this.entityData.set(MATURE_TIME, matureTime);
        }
    }

    static {
        SPIDER_FLAGS = SynchedEntityData.defineId(BabySpiderEntity.class, EntityDataSerializers.BYTE);
        AGE_TICKS = SynchedEntityData.defineId(BabySpiderEntity.class, EntityDataSerializers.INT);
        MATURE_TIME = SynchedEntityData.defineId(BabySpiderEntity.class, EntityDataSerializers.INT);
    }

    private static class AttackGoal extends MeleeAttackGoal {
        public AttackGoal(BabySpiderEntity spider) {
            super(spider, 1.0, true);
        }

        public boolean canUse() {
            return super.canUse() && !this.mob.isVehicle();
        }

        public boolean canContinueToUse() {
            int lightLevel = this.mob.level().getMaxLocalRawBrightness(this.mob.blockPosition());
            float f = lightLevel / 15.0F;
            if (f >= 0.5F && this.mob.getRandom().nextInt(100) == 0) {
                this.mob.setTarget(null);
                return false;
            } else {
                return super.canContinueToUse();
            }
        }
    }

    private static class TargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        public TargetGoal(BabySpiderEntity spider, Class<T> targetEntityClass) {
            super(spider, targetEntityClass, true);
        }

        public boolean canUse() {
            int lightLevel = this.mob.level().getMaxLocalRawBrightness(this.mob.blockPosition());
            float f = lightLevel / 15.0F;
            return !(f >= 0.5F) && super.canUse();
        }
    }

    public static class SpiderData implements SpawnGroupData {
        @Nullable
        public Holder<MobEffect> effect;

        public SpiderData() {
        }

        public void setEffect(RandomSource random) {
            int i = random.nextInt(5);
            if (i <= 1) {
                this.effect = MobEffects.SPEED;
            } else if (i <= 2) {
                this.effect = MobEffects.STRENGTH;
            } else if (i <= 3) {
                this.effect = MobEffects.REGENERATION;
            } else if (i <= 4) {
                this.effect = MobEffects.INVISIBILITY;
            }
        }
    }
}