package potatowolfie.web.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import potatowolfie.web.entity.custom.SpiderWebProjectileEntity;

public class SpiderWebItem extends BlockItem {
    private static final int COOLDOWN_TICKS = 60;
    private static final double SURVIVAL_REACH_DISTANCE = 3.0;
    private static final double CREATIVE_REACH_DISTANCE = 5.0;

    public SpiderWebItem(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);

        if (user.getCooldowns().isOnCooldown(itemStack)) {
            return InteractionResult.FAIL;
        }

        double reachDistance = user.getAbilities().instabuild ? CREATIVE_REACH_DISTANCE : SURVIVAL_REACH_DISTANCE;

        Vec3 start = user.getEyePosition();
        Vec3 direction = user.getLookAngle();
        Vec3 end = start.add(direction.scale(reachDistance));

        BlockHitResult hitResult = world.clip(new ClipContext(
                start, end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                user
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            double distanceToHit = hitResult.getLocation().distanceTo(start);
            if (distanceToHit <= reachDistance) {
                return this.tryPlaceWebBlock(world, user, hand, itemStack, hitResult);
            } else {
                return this.throwSpiderWeb(world, user, hand, itemStack);
            }
        } else {
            return this.throwSpiderWeb(world, user, hand, itemStack);
        }
    }

    private InteractionResult tryPlaceWebBlock(Level world, Player player, InteractionHand hand, ItemStack itemStack, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        Direction side = hitResult.getDirection();

        BlockPlaceContext context = new BlockPlaceContext(player, hand, itemStack, hitResult);
        InteractionResult result = this.place(context);

        if (result.consumesAction()) {
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }

    private InteractionResult throwSpiderWeb(Level world, Player player, InteractionHand hand, ItemStack itemStack) {
        if (!world.isClientSide()) {
            SpiderWebProjectileEntity projectile = new SpiderWebProjectileEntity(world, player);
            projectile.setPos(player.getEyePosition());
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);

            world.addFreshEntity(projectile);

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
                    0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

            player.getCooldowns().addCooldown(itemStack, COOLDOWN_TICKS);

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        return true;
    }
}