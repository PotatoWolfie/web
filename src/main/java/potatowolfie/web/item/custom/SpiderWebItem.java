package potatowolfie.web.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import potatowolfie.web.entity.custom.SpiderWebProjectileEntity;

public class SpiderWebItem extends BlockItem {
    private static final int COOLDOWN_TICKS = 60;
    private static final double SURVIVAL_REACH_DISTANCE = 3.0;
    private static final double CREATIVE_REACH_DISTANCE = 5.0;

    public SpiderWebItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(itemStack)) {
            return ActionResult.FAIL;
        }

        double reachDistance = user.getAbilities().creativeMode ? CREATIVE_REACH_DISTANCE : SURVIVAL_REACH_DISTANCE;

        Vec3d start = user.getEyePos();
        Vec3d direction = user.getRotationVector();
        Vec3d end = start.add(direction.multiply(reachDistance));

        BlockHitResult hitResult = world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                user
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            double distanceToHit = hitResult.getPos().distanceTo(start);
            if (distanceToHit <= reachDistance) {
                return this.tryPlaceWebBlock(world, user, hand, itemStack, hitResult);
            } else {
                return this.throwSpiderWeb(world, user, hand, itemStack);
            }
        } else {
            return this.throwSpiderWeb(world, user, hand, itemStack);
        }
    }

    private ActionResult tryPlaceWebBlock(World world, PlayerEntity player, Hand hand, ItemStack itemStack, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        Direction side = hitResult.getSide();

        ItemPlacementContext context = new ItemPlacementContext(player, hand, itemStack, hitResult);
        ActionResult result = this.place(context);

        if (result.isAccepted()) {
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.FAIL;
        }
    }

    private ActionResult throwSpiderWeb(World world, PlayerEntity player, Hand hand, ItemStack itemStack) {
        if (!world.isClient()) {
            SpiderWebProjectileEntity projectile = new SpiderWebProjectileEntity(world, player);
            projectile.setPosition(player.getEyePos());
            projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 1.5F, 1.0F);

            world.spawnEntity(projectile);

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL,
                    0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

            player.getItemCooldownManager().set(itemStack, COOLDOWN_TICKS);

            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return super.useOnBlock(context);
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        return true;
    }
}