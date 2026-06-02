package potatowolfie.web.interfaces;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.enums.SpiderState;

public interface SpiderAnimationInterface {
    AnimationState web_1_21_6_7$getIdleAnimationState();
    AnimationState web_1_21_6_7$getWalkingAnimationState();
    AnimationState web_1_21_6_7$getShootingAnimationState();
    SpiderState web_1_21_6_7$getSpiderState();
    SpiderState web_1_21_6_7$getPreviousState();
    void web_1_21_6_7$setSpiderState(SpiderState state);
    void web_1_21_6_7$onDataTrackerSync(TrackedData<?> data, CallbackInfo ci);
    void web_1_21_6_7$writeCustomData(WriteView nbt);
    void web_1_21_6_7$readCustomData(ReadView nbt);
}