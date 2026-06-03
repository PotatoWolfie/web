package potatowolfie.web.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.custom.BabySpiderEntity;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;

@Mixin(SpawnEggItem.class)
public abstract class SpawnEggItemMixin {

    @Inject(method = "spawnOffspringFromSpawnEgg", at = @At("HEAD"), cancellable = true)
    private static void web$spawnBaby(Player player, Mob parent, EntityType<? extends Mob> type, ServerLevel level, Vec3 pos, ItemStack spawnEggStack, CallbackInfoReturnable<Optional<Mob>> cir) {

        if (parent instanceof Spider && spawnEggStack.getItem() == Items.SPIDER_SPAWN_EGG) {
            BabySpiderEntity baby = new BabySpiderEntity(WebEntities.BABY_SPIDER, level);
            baby.snapTo(parent.getX(), parent.getY(), parent.getZ(), level.getRandom().nextFloat() * 360.0F, 0.0F);

            baby.finalizeSpawn(level, level.getCurrentDifficultyAt(parent.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);

            level.addFreshEntity(baby);

            if (!player.getAbilities().instabuild) {
                spawnEggStack.shrink(1);
            }

            cir.setReturnValue(Optional.of(baby));
        }
    }

}
