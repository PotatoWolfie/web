package potatowolfie.web.interfaces;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.enums.SpiderState;

public interface SpiderAnimationInterface {
    AnimationState web$getIdleAnimationState();
    AnimationState web$getWalkingAnimationState();
    AnimationState web$getShootingAnimationState();
    SpiderState web$getSpiderState();
    SpiderState web$getPreviousState();
    void web$setSpiderState(SpiderState state);
    void web$onDataTrackerSync(EntityDataAccessor<?> data, CallbackInfo ci);
    void web$writeCustomData(ValueOutput nbt);
    void web$readCustomData(ValueInput nbt);
}