package potatowolfie.web.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.tags.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class SpiderFireDamageMixin {

    @Unique
    private static final float SPIDER_FIRE_DAMAGE_MULTIPLIER = 2.0F;

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
    private float web_1_21_6_7$amplifySpiderFireDamage(float amount, ServerLevel level, DamageSource source) {
        if ((Object) this instanceof Spider && source.is(DamageTypeTags.IS_FIRE)) {
            return amount * SPIDER_FIRE_DAMAGE_MULTIPLIER;
        }
        return amount;
    }
}