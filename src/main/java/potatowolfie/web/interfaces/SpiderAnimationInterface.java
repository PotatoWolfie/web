package potatowolfie.web.interfaces;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.enums.SpiderState;

public interface SpiderAnimationInterface {
    AnimationState web_1_21_6_7$getIdleAnimationState();
    AnimationState web_1_21_6_7$getWalkingAnimationState();
    AnimationState web_1_21_6_7$getShootingAnimationState();
    SpiderState web_1_21_6_7$getSpiderState();
    SpiderState web_1_21_6_7$getPreviousState();
    void web_1_21_6_7$setSpiderState(SpiderState state);
    void web_1_21_6_7$onDataTrackerSync(EntityDataAccessor<?> data, CallbackInfo ci);
    void web_1_21_6_7$writeCustomData(ValueOutput nbt);
    void web_1_21_6_7$readCustomData(ValueInput nbt);
}