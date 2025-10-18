package potatowolfie.web.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.SpiderEntity;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import potatowolfie.web.advancement.BurnTheNestHandler;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.custom.BabySpiderEntity;

public class SpiderEggBlock extends Block {
    public static final BooleanProperty PREVENTED = BooleanProperty.of("prevented");
    public static final BooleanProperty SPAWNED_PROTECTORS = BooleanProperty.of("spawned_protectors");
    public static final IntProperty REMAINING_TIME = IntProperty.of("remaining_time", 0, 288);
    private static final int HATCH_TIME = 24000;
    private static final int MIN_SCALED_HATCH_TIME = 216;
    private static final int MAX_SCALED_HATCH_TIME = 288;
    private static final double PLAYER_RANGE = 50.0;
    private static final int PLAYER_CHECK_INTERVAL = 100;
    private static final int MAX_PROTECTOR_SPIDERS = 2;
    private static final double PROTECTOR_SPAWN_RANGE = 8.0;
    private static final double PROTECTOR_CHECK_RANGE = 16.0;
    private static final float SPIDER_WIDTH = 1.4F;
    private static final float SPIDER_HEIGHT = 0.5F;

    public SpiderEggBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(PREVENTED, false)
                .with(SPAWNED_PROTECTORS, false)
                .with(REMAINING_TIME, MAX_SCALED_HATCH_TIME));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PREVENTED, REMAINING_TIME, SPAWNED_PROTECTORS);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient()) {
            boolean shouldPrevent = shouldStartPrevented(world, pos);

            if (shouldPrevent && !state.get(PREVENTED)) {
                world.setBlockState(pos, state.with(PREVENTED, true), Block.NOTIFY_ALL);
                state = world.getBlockState(pos);
            }

            if (!state.get(PREVENTED)) {
                world.scheduleBlockTick(pos, this, PLAYER_CHECK_INTERVAL);
            }
        }
    }

    private boolean shouldStartPrevented(World world, BlockPos pos) {
        if (world.isClient()) return false;

        return world.getPlayers().stream()
                .filter(player -> player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= PLAYER_RANGE * PLAYER_RANGE)
                .anyMatch(player -> player instanceof ServerPlayerEntity serverPlayer &&
                        serverPlayer.interactionManager.getGameMode() == GameMode.SURVIVAL);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(PREVENTED)) {
            return;
        }

        int remainingTime = state.get(REMAINING_TIME);
        boolean spawnedProtectors = state.get(SPAWNED_PROTECTORS);

        if (remainingTime == MAX_SCALED_HATCH_TIME) {
            int randomHatchTime = MIN_SCALED_HATCH_TIME + random.nextInt(MAX_SCALED_HATCH_TIME - MIN_SCALED_HATCH_TIME + 1);
            world.setBlockState(pos, state.with(REMAINING_TIME, randomHatchTime), Block.NOTIFY_ALL);
            state = world.getBlockState(pos);
            remainingTime = randomHatchTime;
        }

        boolean playerNearby = world.getPlayers().stream()
                .anyMatch(player -> player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= PLAYER_RANGE * PLAYER_RANGE);

        if (playerNearby && !spawnedProtectors && random.nextFloat() < 0.65f) {
            if (spawnProtectorSpiders(world, pos, random)) {
                world.setBlockState(pos, state.with(SPAWNED_PROTECTORS, true), Block.NOTIFY_ALL);
                state = world.getBlockState(pos);
            }
        }

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

    private boolean spawnProtectorSpiders(ServerWorld world, BlockPos eggPos, Random random) {
        Box searchBox = Box.of(
                eggPos.toCenterPos(),
                PROTECTOR_CHECK_RANGE * 2,
                PROTECTOR_CHECK_RANGE * 2,
                PROTECTOR_CHECK_RANGE * 2
        );

        long nearbySpiders = world.getEntitiesByClass(SpiderEntity.class, searchBox, spider -> true).size();

        if (nearbySpiders >= MAX_PROTECTOR_SPIDERS) {
            return false;
        }

        int spidersToSpawn = Math.min(MAX_PROTECTOR_SPIDERS - (int)nearbySpiders, MAX_PROTECTOR_SPIDERS);
        int spawnedCount = 0;

        for (int i = 0; i < spidersToSpawn; i++) {
            BlockPos spawnPos = findValidSpawnPos(world, eggPos, random);
            if (spawnPos != null) {
                SpiderEntity protectorSpider = new SpiderEntity(EntityType.SPIDER, world);
                protectorSpider.refreshPositionAndAngles(
                        spawnPos.getX() + 0.5,
                        spawnPos.getY(),
                        spawnPos.getZ() + 0.5,
                        random.nextFloat() * 360.0F,
                        0.0F
                );

                protectorSpider.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.NATURAL, null);

                if (world.spawnEntity(protectorSpider)) {
                    spawnedCount++;
                    world.playSound(null, spawnPos, SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.HOSTILE, 0.5F, 0.8F);
                }
            }
        }

        return spawnedCount > 0;
    }

    private BlockPos findValidSpawnPos(ServerWorld world, BlockPos center, Random random) {
        for (int attempts = 0; attempts < 10; attempts++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 3.0 + random.nextDouble() * (PROTECTOR_SPAWN_RANGE - 3.0);

            int x = center.getX() + (int)(Math.cos(angle) * distance);
            int z = center.getZ() + (int)(Math.sin(angle) * distance);

            for (int y = center.getY() + 3; y >= center.getY() - 3; y--) {
                BlockPos candidatePos = new BlockPos(x, y, z);

                if (hasEnoughSpaceForSpider(world, candidatePos)) {
                    if (SpawnRestriction.canSpawn(EntityType.SPIDER, world, SpawnReason.NATURAL, candidatePos, random)) {
                        return candidatePos;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasEnoughSpaceForSpider(ServerWorld world, BlockPos pos) {
        BlockPos groundPos = pos.down();

        if (!world.getBlockState(groundPos).isSolidBlock(world, groundPos)) {
            return false;
        }

        if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir()) {
            return false;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos adjacentPos = pos.add(dx, 0, dz);
                BlockState adjacentState = world.getBlockState(adjacentPos);

                if (!adjacentState.isAir() && adjacentState.isSolidBlock(world, adjacentPos)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.isClient() && !state.get(PREVENTED) && !world.getBlockTickScheduler().isQueued(pos, this)) {
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
            if (!world.isClient()) {
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