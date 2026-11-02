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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import potatowolfie.web.advancement.BurnTheNestHandler;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.baby_spider.BabySpiderEntity;
import potatowolfie.web.particle.WebParticles;

public class SpiderEggBlock extends Block {
    public static final EnumProperty<EggStage> STAGE = EnumProperty.create("stage", EggStage.class);
    public static final BooleanProperty PREVENTED = BooleanProperty.create("prevented");
    public static final BooleanProperty SPAWNED_PROTECTORS = BooleanProperty.create("spawned_protectors");
    public static final IntegerProperty REMAINING_TIME = IntegerProperty.create("remaining_time", 0, 28800);
    public static final BooleanProperty COVERED = BooleanProperty.create("covered");

    private static final double ACTIVATION_RANGE = 7.0;
    private static final int OLD_STAGE_CHECK_INTERVAL = 20;

    private static final int MIN_HATCH_TICKS = 300;
    private static final int MAX_HATCH_TICKS = 400;

    private static final int MIN_DEFAULT_HATCH_TICKS = 18 * 60 * 20;
    private static final int MAX_DEFAULT_HATCH_TICKS = 24 * 60 * 20;

    private static final int HATCH_TICK_INTERVAL = 20;

    private static final double PLAYER_RANGE = 50.0;
    private static final int MAX_PROTECTOR_SPIDERS = 2;
    private static final double PROTECTOR_SPAWN_RANGE = 8.0;
    private static final double PROTECTOR_CHECK_RANGE = 16.0;

    private static final int BABY_SPIDER_RESTRICTION_RADIUS = 12;

