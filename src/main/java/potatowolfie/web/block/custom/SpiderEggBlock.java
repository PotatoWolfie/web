package potatowolfie.web.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import potatowolfie.web.advancement.BurnTheNestHandler;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.custom.BabySpiderEntity;

public class SpiderEggBlock extends Block {
    public static final BooleanProperty PREVENTED = BooleanProperty.of("prevented");
    public static final IntProperty REMAINING_TIME = IntProperty.of("remaining_time", 0, 240);
    private static final int HATCH_TIME = 24000;
    private static final int SCALED_HATCH_TIME = 240;
    private static final double PLAYER_RANGE = 50.0;
    private static final int PLAYER_CHECK_INTERVAL = 100;

    public SpiderEggBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(PREVENTED, false)
                .with(REMAINING_TIME, SCALED_HATCH_TIME));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PREVENTED, REMAINING_TIME);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient && !state.get(PREVENTED)) {
            world.scheduleBlockTick(pos, this, PLAYER_CHECK_INTERVAL);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(PREVENTED)) {
            return;
        }

        int remainingTime = state.get(REMAINING_TIME);

        boolean playerNearby = world.getPlayers().stream()
                .anyMatch(player -> player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= PLAYER_RANGE * PLAYER_RANGE);

        if (playerNearby) {
            int newRemainingTime = remainingTime - 1;

            if (newRemainingTime <= 0) {
                hatchEgg(world, pos);
                return;
            } else {
                world.setBlockState(pos, state.with(REMAINING_TIME, newRemainingTime), Block.NOTIFY_ALL);
                world.scheduleBlockTick(pos, this, PLAYER_CHECK_INTERVAL);
            }
        } else {
            world.scheduleBlockTick(pos, this, PLAYER_CHECK_INTERVAL);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.isClient && !state.get(PREVENTED) && !world.getBlockTickScheduler().isQueued(pos, this)) {
            world.scheduleBlockTick(pos, this, PLAYER_CHECK_INTERVAL);
        }
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return !state.get(PREVENTED);
    }

    private void hatchEgg(ServerWorld world, BlockPos pos) {
        BabySpiderEntity babySpider = new BabySpiderEntity(WebEntities.BABY_SPIDER, world);
        babySpider.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0F, 0.0F);

        world.spawnEntity(babySpider);
        world.playSound(null, pos, SoundEvents.BLOCK_SNIFFER_EGG_HATCH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.removeBlock(pos, false);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof FlintAndSteelItem && !state.get(PREVENTED)) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(PREVENTED, true), Block.NOTIFY_ALL);
                world.playSound(null, pos, SoundEvents.ENTITY_SPIDER_DEATH, SoundCategory.BLOCKS, 1.0F, 1.0F);

                if (player instanceof ServerPlayerEntity serverPlayer) {
                    BurnTheNestHandler.grantBurnTheNestAdvancement(serverPlayer);
                }

                if (stack.isDamageable()) {
                    stack.damage(1, player);
                } else {
                    stack.decrement(1);
                }
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}