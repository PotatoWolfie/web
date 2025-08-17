package potatowolfie.web.interfaces;

import net.minecraft.entity.LivingEntity;

public interface WebSpiderInterface {
    void shootWeb(LivingEntity target);

    int getWebCooldown();
    void setWebCooldown(int cooldown);

    boolean hasShootWeb();
    void setHasShootWeb(boolean hasShot);

    boolean isInCombat();
    void setInCombat(boolean combat);

    int getCombatTimer();
    void setCombatTimer(int timer);

}