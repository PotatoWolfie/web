package potatowolfie.web.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.enums.CaveSpiderState;
import potatowolfie.web.interfaces.CaveSpiderAnimationInterface;

@Mixin(Spider.class)
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
    private static final EntityDataAccessor<Integer> CAVE_SPIDER_DATA_ID_STATE =
            SynchedEntityData.defineId(CaveSpider.class, EntityDataSerializers.INT);

    @Unique
    private CaveSpiderState caveSpiderState = CaveSpiderState.IDLE;
    @Unique
    private CaveSpiderState previousCaveSpiderState = CaveSpiderState.IDLE;

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void addCaveSpiderAnimationDataTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        Spider spider = (Spider) (Object) this;

        if (spider instanceof CaveSpider) {
            builder.define(CAVE_SPIDER_DATA_ID_STATE, CaveSpiderState.IDLE.ordinal());
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onCaveSpiderTick(CallbackInfo ci) {
        Spider spider = (Spider) (Object) this;

        if (!(spider instanceof CaveSpider)) {
            return;
        }

        CaveSpider caveSpider = (CaveSpider) spider;
        Level world = caveSpider.level();

        if (caveSpider.isRemoved() || world == null) {
            return;
        }

        caveSpiderAnimationStartedThisTick = false;

        boolean isMoving = caveSpider.getDeltaMovement().horizontalDistance() > 0.01;

        if (isMoving) {
            if (this.caveSpiderState != CaveSpiderState.WALKING) {
                setCaveSpiderState(CaveSpiderState.WALKING, caveSpider);
            }
        } else {
            if (this.caveSpiderState != CaveSpiderState.IDLE) {
                setCaveSpiderState(CaveSpiderState.IDLE, caveSpider);
            }
        }

        if (world.isClientSide()) {
            updateCaveSpiderAnimations(caveSpider);
        }
    }

    @Unique
    private void updateCaveSpiderAnimations(CaveSpider caveSpider) {
        if (this.caveSpiderState == CaveSpiderState.WALKING) {
            if (!isCaveSpiderWalkingAnimationRunning) {
                this.caveSpiderIdleAnimationState.stop();
                this.isCaveSpiderIdleAnimationRunning = false;
                this.caveSpiderIdleAnimationTimeout = 0;

                this.caveSpiderWalkingAnimationState.start(caveSpider.tickCount);
                this.isCaveSpiderWalkingAnimationRunning = true;
            }
        } else if (this.caveSpiderState == CaveSpiderState.IDLE) {
            if (!isCaveSpiderIdleAnimationRunning) {
                --this.caveSpiderIdleAnimationTimeout;
                if (this.caveSpiderIdleAnimationTimeout <= 0) {
                    this.caveSpiderWalkingAnimationState.stop();
                    this.isCaveSpiderWalkingAnimationRunning = false;

                    this.caveSpiderIdleAnimationTimeout = caveSpider.getRandom().nextInt(40) + 80;
                    this.caveSpiderIdleAnimationState.start(caveSpider.tickCount);
                    this.isCaveSpiderIdleAnimationRunning = true;
                }
            }
        }
    }

    @Unique
    private void setCaveSpiderState(CaveSpiderState newState, CaveSpider caveSpider) {
        Level world = caveSpider.level();

        if (world == null || this.caveSpiderState == newState || isCaveSpiderChangingState) {
            return;
        }

        isCaveSpiderChangingState = true;
        this.previousCaveSpiderState = this.caveSpiderState;
        this.caveSpiderState = newState;

        if (!world.isClientSide()) {
            caveSpider.getEntityData().set(CAVE_SPIDER_DATA_ID_STATE, newState.ordinal());
        }

        isCaveSpiderChangingState = false;
    }

    @Unique
    public void web_1_21_6_7$onCaveSpiderDataTrackerSync(EntityDataAccessor<?> data, CallbackInfo ci) {
        Spider spider = (Spider) (Object) this;

        if (!(spider instanceof CaveSpider)) {
            return;
        }

        CaveSpider caveSpider = (CaveSpider) spider;
        Level world = caveSpider.level();

        if (CAVE_SPIDER_DATA_ID_STATE.equals(data) && world != null && world.isClientSide()) {
            try {
                CaveSpiderState newState = CaveSpiderState.values()[caveSpider.getEntityData().get(CAVE_SPIDER_DATA_ID_STATE)];
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
    public void web_1_21_6_7$writeCaveSpiderCustomData(ValueOutput nbt) {
        Spider spider = (Spider) (Object) this;

        if (spider instanceof CaveSpider) {
            nbt.putString("CaveSpiderState", caveSpiderState.name());
        }
    }

    @Unique
    public void web_1_21_6_7$readCaveSpiderCustomData(ValueInput nbt) {
        Spider spider = (Spider) (Object) this;

        if (!(spider instanceof CaveSpider)) {
            return;
        }

        String stateString = nbt.getStringOr("CaveSpiderState", "IDLE");
        try {
            CaveSpiderState loadedState = CaveSpiderState.valueOf(stateString);
            this.caveSpiderState = loadedState;

            CaveSpider caveSpider = (CaveSpider) spider;
            Level world = caveSpider.level();

            if (world != null && !world.isClientSide()) {
                caveSpider.getEntityData().set(CAVE_SPIDER_DATA_ID_STATE, loadedState.ordinal());
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