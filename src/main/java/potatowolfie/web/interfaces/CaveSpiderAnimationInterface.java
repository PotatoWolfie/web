package potatowolfie.web.interfaces;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.enums.CaveSpiderState;

public interface CaveSpiderAnimationInterface {
    void web_1_21_6_7$onCaveSpiderDataTrackerSync(TrackedData<?> data, CallbackInfo ci);
    void web_1_21_6_7$writeCaveSpiderCustomData(WriteView nbt);
    void web_1_21_6_7$readCaveSpiderCustomData(ReadView nbt);
    AnimationState web_1_21_6_7$getCaveSpiderIdleAnimationState();
    AnimationState web_1_21_6_7$getCaveSpiderWalkingAnimationState();
    CaveSpiderState web_1_21_6_7$getCaveSpiderState();
    CaveSpiderState web_1_21_6_7$getPreviousState();
}