package potatowolfie.web.interfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface WebSpiderInterface {
    void shootWeb(LivingEntity target);

    @Unique
    void onTrackedDataSet(TrackedData<?> data, CallbackInfo ci);

    int getWebCooldown();
    void setWebCooldown(int cooldown);
    boolean hasShootWeb();
    void setHasShootWeb(boolean hasShot);
    boolean isInCombat();
    void setInCombat(boolean combat);
    int getCombatTimer();
    void setCombatTimer(int timer);

    boolean isShootingWeb();
    int getShootAnimationTicks();
}