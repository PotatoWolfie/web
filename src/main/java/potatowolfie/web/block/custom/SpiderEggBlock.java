package potatowolfie.web.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import potatowolfie.web.advancement.BurnTheNestHandler;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.custom.BabySpiderEntity;

public class SpiderEggBlock extends Block {
    public static final BooleanProperty PREVENTED = BooleanProperty.create("prevented");
    public static final BooleanProperty SPAWNED_PROTECTORS = BooleanProperty.create("spawned_protectors");
    public static final IntegerProperty REMAINING_TIME = IntegerProperty.create("remaining_time", 0, 288);
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

    public SpiderEggBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PREVENTED, false)
                .setValue(SPAWNED_PROTECTORS, false)
                .setValue(REMAINING_TIME, MAX_SCALED_HATCH_TIME));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PREVENTED, REMAINING_TIME, SPAWNED_PROTECTORS);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClientSide()) {
            if (oldState.isAir() || oldState.getBlock() != this) {
                boolean shouldPrevent = shouldStartPrevented(world, pos);

                if (shouldPrevent && !state.getValue(PREVENTED)) {
                    world.setBlock(pos, state.setValue(PREVENTED, true), Block.UPDATE_ALL);
                    state = world.getBlockState(pos);
                }

                if (!state.getValue(PREVENTED)) {
                    world.scheduleTick(pos, this, PLAYER_CHECK_INTERVAL);
                }
            }
        }
    }
    private boolean shouldStartPrevented(Level world, BlockPos pos) {
        if (world.isClientSide()) return false;

        return world.players().stream()
                .filter(player -> player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= PLAYER_RANGE * PLAYER_RANGE)
                .anyMatch(player -> player instanceof ServerPlayer serverPlayer &&
                        serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(PREVENTED)) {
            return;
        }

        int remainingTime = state.getValue(REMAINING_TIME);
        boolean spawnedProtectors = state.getValue(SPAWNED_PROTECTORS);

        if (remainingTime == MAX_SCALED_HATCH_TIME) {
            int randomHatchTime = MIN_SCALED_HATCH_TIME + random.nextInt(MAX_SCALED_HATCH_TIME - MIN_SCALED_HATCH_TIME + 1);
            world.setBlock(pos, state.setValue(REMAINING_TIME, randomHatchTime), Block.UPDATE_ALL);
            state = world.getBlockState(pos);
            remainingTime = randomHatchTime;
        }

        boolean playerNearby = world.players().stream()
                .anyMatch(player -> player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= PLAYER_RANGE * PLAYER_RANGE);

        if (playerNearby && !spawnedProtectors && random.nextFloat() < 0.65f) {
            if (spawnProtectorSpiders(world, pos, random)) {
                world.setBlock(pos, state.setValue(SPAWNED_PROTECTORS, true), Block.UPDATE_ALL);
                state = world.getBlockState(pos);
            }
        }

        if (playerNearby) {
            int newRemainingTime = remainingTime - 1;

            if (newRemainingTime <= 0) {
                hatchEgg(world, pos);
                return;
            } else {
                world.setBlock(pos, state.setValue(REMAINING_TIME, newRemainingTime), Block.UPDATE_ALL);
                world.scheduleTick(pos, this, PLAYER_CHECK_INTERVAL);
            }
        } else {
            world.scheduleTick(pos, this, PLAYER_CHECK_INTERVAL);
        }
    }

    private boolean spawnProtectorSpiders(ServerLevel world, BlockPos eggPos, RandomSource random) {
        Vec3 center = Vec3.atCenterOf(eggPos);
        AABB searchBox = AABB.ofSize(
                center,
                PROTECTOR_CHECK_RANGE * 2,
                PROTECTOR_CHECK_RANGE * 2,
                PROTECTOR_CHECK_RANGE * 2
        );

        long nearbySpiders = world.getEntitiesOfClass(Spider.class, searchBox, spider -> true).size();

        if (nearbySpiders >= MAX_PROTECTOR_SPIDERS) {
            return false;
        }

        int spidersToSpawn = Math.min(MAX_PROTECTOR_SPIDERS - (int)nearbySpiders, MAX_PROTECTOR_SPIDERS);
        int spawnedCount = 0;

        for (int i = 0; i < spidersToSpawn; i++) {
            BlockPos spawnPos = findValidSpawnPos(world, eggPos, random);
            if (spawnPos != null) {
                Spider protectorSpider = new Spider(EntityTypes.SPIDER, world);
                protectorSpider.snapTo(
                        spawnPos.getX() + 0.5,
                        spawnPos.getY(),
                        spawnPos.getZ() + 0.5,
                        random.nextFloat() * 360.0F,
                        0.0F
                );

                protectorSpider.finalizeSpawn(world, world.getCurrentDifficultyAt(spawnPos), EntitySpawnReason.NATURAL, null);

                if (world.addFreshEntity(protectorSpider)) {
                    spawnedCount++;
                    world.playSound(null, spawnPos, SoundEvents.SPIDER_AMBIENT, SoundSource.HOSTILE, 0.5F, 0.8F);
                }
            }
        }

        return spawnedCount > 0;
    }

    private BlockPos findValidSpawnPos(ServerLevel world, BlockPos center, RandomSource random) {
        for (int attempts = 0; attempts < 10; attempts++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 3.0 + random.nextDouble() * (PROTECTOR_SPAWN_RANGE - 3.0);

            int x = center.getX() + (int)(Math.cos(angle) * distance);
            int z = center.getZ() + (int)(Math.sin(angle) * distance);

            for (int y = center.getY() + 3; y >= center.getY() - 3; y--) {
                BlockPos candidatePos = new BlockPos(x, y, z);

                if (hasEnoughSpaceForSpider(world, candidatePos)) {
                    if (SpawnPlacements.checkSpawnRules(EntityTypes.SPIDER, world, EntitySpawnReason.NATURAL, candidatePos, random)) {
                        return candidatePos;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasEnoughSpaceForSpider(ServerLevel world, BlockPos pos) {
        BlockPos groundPos = pos.below();

        if (!world.getBlockState(groundPos).isRedstoneConductor(world, groundPos)) {
            return false;
        }

        if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.above()).isAir()) {
            return false;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos adjacentPos = pos.offset(dx, 0, dz);
                BlockState adjacentState = world.getBlockState(adjacentPos);

                if (!adjacentState.isAir() && adjacentState.isRedstoneConductor(world, adjacentPos)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!world.isClientSide() && !state.getValue(PREVENTED) && !world.getBlockTicks().hasScheduledTick(pos, this)) {
            world.scheduleTick(pos, this, PLAYER_CHECK_INTERVAL);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(PREVENTED);
    }

    private void hatchEgg(ServerLevel world, BlockPos pos) {
        BabySpiderEntity babySpider = new BabySpiderEntity(WebEntities.BABY_SPIDER, world);
        babySpider.snapTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0F, 0.0F);

        world.addFreshEntity(babySpider);
        world.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 1.0F, 1.0F);
        world.removeBlock(pos, false);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof FlintAndSteelItem && !state.getValue(PREVENTED)) {
            if (!world.isClientSide()) {
                world.setBlock(pos, state.setValue(PREVENTED, true), Block.UPDATE_ALL);
                world.playSound(null, pos, SoundEvents.SPIDER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (player instanceof ServerPlayer serverPlayer) {
                    BurnTheNestHandler.grantBurnTheNestAdvancement(serverPlayer);
                }

                if (stack.isDamageableItem()) {
                    stack.hurtWithoutBreaking(1, player);
                } else {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}