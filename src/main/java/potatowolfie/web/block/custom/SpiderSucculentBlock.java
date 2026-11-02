package potatowolfie.web.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import potatowolfie.web.particle.WebParticles;

import java.util.List;

public class SpiderSucculentBlock extends Block implements BonemealableBlock {

    public enum Part implements StringRepresentable {
        ROOT("root"),
        TOP("top"),
        BLOSSOM("blossom");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final IntegerProperty TARGET_ROOTS = IntegerProperty.create("target_roots", 0, 3);

    private static final VoxelShape ROOT_SHAPE = Block.column(14.0, 0.0, 16.0);
    private static final VoxelShape TOP_SHAPE = Block.column(14.0, 0.0, 14.0);

    private static final double DETECT_RANGE_HORIZONTAL = 3.0;
    private static final double DETECT_RANGE_UP = 3.0;
    private static final double DETECT_RANGE_DOWN = 7.0;
    private static final int CHECK_INTERVAL = 10;
    private static final int SLOWNESS_TICKS = 160;
    private static final int BURST_COOLDOWN = 1400;
    private static final int REGROW_CHANCE = 30;

    public SpiderSucculentBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PART, Part.TOP)
                .setValue(TARGET_ROOTS, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, TARGET_ROOTS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockState below = level.getBlockState(ctx.getClickedPos().below());

        int target = below.is(this)
                ? below.getValue(TARGET_ROOTS)
                : level.getRandom().nextInt(4);

        return this.defaultBlockState().setValue(TARGET_ROOTS, target);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        return (below.is(this) || below.is(net.minecraft.tags.BlockTags.SUPPORTS_VEGETATION))
                && !level.getBlockState(pos.above()).liquid();
    }

    private boolean isHeadlessRoot(BlockState state, LevelReader level, BlockPos pos) {
        return state.is(this)
                && state.getValue(PART) == Part.ROOT
                && !level.getBlockState(pos.above()).is(this);
    }

    private void growTopAbove(ServerLevel level, BlockPos rootPos, BlockState rootState) {
        BlockPos above = rootPos.above();
        if (!level.getBlockState(above).isAir()) return;

        level.setBlock(above, this.defaultBlockState()
                .setValue(PART, Part.TOP)
                .setValue(TARGET_ROOTS, rootState.getValue(TARGET_ROOTS)), Block.UPDATE_ALL);
        level.scheduleTick(above, this, CHECK_INTERVAL);
    }

    private BlockPos findColumnTop(LevelReader level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = pos.mutable();
        while (level.getBlockState(cursor.above()).is(this)) {
            cursor.move(Direction.UP);
        }
        return cursor.immutable();
    }

    private int countRootsBelow(LevelReader level, BlockPos top) {
        int roots = 0;
        BlockPos.MutableBlockPos cursor = top.mutable().move(Direction.DOWN);
        while (true) {
            BlockState s = level.getBlockState(cursor);
            if (s.is(this) && s.getValue(PART) == Part.ROOT) {
                roots++;
                cursor.move(Direction.DOWN);
            } else {
                break;
            }
        }
        return roots;
    }
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        BlockPos top = findColumnTop(level, pos);
        BlockState topState = level.getBlockState(top);

        if (topState.getValue(PART) == Part.BLOSSOM) {
            return false;
        }
        if (topState.getValue(PART) == Part.ROOT) {
            return level.getBlockState(top.above()).isAir();
        }

        int roots = countRootsBelow(level, top);
        if (roots < topState.getValue(TARGET_ROOTS)) {
            return level.getBlockState(top.above()).isAir();
        }
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        BlockPos top = findColumnTop(level, pos);
        BlockState topState = level.getBlockState(top);

        if (topState.getValue(PART) == Part.BLOSSOM) return;

        if (topState.getValue(PART) == Part.ROOT) {
            growTopAbove(level, top, topState);
            return;
        }

        int target = topState.getValue(TARGET_ROOTS);
        int roots = countRootsBelow(level, top);

        if (roots < target) {
            BlockPos above = top.above();
            if (!level.getBlockState(above).isAir()) return;
            level.setBlock(top, topState.setValue(PART, Part.ROOT), Block.UPDATE_ALL);
            level.setBlock(above, this.defaultBlockState()
                    .setValue(PART, Part.TOP)
                    .setValue(TARGET_ROOTS, target), Block.UPDATE_ALL);
            level.scheduleTick(above, this, CHECK_INTERVAL);
        } else {
            level.setBlock(top, topState.setValue(PART, Part.BLOSSOM), Block.UPDATE_ALL);
            level.scheduleTick(top, this, CHECK_INTERVAL);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, CHECK_INTERVAL);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, CHECK_INTERVAL);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }

        if (isHeadlessRoot(state, level, pos) && random.nextInt(REGROW_CHANCE) == 0) {
            growTopAbove(level, pos, state);
        }

        int nextDelay = CHECK_INTERVAL;

        if (state.getValue(PART) == Part.BLOSSOM) {
            Vec3 center = Vec3.atCenterOf(pos);
            AABB detectionBox = new AABB(
                    pos.getX() - DETECT_RANGE_HORIZONTAL,
                    pos.getY() - DETECT_RANGE_DOWN,
                    pos.getZ() - DETECT_RANGE_HORIZONTAL,
                    pos.getX() + 1 + DETECT_RANGE_HORIZONTAL,
                    pos.getY() + 1 + DETECT_RANGE_UP,
                    pos.getZ() + 1 + DETECT_RANGE_HORIZONTAL);
            List<Player> targets = level.getEntitiesOfClass(
                    Player.class,
                    detectionBox,
                    player -> !player.isSpectator() && player.isAlive());

            if (!targets.isEmpty()) {
                for (Player player : targets) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_TICKS, 1));
                }

                level.sendParticles(WebParticles.SPIDER_SUCCULENT_CLOUD,
                        center.x, pos.getY() + 1.0, center.z,
                        22, 0.15, 0.05, 0.15, 0.0);
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.9F);

                nextDelay = BURST_COOLDOWN;
            }
        }

        level.scheduleTick(pos, this, nextDelay);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide() && !state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction directionToNeighbor, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            tickAccess.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, level, tickAccess, pos, directionToNeighbor, neighborPos, neighborState, random);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PART) == Part.ROOT ? ROOT_SHAPE : TOP_SHAPE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}