package potatowolfie.web.interfaces;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.enums.CaveSpiderState;

public interface CaveSpiderAnimationInterface {
    void web_1_21_6_7$onCaveSpiderDataTrackerSync(EntityDataAccessor<?> data, CallbackInfo ci);
    void web_1_21_6_7$writeCaveSpiderCustomData(ValueOutput nbt);
    void web_1_21_6_7$readCaveSpiderCustomData(ValueInput nbt);
    AnimationState web_1_21_6_7$getCaveSpiderIdleAnimationState();
    AnimationState web_1_21_6_7$getCaveSpiderWalkingAnimationState();
    CaveSpiderState web_1_21_6_7$getCaveSpiderState();
    CaveSpiderState web_1_21_6_7$getPreviousState();
}