package potatowolfie.web.mixin;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.enums.CaveSpiderState;
import potatowolfie.web.interfaces.CaveSpiderAnimationInterface;

@Mixin(SpiderEntity.class)
public class CaveSpiderAnimationMixin implements CaveSpiderAnimationInterface {
    @Unique
    public final AnimationState caveSpiderIdleAnimationState = new AnimationState();
    @Unique
    public final AnimationState caveSpiderWalkingAnimationState = new AnimationState();

    @Unique
    private int caveSpiderIdleAnimationTimeout = 0;
    @Unique
    private boolean isCaveSpiderIdleAnimationRunning = false;
    @Unique
    private boolean isCaveSpiderWalkingAnimationRunning = false;
    @Unique
    private boolean isCaveSpiderChangingState = false;
    @Unique
    private boolean caveSpiderAnimationStartedThisTick = false;

    @Unique
    private static final TrackedData<Integer> CAVE_SPIDER_DATA_ID_STATE =
            DataTracker.registerData(CaveSpiderEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Unique
    private CaveSpiderState caveSpiderState = CaveSpiderState.IDLE;
    @Unique
    private CaveSpiderState previousCaveSpiderState = CaveSpiderState.IDLE;

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void addCaveSpiderAnimationDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity) (Object) this;

        if (spider instanceof CaveSpiderEntity) {
            builder.add(CAVE_SPIDER_DATA_ID_STATE, CaveSpiderState.IDLE.ordinal());
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onCaveSpiderTick(CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity) (Object) this;

        if (!(spider instanceof CaveSpiderEntity)) {
            return;
        }

        CaveSpiderEntity caveSpider = (CaveSpiderEntity) spider;
        World world = caveSpider.getEntityWorld();

        if (caveSpider.isRemoved() || world == null) {
            return;
        }

        caveSpiderAnimationStartedThisTick = false;

        boolean isMoving = caveSpider.getVelocity().horizontalLength() > 0.01;

        if (isMoving) {
            if (this.caveSpiderState != CaveSpiderState.WALKING) {
                setCaveSpiderState(CaveSpiderState.WALKING, caveSpider);
            }
        } else {
            if (this.caveSpiderState != CaveSpiderState.IDLE) {
                setCaveSpiderState(CaveSpiderState.IDLE, caveSpider);
            }
        }

        if (world.isClient()) {
            updateCaveSpiderAnimations(caveSpider);
        }
    }

    @Unique
    private void updateCaveSpiderAnimations(CaveSpiderEntity caveSpider) {
        if (this.caveSpiderState == CaveSpiderState.WALKING) {
            if (!isCaveSpiderWalkingAnimationRunning) {
                this.caveSpiderIdleAnimationState.stop();
                this.isCaveSpiderIdleAnimationRunning = false;
                this.caveSpiderIdleAnimationTimeout = 0;

                this.caveSpiderWalkingAnimationState.start(caveSpider.age);
                this.isCaveSpiderWalkingAnimationRunning = true;
            }
        } else if (this.caveSpiderState == CaveSpiderState.IDLE) {
            if (!isCaveSpiderIdleAnimationRunning) {
                --this.caveSpiderIdleAnimationTimeout;
                if (this.caveSpiderIdleAnimationTimeout <= 0) {
                    this.caveSpiderWalkingAnimationState.stop();
                    this.isCaveSpiderWalkingAnimationRunning = false;

                    this.caveSpiderIdleAnimationTimeout = caveSpider.getRandom().nextInt(40) + 80;
                    this.caveSpiderIdleAnimationState.start(caveSpider.age);
                    this.isCaveSpiderIdleAnimationRunning = true;
                }
            }
        }
    }

    @Unique
    private void setCaveSpiderState(CaveSpiderState newState, CaveSpiderEntity caveSpider) {
        World world = caveSpider.getEntityWorld();

        if (world == null || this.caveSpiderState == newState || isCaveSpiderChangingState) {
            return;
        }

        isCaveSpiderChangingState = true;
        this.previousCaveSpiderState = this.caveSpiderState;
        this.caveSpiderState = newState;

        if (!world.isClient()) {
            caveSpider.getDataTracker().set(CAVE_SPIDER_DATA_ID_STATE, newState.ordinal());
        }

        isCaveSpiderChangingState = false;
    }

    @Unique
    public void web_1_21_6_7$onCaveSpiderDataTrackerSync(TrackedData<?> data, CallbackInfo ci) {
        SpiderEntity spider = (SpiderEntity) (Object) this;

        if (!(spider instanceof CaveSpiderEntity)) {
            return;
        }

        CaveSpiderEntity caveSpider = (CaveSpiderEntity) spider;
        World world = caveSpider.getEntityWorld();

        if (CAVE_SPIDER_DATA_ID_STATE.equals(data) && world != null && world.isClient()) {
            try {
                CaveSpiderState newState = CaveSpiderState.values()[caveSpider.getDataTracker().get(CAVE_SPIDER_DATA_ID_STATE)];
                if (this.caveSpiderState != newState && !isCaveSpiderChangingState) {
                    isCaveSpiderChangingState = true;
                    this.previousCaveSpiderState = this.caveSpiderState;
                    this.caveSpiderState = newState;
                    updateCaveSpiderAnimations(caveSpider);
                    isCaveSpiderChangingState = false;
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Unique
    public void web_1_21_6_7$writeCaveSpiderCustomData(WriteView nbt) {
        SpiderEntity spider = (SpiderEntity) (Object) this;

        if (spider instanceof CaveSpiderEntity) {
            nbt.putString("CaveSpiderState", caveSpiderState.name());
        }
    }

    @Unique
    public void web_1_21_6_7$readCaveSpiderCustomData(ReadView nbt) {
        SpiderEntity spider = (SpiderEntity) (Object) this;

        if (!(spider instanceof CaveSpiderEntity)) {
            return;
        }

        String stateString = nbt.getString("CaveSpiderState", "IDLE");
        try {
            CaveSpiderState loadedState = CaveSpiderState.valueOf(stateString);
            this.caveSpiderState = loadedState;

            CaveSpiderEntity caveSpider = (CaveSpiderEntity) spider;
            World world = caveSpider.getEntityWorld();

            if (world != null && !world.isClient()) {
                caveSpider.getDataTracker().set(CAVE_SPIDER_DATA_ID_STATE, loadedState.ordinal());
            }
        } catch (IllegalArgumentException e) {
            this.caveSpiderState = CaveSpiderState.IDLE;
        }
    }

    @Override
    @Unique
    public AnimationState web_1_21_6_7$getCaveSpiderIdleAnimationState() {
        return caveSpiderIdleAnimationState;
    }

    @Override
    @Unique
    public AnimationState web_1_21_6_7$getCaveSpiderWalkingAnimationState() {
        return caveSpiderWalkingAnimationState;
    }

    @Override
    @Unique
    public CaveSpiderState web_1_21_6_7$getCaveSpiderState() {
        return caveSpiderState;
    }

    @Override
    @Unique
    public CaveSpiderState web_1_21_6_7$getPreviousState() {
        return previousCaveSpiderState;
    }
}