package potatowolfie.web.mixin;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobEntityMixin {

    @Unique
    private double web$originalBaseDamage = -1.0;

    @Inject(method = "doHurtTarget", at = @At("HEAD"))
    private void nerfHardModeDamage(ServerLevel level, Entity target, CallbackInfoReturnable<Boolean> cir) {
        Mob entity = (Mob) (Object) this;

        if (entity instanceof Spider && entity.level().getDifficulty() == Difficulty.HARD) {
            AttributeInstance attackAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);

            if (attackAttribute != null) {
                this.web$originalBaseDamage = attackAttribute.getBaseValue();

                double adjustedBase = (this.web$originalBaseDamage + 0.5) / 1.5;

                attackAttribute.setBaseValue(adjustedBase);
            }
        }
    }

    @Inject(method = "doHurtTarget", at = @At("TAIL"))
    private void restoreNormalDamage(ServerLevel level, Entity target, CallbackInfoReturnable<Boolean> cir) {
        Mob entity = (Mob) (Object) this;

        if (entity instanceof Spider && this.web$originalBaseDamage != -1.0) {
            AttributeInstance attackAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackAttribute != null) {
                attackAttribute.setBaseValue(this.web$originalBaseDamage);
            }
            this.web$originalBaseDamage = -1.0;
        }
    }
}