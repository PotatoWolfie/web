package potatowolfie.web.interfaces;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.enums.CaveSpiderState;

public interface CaveSpiderAnimationInterface {
    void web$onCaveSpiderDataTrackerSync(EntityDataAccessor<?> data, CallbackInfo ci);
    void web$writeCaveSpiderCustomData(ValueOutput nbt);
    void web$readCaveSpiderCustomData(ValueInput nbt);
    AnimationState web$getCaveSpiderIdleAnimationState();
    AnimationState web$getCaveSpiderWalkingAnimationState();
    CaveSpiderState web$getCaveSpiderState();
    CaveSpiderState web$getPreviousState();
}