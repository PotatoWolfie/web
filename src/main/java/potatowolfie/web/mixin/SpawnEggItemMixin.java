package potatowolfie.web.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.custom.BabySpiderEntity;

import java.util.Optional;

@Mixin(SpawnEggItem.class)
public abstract class SpawnEggItemMixin {

    @Inject(method = "spawnBaby", at = @At("HEAD"), cancellable = true)
    private void web$spawnBaby(PlayerEntity user, MobEntity entity, EntityType<? extends MobEntity> entityType, ServerWorld world, Vec3d pos, ItemStack stack, CallbackInfoReturnable<Optional<MobEntity>> cir) {
        if (entity instanceof SpiderEntity && stack.getItem() == Items.SPIDER_SPAWN_EGG) {
            BabySpiderEntity baby = new BabySpiderEntity(WebEntities.BABY_SPIDER, world);
            baby.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), world.getRandom().nextFloat() * 360.0F, 0.0F);

            baby.initialize(world, world.getLocalDifficulty(entity.getBlockPos()), SpawnReason.COMMAND, null);

            world.spawnEntity(baby);

            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }

            cir.setReturnValue(Optional.of(baby));
        }
    }

}