    public SpiderEggBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(STAGE, EggStage.ACTIVE)
                .setValue(PREVENTED, false)
                .setValue(SPAWNED_PROTECTORS, false)
                .setValue(REMAINING_TIME, 0)
                .setValue(COVERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, PREVENTED, REMAINING_TIME, SPAWNED_PROTECTORS, COVERED);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClientSide()) {
            if (oldState.isAir() || oldState.getBlock() != this) {
                if (state.getValue(STAGE) == EggStage.OLD) {
                    boolean shouldPrevent = shouldStartPrevented(world, pos);

                    if (shouldPrevent && !state.getValue(PREVENTED)) {
                        world.setBlock(pos, state.setValue(PREVENTED, true), Block.UPDATE_ALL);
                        state = world.getBlockState(pos);
                    }

                    if (!state.getValue(PREVENTED)) {
                        world.scheduleTick(pos, this, OLD_STAGE_CHECK_INTERVAL);
                    }
                } else if (state.getValue(STAGE) == EggStage.ACTIVE) {
                    if (state.getValue(REMAINING_TIME) <= 0) {
                        int hatchTicks = MIN_DEFAULT_HATCH_TICKS + world.getRandom().nextInt(MAX_DEFAULT_HATCH_TICKS - MIN_DEFAULT_HATCH_TICKS + 1);
                        world.setBlock(pos, state.setValue(REMAINING_TIME, hatchTicks), Block.UPDATE_ALL);
                    }
                    world.scheduleTick(pos, this, HATCH_TICK_INTERVAL);
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
        switch (state.getValue(STAGE)) {
            case OLD -> tickOldStage(state, world, pos, random);
            case ACTIVE -> tickActiveStage(state, world, pos, random);
            case DECAYED -> {
            }
        }
    }

    private void tickOldStage(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(PREVENTED)) {
            return;
        }

        boolean playerClose = world.players().stream()
                .anyMatch(player -> player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= ACTIVATION_RANGE * ACTIVATION_RANGE);

        if (playerClose) {
            activateEgg(world, pos, state, random);
        } else {
            world.scheduleTick(pos, this, OLD_STAGE_CHECK_INTERVAL);
        }
    }

    private void activateEgg(ServerLevel world, BlockPos pos, BlockState state, RandomSource random) {
        spawnWebBurstParticles(world, pos);
        world.playSound(null, pos, SoundEvents.SPIDER_AMBIENT, SoundSource.BLOCKS, 0.6F, 1.4F);

        int hatchTicks = MIN_HATCH_TICKS + random.nextInt(MAX_HATCH_TICKS - MIN_HATCH_TICKS + 1);

        BlockState activeState = state.setValue(STAGE, EggStage.ACTIVE).setValue(REMAINING_TIME, hatchTicks);
        world.setBlock(pos, activeState, Block.UPDATE_ALL);
        world.scheduleTick(pos, this, HATCH_TICK_INTERVAL);
    }

    private void tickActiveStage(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        boolean playerNearby = world.players().stream()
                .anyMatch(player -> player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= PLAYER_RANGE * PLAYER_RANGE);

        if (!playerNearby) {
            world.scheduleTick(pos, this, HATCH_TICK_INTERVAL);
            return;
        }

        int remainingTime = state.getValue(REMAINING_TIME);
        boolean spawnedProtectors = state.getValue(SPAWNED_PROTECTORS);

        if (!spawnedProtectors && random.nextFloat() < 0.65f) {
            if (spawnProtectorSpiders(world, pos, random)) {
                world.setBlock(pos, state.setValue(SPAWNED_PROTECTORS, true), Block.UPDATE_ALL);
                state = world.getBlockState(pos);
            }
        }

        int newRemainingTime = remainingTime - HATCH_TICK_INTERVAL;

        if (newRemainingTime <= 0) {
            hatchEgg(world, pos, state);
        } else {
            world.setBlock(pos, state.setValue(REMAINING_TIME, newRemainingTime), Block.UPDATE_ALL);
            world.scheduleTick(pos, this, HATCH_TICK_INTERVAL);
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

        int spidersToSpawn = Math.min(MAX_PROTECTOR_SPIDERS - (int) nearbySpiders, MAX_PROTECTOR_SPIDERS);
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

            int x = center.getX() + (int) (Math.cos(angle) * distance);
            int z = center.getZ() + (int) (Math.sin(angle) * distance);

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
        if (world.isClientSide() || !isRandomlyTicking(state)) {
            return;
        }

        if (!world.getBlockTicks().hasScheduledTick(pos, this)) {
            int interval = state.getValue(STAGE) == EggStage.OLD ? OLD_STAGE_CHECK_INTERVAL : HATCH_TICK_INTERVAL;
            world.scheduleTick(pos, this, interval);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        EggStage stage = state.getValue(STAGE);

        if (stage == EggStage.DECAYED) {
            return false;
        }
        if (stage == EggStage.OLD && state.getValue(PREVENTED)) {
            return false;
        }
        return true;
    }

    private void hatchEgg(ServerLevel world, BlockPos pos, BlockState state) {
        BabySpiderEntity babySpider = new BabySpiderEntity(WebEntities.BABY_SPIDER, world);
        babySpider.snapTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0F, 0.0F);
        babySpider.setHomeTo(pos, BABY_SPIDER_RESTRICTION_RADIUS);

        world.addFreshEntity(babySpider);
        world.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 1.0F, 1.0F);
        spawnEggPopParticles(world, pos);

        BlockState decayedState = state.setValue(STAGE, EggStage.DECAYED);
        world.setBlock(pos, decayedState, Block.UPDATE_ALL);
    }

    private void spawnWebBurstParticles(ServerLevel world, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.3;
        double z = pos.getZ() + 0.5;
        world.sendParticles(WebParticles.WEB_BURST, x, y, z, 20, 0.4, 0.3, 0.4, 0.02);
    }

    private void spawnEggPopParticles(ServerLevel world, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.4;
        double z = pos.getZ() + 0.5;
        world.sendParticles(WebParticles.SPIDER_EGG_POP, x, y, z, 25, 0.35, 0.35, 0.35, 0.03);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        EggStage stage = state.getValue(STAGE);

        if (stack.getItem() instanceof FlintAndSteelItem && stage != EggStage.DECAYED) {
            if (!world.isClientSide()) {
                BlockState decayedState = state.setValue(STAGE, EggStage.DECAYED);
                world.setBlock(pos, decayedState, Block.UPDATE_ALL);
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

        if (stage == EggStage.DECAYED && stack.is(WebBlocks.SPIDER_EGG_SHELLS.asItem())) {
            if (player.isSecondaryUseActive()) {
                return InteractionResult.PASS;
            }

            if (state.getValue(COVERED)) {
                return InteractionResult.PASS;
            }

            if (!world.isClientSide()) {
                world.setBlock(pos, state.setValue(COVERED, true), Block.UPDATE_ALL);
                world.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}