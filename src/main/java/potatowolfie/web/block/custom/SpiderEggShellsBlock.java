package potatowolfie.web.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class SpiderEggShellsBlock extends MultifaceGrowthBlock {
    public static final BooleanProperty TEXTURE_VARIANT = BooleanProperty.of("texture_variant");
    public static final BooleanProperty INITIALIZED = BooleanProperty.of("initialized");

    private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape UP_SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public SpiderEggShellsBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(TEXTURE_VARIANT, false)
                .with(INITIALIZED, false));
    }

    public static final MapCodec<SpiderEggShellsBlock> CODEC = createCodec(SpiderEggShellsBlock::new);

    @Override
    public MapCodec<SpiderEggShellsBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TEXTURE_VARIANT, INITIALIZED);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = VoxelShapes.empty();

        if (state.get(Properties.DOWN)) {
            shape = VoxelShapes.union(shape, DOWN_SHAPE);
        }
        if (state.get(Properties.UP)) {
            shape = VoxelShapes.union(shape, UP_SHAPE);
        }
        if (state.get(Properties.NORTH)) {
            shape = VoxelShapes.union(shape, NORTH_SHAPE);
        }
        if (state.get(Properties.SOUTH)) {
            shape = VoxelShapes.union(shape, SOUTH_SHAPE);
        }
        if (state.get(Properties.WEST)) {
            shape = VoxelShapes.union(shape, WEST_SHAPE);
        }
        if (state.get(Properties.EAST)) {
            shape = VoxelShapes.union(shape, EAST_SHAPE);
        }

        return shape.isEmpty() ? DOWN_SHAPE : shape;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (ctx == null) {
            return this.getDefaultState().with(Properties.DOWN, true).with(TEXTURE_VARIANT, false).with(INITIALIZED, false);
        }

        BlockPos pos = ctx.getBlockPos();
        BlockState existingState = ctx.getWorld().getBlockState(pos);
        BlockState state;

        if (existingState.getBlock() == this) {
            state = existingState;
            Direction clickedFace = ctx.getSide().getOpposite();
            BooleanProperty property = getDirectionProperty(clickedFace);

            if (property != null && !state.get(property) && this.canPlaceOn(ctx.getWorld(), pos, clickedFace)) {
                state = state.with(property, true);
                return state;
            } else {
                for (Direction direction : Direction.values()) {
                    BooleanProperty fallbackProperty = getDirectionProperty(direction);
                    if (fallbackProperty != null && !state.get(fallbackProperty) && this.canPlaceOn(ctx.getWorld(), pos, direction)) {
                        state = state.with(fallbackProperty, true);
                        return state;
                    }
                }
                return null;
            }
        } else {
            state = this.getDefaultState().with(INITIALIZED, false);
        }

        Direction clickedFace = ctx.getSide().getOpposite();
        BooleanProperty property = getDirectionProperty(clickedFace);

        if (property != null && this.canPlaceOn(ctx.getWorld(), pos, clickedFace)) {
            state = state.with(property, true);
        } else {
            if (this.canPlaceOn(ctx.getWorld(), pos, Direction.DOWN)) {
                state = state.with(Properties.DOWN, true);
            } else {
                boolean foundValidFace = false;
                for (Direction direction : Direction.values()) {
                    BooleanProperty fallbackProperty = getDirectionProperty(direction);
                    if (fallbackProperty != null && this.canPlaceOn(ctx.getWorld(), pos, direction)) {
                        state = state.with(fallbackProperty, true);
                        foundValidFace = true;
                        break;
                    }
                }
                if (!foundValidFace) {
                    return null;
                }
            }
        }

        return state;
    }

    protected static boolean hasAnyDirection(BlockState state) {
        return state.get(Properties.NORTH) || state.get(Properties.SOUTH) || state.get(Properties.EAST) ||
                state.get(Properties.WEST) || state.get(Properties.UP) || state.get(Properties.DOWN);
    }

    private boolean canPlaceOn(WorldView world, BlockPos pos, Direction direction) {
        BlockPos adjacentPos = pos.offset(direction);
        BlockState adjacentState = world.getBlockState(adjacentPos);
        return adjacentState.isSideSolidFullSquare(world, adjacentPos, direction.getOpposite());
    }

    private BooleanProperty getDirectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> Properties.NORTH;
            case SOUTH -> Properties.SOUTH;
            case EAST -> Properties.EAST;
            case WEST -> Properties.WEST;
            case UP -> Properties.UP;
            case DOWN -> Properties.DOWN;
        };
    }

    private boolean isWaterAdjacent(WorldView world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.offset(direction);
            BlockState adjacentState = world.getBlockState(adjacentPos);

            if (adjacentState.isOf(Blocks.WATER) || adjacentState.getBlock() instanceof FluidBlock) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> state.with(Properties.NORTH, state.get(Properties.SOUTH))
                    .with(Properties.EAST, state.get(Properties.WEST))
                    .with(Properties.SOUTH, state.get(Properties.NORTH))
                    .with(Properties.WEST, state.get(Properties.EAST));
            case COUNTERCLOCKWISE_90 -> state.with(Properties.NORTH, state.get(Properties.EAST))
                    .with(Properties.EAST, state.get(Properties.SOUTH))
                    .with(Properties.SOUTH, state.get(Properties.WEST))
                    .with(Properties.WEST, state.get(Properties.NORTH));
            case CLOCKWISE_90 -> state.with(Properties.NORTH, state.get(Properties.WEST))
                    .with(Properties.EAST, state.get(Properties.NORTH))
                    .with(Properties.SOUTH, state.get(Properties.EAST))
                    .with(Properties.WEST, state.get(Properties.SOUTH));
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> state.with(Properties.NORTH, state.get(Properties.SOUTH)).with(Properties.SOUTH, state.get(Properties.NORTH));
            case FRONT_BACK -> state.with(Properties.EAST, state.get(Properties.WEST)).with(Properties.WEST, state.get(Properties.EAST));
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (isWaterAdjacent(world, pos)) {
            return false;
        }

        for (Direction direction : Direction.values()) {
            BooleanProperty property = getDirectionProperty(direction);
            if (property != null && state.get(property)) {
                if (canPlaceOn(world, pos, direction)) {
                    return true;
                }
            }
        }
        return false;
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (neighborState.isOf(Blocks.WATER) || neighborState.getBlock() instanceof FluidBlock) {
            return Blocks.AIR.getDefaultState();
        }

        BooleanProperty property = getDirectionProperty(direction);
        if (property != null && state.get(property)) {
            if (!canPlaceOn(world, pos, direction)) {
                state = state.with(property, false);
            }
        }

        if (!this.hasAnyDirection(state)) {
            return Blocks.AIR.getDefaultState();
        }

        return state;
    }

    @Override
    public void onBlockAdded(BlockState state, net.minecraft.world.World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);

        if (isWaterAdjacent(world, pos)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            return;
        }

        if (!state.get(INITIALIZED)) {
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    public void scheduledTick(BlockState state, net.minecraft.server.world.ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (isWaterAdjacent(world, pos)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            return;
        }

        if (!state.get(INITIALIZED)) {
            boolean textureVariant = world.random.nextBoolean();
            BlockState newState = state.with(TEXTURE_VARIANT, textureVariant).with(INITIALIZED, true);
            world.setBlockState(pos, newState, Block.NOTIFY_ALL);
        }
    }

    public BlockState getInitializedState(BlockState state, net.minecraft.util.math.random.Random random) {
        if (!state.get(INITIALIZED)) {
            boolean textureVariant = random.nextBoolean();
            return state.with(TEXTURE_VARIANT, textureVariant).with(INITIALIZED, true);
        }
        return state;
    }

    @Override
    public MultifaceGrower getGrower() {
        return new MultifaceGrower(this) {
            @Override
            public boolean canGrow(BlockState state, BlockView world, BlockPos pos, Direction direction) {
                return false;
            }
        };
    }
}