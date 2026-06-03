package potatowolfie.web.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpiderEggShellsBlock extends MultifaceSpreadeableBlock {
    public static final BooleanProperty TEXTURE_VARIANT = BooleanProperty.create("texture_variant");
    public static final BooleanProperty INITIALIZED = BooleanProperty.create("initialized");

    private static final VoxelShape DOWN_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape UP_SHAPE = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape NORTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public SpiderEggShellsBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(TEXTURE_VARIANT, false)
                .setValue(INITIALIZED, false));
    }

    public static final MapCodec<SpiderEggShellsBlock> CODEC = simpleCodec(SpiderEggShellsBlock::new);

    @Override
    public MapCodec<SpiderEggShellsBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TEXTURE_VARIANT, INITIALIZED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();

        if (state.getValue(BlockStateProperties.DOWN)) {
            shape = Shapes.or(shape, DOWN_SHAPE);
        }
        if (state.getValue(BlockStateProperties.UP)) {
            shape = Shapes.or(shape, UP_SHAPE);
        }
        if (state.getValue(BlockStateProperties.NORTH)) {
            shape = Shapes.or(shape, NORTH_SHAPE);
        }
        if (state.getValue(BlockStateProperties.SOUTH)) {
            shape = Shapes.or(shape, SOUTH_SHAPE);
        }
        if (state.getValue(BlockStateProperties.WEST)) {
            shape = Shapes.or(shape, WEST_SHAPE);
        }
        if (state.getValue(BlockStateProperties.EAST)) {
            shape = Shapes.or(shape, EAST_SHAPE);
        }

        return shape.isEmpty() ? DOWN_SHAPE : shape;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        if (ctx == null) {
            return this.defaultBlockState().setValue(BlockStateProperties.DOWN, true).setValue(TEXTURE_VARIANT, false).setValue(INITIALIZED, false);
        }

        BlockPos pos = ctx.getClickedPos();
        BlockState existingState = ctx.getLevel().getBlockState(pos);
        BlockState state;

        if (existingState.getBlock() == this) {
            state = existingState;
            Direction clickedFace = ctx.getClickedFace().getOpposite();
            BooleanProperty property = getDirectionProperty(clickedFace);

            if (property != null && !state.getValue(property) && this.canPlaceOn(ctx.getLevel(), pos, clickedFace)) {
                state = state.setValue(property, true);
                return state;
            } else {
                for (Direction direction : Direction.values()) {
                    BooleanProperty fallbackProperty = getDirectionProperty(direction);
                    if (fallbackProperty != null && !state.getValue(fallbackProperty) && this.canPlaceOn(ctx.getLevel(), pos, direction)) {
                        state = state.setValue(fallbackProperty, true);
                        return state;
                    }
                }
                return null;
            }
        } else {
            state = this.defaultBlockState().setValue(INITIALIZED, false);
        }

        Direction clickedFace = ctx.getClickedFace().getOpposite();
        BooleanProperty property = getDirectionProperty(clickedFace);

        if (property != null && this.canPlaceOn(ctx.getLevel(), pos, clickedFace)) {
            state = state.setValue(property, true);
        } else {
            if (this.canPlaceOn(ctx.getLevel(), pos, Direction.DOWN)) {
                state = state.setValue(BlockStateProperties.DOWN, true);
            } else {
                boolean foundValidFace = false;
                for (Direction direction : Direction.values()) {
                    BooleanProperty fallbackProperty = getDirectionProperty(direction);
                    if (fallbackProperty != null && this.canPlaceOn(ctx.getLevel(), pos, direction)) {
                        state = state.setValue(fallbackProperty, true);
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

    protected static boolean hasAnyFace(BlockState state) {
        return state.getValue(BlockStateProperties.NORTH) || state.getValue(BlockStateProperties.SOUTH) || state.getValue(BlockStateProperties.EAST) ||
                state.getValue(BlockStateProperties.WEST) || state.getValue(BlockStateProperties.UP) || state.getValue(BlockStateProperties.DOWN);
    }

    private boolean canPlaceOn(LevelReader world, BlockPos pos, Direction direction) {
        BlockPos adjacentPos = pos.relative(direction);
        BlockState adjacentState = world.getBlockState(adjacentPos);
        return adjacentState.isFaceSturdy(world, adjacentPos, direction.getOpposite());
    }

    private BooleanProperty getDirectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> BlockStateProperties.NORTH;
            case SOUTH -> BlockStateProperties.SOUTH;
            case EAST -> BlockStateProperties.EAST;
            case WEST -> BlockStateProperties.WEST;
            case UP -> BlockStateProperties.UP;
            case DOWN -> BlockStateProperties.DOWN;
        };
    }

    private boolean isWaterAdjacent(LevelReader world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = world.getBlockState(adjacentPos);

            if (adjacentState.is(Blocks.WATER) || adjacentState.getBlock() instanceof LiquidBlock) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> state.setValue(BlockStateProperties.NORTH, state.getValue(BlockStateProperties.SOUTH))
                    .setValue(BlockStateProperties.EAST, state.getValue(BlockStateProperties.WEST))
                    .setValue(BlockStateProperties.SOUTH, state.getValue(BlockStateProperties.NORTH))
                    .setValue(BlockStateProperties.WEST, state.getValue(BlockStateProperties.EAST));
            case COUNTERCLOCKWISE_90 -> state.setValue(BlockStateProperties.NORTH, state.getValue(BlockStateProperties.EAST))
                    .setValue(BlockStateProperties.EAST, state.getValue(BlockStateProperties.SOUTH))
                    .setValue(BlockStateProperties.SOUTH, state.getValue(BlockStateProperties.WEST))
                    .setValue(BlockStateProperties.WEST, state.getValue(BlockStateProperties.NORTH));
            case CLOCKWISE_90 -> state.setValue(BlockStateProperties.NORTH, state.getValue(BlockStateProperties.WEST))
                    .setValue(BlockStateProperties.EAST, state.getValue(BlockStateProperties.NORTH))
                    .setValue(BlockStateProperties.SOUTH, state.getValue(BlockStateProperties.EAST))
                    .setValue(BlockStateProperties.WEST, state.getValue(BlockStateProperties.SOUTH));
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> state.setValue(BlockStateProperties.NORTH, state.getValue(BlockStateProperties.SOUTH)).setValue(BlockStateProperties.SOUTH, state.getValue(BlockStateProperties.NORTH));
            case FRONT_BACK -> state.setValue(BlockStateProperties.EAST, state.getValue(BlockStateProperties.WEST)).setValue(BlockStateProperties.WEST, state.getValue(BlockStateProperties.EAST));
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (isWaterAdjacent(world, pos)) {
            return false;
        }

        for (Direction direction : Direction.values()) {
            BooleanProperty property = getDirectionProperty(direction);
            if (property != null && state.getValue(property)) {
                if (canPlaceOn(world, pos, direction)) {
                    return true;
                }
            }
        }
        return false;
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (neighborState.is(Blocks.WATER) || neighborState.getBlock() instanceof LiquidBlock) {
            return Blocks.AIR.defaultBlockState();
        }

        BooleanProperty property = getDirectionProperty(direction);
        if (property != null && state.getValue(property)) {
            if (!canPlaceOn(world, pos, direction)) {
                state = state.setValue(property, false);
            }
        }

        if (!this.hasAnyFace(state)) {
            return Blocks.AIR.defaultBlockState();
        }

        return state;
    }

    @Override
    public void onPlace(BlockState state, net.minecraft.world.level.Level world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onPlace(state, world, pos, oldState, notify);

        if (isWaterAdjacent(world, pos)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }

        if (!state.getValue(INITIALIZED)) {
            world.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, net.minecraft.server.level.ServerLevel world, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (isWaterAdjacent(world, pos)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }

        if (!state.getValue(INITIALIZED)) {
            boolean textureVariant = world.getRandom().nextBoolean();
            BlockState newState = state.setValue(TEXTURE_VARIANT, textureVariant).setValue(INITIALIZED, true);
            world.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }

    public BlockState getInitializedState(BlockState state, net.minecraft.util.RandomSource random) {
        if (!state.getValue(INITIALIZED)) {
            boolean textureVariant = random.nextBoolean();
            return state.setValue(TEXTURE_VARIANT, textureVariant).setValue(INITIALIZED, true);
        }
        return state;
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return new MultifaceSpreader(this) {
            @Override
            public boolean canSpreadInAnyDirection(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
                return false;
            }
        };
    }
}