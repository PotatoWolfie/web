package potatowolfie.web.mixin;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.enums.SpiderState;
import potatowolfie.web.interfaces.SpiderAnimationInterface;

@Mixin(SpiderEntity.class)
public class SpiderAnimationMixin implements SpiderAnimationInterface {
    @Unique
    public final AnimationState idleAnimationState = new AnimationState();
    @Unique
    public final AnimationState walkingAnimationState = new AnimationState();

    @Unique
    private int idleAnimationTimeout = 0;
    @Unique
    private boolean isIdleAnimationRunning = false;
    @Unique
    private boolean isWalkingAnimationRunning = false;
    @Unique
    private boolean isChangingState = false;
    @Unique
    private boolean animationStartedThisTick = false;

    @Unique
    private static final TrackedData<Integer> DATA_ID_STATE =
            DataTracker.registerData(SpiderEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Unique
    private SpiderState spiderState = SpiderState.IDLE;
    @Unique
    private SpiderState previousState = SpiderState.IDLE;

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void addAnimationDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(DATA_ID_STATE, SpiderState.IDLE.ordinal());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity) (Object) this;
        World world = spider.getWorld();

        if (spider.isRemoved() || world == null) {
            return;
        }

        animationStartedThisTick = false;

        boolean isMoving = spider.getVelocity().horizontalLength() > 0.01;

        if (isMoving) {
            if (this.spiderState != SpiderState.WALKING) {
                setSpiderState(SpiderState.WALKING, spider);
            }
        } else {
            if (this.spiderState != SpiderState.IDLE) {
                setSpiderState(SpiderState.IDLE, spider);
            }
        }

        if (world.isClient()) {
            updateAnimations(spider);
        }
    }

    @Unique
    private void updateAnimations(SpiderEntity spider) {
        if (this.spiderState == SpiderState.WALKING) {
            if (!isWalkingAnimationRunning) {
                this.idleAnimationState.stop();
                this.isIdleAnimationRunning = false;
                this.idleAnimationTimeout = 0;

                this.walkingAnimationState.start(spider.age);
                this.isWalkingAnimationRunning = true;
            }
        } else if (this.spiderState == SpiderState.IDLE) {
            if (!isIdleAnimationRunning) {
                --this.idleAnimationTimeout;
                if (this.idleAnimationTimeout <= 0) {
                    this.walkingAnimationState.stop();
                    this.isWalkingAnimationRunning = false;

                    this.idleAnimationTimeout = spider.getRandom().nextInt(40) + 80;
                    this.idleAnimationState.start(spider.age);
                    this.isIdleAnimationRunning = true;
                }
            }
        }
    }

    @Unique
    private void setSpiderState(SpiderState newState, SpiderEntity spider) {
        World world = spider.getWorld();

        if (world == null || this.spiderState == newState || isChangingState) {
            return;
        }

        isChangingState = true;
        this.previousState = this.spiderState;
        this.spiderState = newState;

        if (!world.isClient()) {
            spider.getDataTracker().set(DATA_ID_STATE, newState.ordinal());
        }

        isChangingState = false;
    }

    @Unique
    @Override
    public void web_1_21_6_7$onDataTrackerSync(TrackedData<?> data, CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity) (Object) this;
        World world = spider.getWorld();

        if (DATA_ID_STATE.equals(data) && world != null && world.isClient()) {
            try {
                SpiderState newState = SpiderState.values()[spider.getDataTracker().get(DATA_ID_STATE)];
                if (this.spiderState != newState && !isChangingState) {
                    isChangingState = true;
                    this.previousState = this.spiderState;
                    this.spiderState = newState;
                    updateAnimations(spider);
                    isChangingState = false;
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Unique
    @Override
    public void web_1_21_6_7$writeCustomData(WriteView nbt) {
        nbt.putString("SpiderState", spiderState.name());
    }

    @Unique
    @Override
    public void web_1_21_6_7$readCustomData(ReadView nbt) {
        String stateString = nbt.getString("SpiderState", "IDLE");
        try {
            SpiderState loadedState = SpiderState.valueOf(stateString);
            this.spiderState = loadedState;

            SpiderEntity spider = (SpiderEntity) (Object) this;
            World world = spider.getWorld();

            if (world != null && !world.isClient()) {
                spider.getDataTracker().set(DATA_ID_STATE, loadedState.ordinal());
            }
        } catch (IllegalArgumentException e) {
            this.spiderState = SpiderState.IDLE;
        }
    }

    @Override
    @Unique
    public AnimationState web_1_21_6_7$getIdleAnimationState() {
        return idleAnimationState;
    }

    @Override
    @Unique
    public AnimationState web_1_21_6_7$getWalkingAnimationState() {
        return walkingAnimationState;
    }

    @Override
    @Unique
    public SpiderState web_1_21_6_7$getSpiderState() {
        return spiderState;
    }

    @Override
    @Unique
    public SpiderState web_1_21_6_7$getPreviousState() {
        return previousState;
    }
}