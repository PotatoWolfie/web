package potatowolfie.web.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import potatowolfie.web.entity.custom.SpiderWebBlockEntity;
import potatowolfie.web.item.WebItems;

import static net.minecraft.state.property.Properties.FACING;

public class SpiderWebBlock extends BlockWithEntity {
    public static final MapCodec<SpiderWebBlock> CODEC = createCodec(SpiderWebBlock::new);
    public static final EnumProperty<WebType> WEB_TYPE = EnumProperty.of("web_type", WebType.class);

    @Override
    public MapCodec<SpiderWebBlock> getCodec() {
        return CODEC;
    }

    private static final VoxelShape GROUND_SHAPE = Block.createCuboidShape(-10, 0, -10, 26, 8, 26);
    private static final VoxelShape HANGING_SHAPE_NS = Block.createCuboidShape(0, 0, 6, 16, 16, 10);
    private static final VoxelShape HANGING_SHAPE_EW = Block.createCuboidShape(6, 0, 0, 10, 16, 16);
    private static final VoxelShape HANGING_1_SHAPE_NS = Block.createCuboidShape(0, 2, 6, 16, 16, 10);
    private static final VoxelShape HANGING_1_SHAPE_EW = Block.createCuboidShape(6, 2, 0, 10, 16, 16);
    private static final VoxelShape HANGING_TIP_SHAPE_NS = Block.createCuboidShape(0, 10, 6, 16, 16, 10);
    private static final VoxelShape HANGING_TIP_SHAPE_EW = Block.createCuboidShape(6, 10, 0, 10, 16, 16);

    public enum WebType implements StringIdentifiable {
        GROUND("ground"),
        HANGING_1("hanging_1"),
        HANGING_TOP("hanging_top"),
        HANGING_MIDDLE("hanging_middle"),
        HANGING_TIP("hanging_tip");

        private final String name;

        WebType(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }

    public SpiderWebBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.UP)
                .with(WEB_TYPE, WebType.GROUND));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WEB_TYPE);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.get(WEB_TYPE) == WebType.GROUND) {
            return new SpiderWebBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        if (state.get(WEB_TYPE) == WebType.GROUND) {
            return BlockRenderType.INVISIBLE;
        }
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        WebType webType = state.get(WEB_TYPE);
        Direction facing = state.get(FACING);

        if (webType == WebType.GROUND) {
            return GROUND_SHAPE;
        }

        return switch (webType) {
            case HANGING_1 -> switch (facing) {
                case NORTH, SOUTH -> HANGING_1_SHAPE_NS;
                case EAST, WEST -> HANGING_1_SHAPE_EW;
                default -> HANGING_1_SHAPE_NS;
            };
            case HANGING_TOP -> switch (facing) {
                case NORTH, SOUTH -> HANGING_SHAPE_NS;
                case EAST, WEST -> HANGING_SHAPE_EW;
                default -> HANGING_SHAPE_NS;
            };
            case HANGING_MIDDLE -> switch (facing) {
                case NORTH, SOUTH -> HANGING_SHAPE_NS;
                case EAST, WEST -> HANGING_SHAPE_EW;
                default -> HANGING_SHAPE_NS;
            };
            case HANGING_TIP -> switch (facing) {
                case NORTH, SOUTH -> HANGING_TIP_SHAPE_NS;
                case EAST, WEST -> HANGING_TIP_SHAPE_EW;
                default -> HANGING_TIP_SHAPE_NS;
            };
            default -> GROUND_SHAPE;
        };
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction clickedSide = ctx.getSide();
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();

        if (clickedSide == Direction.UP) {
            return this.getDefaultState()
                    .with(FACING, Direction.UP)
                    .with(WEB_TYPE, WebType.GROUND);
        } else if (clickedSide == Direction.DOWN) {
            Direction facingDirection = getHangingWebFacing(world, pos, ctx);
            WebType webType = determineHangingType(world, pos);
            return this.getDefaultState()
                    .with(FACING, facingDirection)
                    .with(WEB_TYPE, webType);
        } else if (clickedSide.getAxis().isHorizontal()) {
            BlockPos belowPos = pos.down();
            BlockState belowState = world.getBlockState(belowPos);

            if (belowState.isSolidBlock(world, belowPos) && belowState.getBlock() != this) {
                return this.getDefaultState()
                        .with(FACING, Direction.UP)
                        .with(WEB_TYPE, WebType.GROUND);
            }
            return null;
        }
        return null;
    }

    private Direction getHangingWebFacing(World world, BlockPos pos, ItemPlacementContext ctx) {
        BlockPos abovePos = pos.up();
        BlockState aboveState = world.getBlockState(abovePos);

        if (aboveState.getBlock() == this && aboveState.get(WEB_TYPE) != WebType.GROUND) {
            return aboveState.get(FACING);
        }

        BlockPos checkPos = abovePos.up();
        for (int i = 0; i < 10 && checkPos.getY() < world.getHeight(); i++) {
            BlockState checkState = world.getBlockState(checkPos);
            if (checkState.getBlock() == this && checkState.get(WEB_TYPE) != WebType.GROUND) {
                return checkState.get(FACING);
            }
            if (!checkState.isAir() && checkState.getBlock() != this) {
                break;
            }
            checkPos = checkPos.up();
        }

        return ctx.getHorizontalPlayerFacing().getOpposite();
    }

    private WebType determineHangingType(World world, BlockPos pos) {
        BlockPos belowPos = pos.down();
        BlockState belowState = world.getBlockState(belowPos);

        if (belowState.getBlock() == this && belowState.get(WEB_TYPE) != WebType.GROUND) {
            WebType belowType = belowState.get(WEB_TYPE);
            if (belowType == WebType.HANGING_1) {
                return WebType.HANGING_TOP;
            } else {
                return WebType.HANGING_TOP;
            }
        }
        return WebType.HANGING_1;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView scheduledTickView,
                                                   BlockPos pos, Direction direction, BlockPos neighborPos,
                                                   BlockState neighborState, net.minecraft.util.math.random.Random random) {
        WebType currentType = state.get(WEB_TYPE);

        if (currentType != WebType.GROUND) {
            if (direction == Direction.UP && !canSupportHangingWeb(world, neighborPos, neighborState)) {
                if (world instanceof WorldAccess worldAccess && worldAccess instanceof ServerWorld serverWorld) {
                    serverWorld.scheduleBlockTick(pos, this, 1);
                }
                return Blocks.AIR.getDefaultState();
            }

            if (world instanceof WorldAccess worldAccess) {
                updateHangingWebChain(worldAccess, pos);
            }
        }
        return super.getStateForNeighborUpdate(state, world, scheduledTickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (state.get(WEB_TYPE) != WebType.GROUND) {
            breakHangingChain(world, pos);
        }
    }

    private boolean canSupportHangingWeb(WorldView world, BlockPos pos, BlockState state) {
        if (state.getBlock() == this) {
            return state.get(WEB_TYPE) != WebType.GROUND;
        }
        return state.isSolidBlock(world, pos);
    }

    private void breakHangingChain(ServerWorld world, BlockPos startPos) {
        BlockPos currentPos = startPos;

        while (world.getBlockState(currentPos).getBlock() == this &&
                world.getBlockState(currentPos).get(WEB_TYPE) != WebType.GROUND) {
            world.breakBlock(currentPos, true);
            currentPos = currentPos.down();
        }
    }

    private void updateHangingWebChain(WorldAccess world, BlockPos startPos) {
        BlockPos topPos = findTopOfChain(world, startPos);
        BlockPos currentPos = topPos;
        int chainIndex = 0;

        while (world.getBlockState(currentPos).getBlock() == this &&
                world.getBlockState(currentPos).get(WEB_TYPE) != WebType.GROUND) {

            BlockPos belowPos = currentPos.down();
            boolean hasBelow = world.getBlockState(belowPos).getBlock() == this &&
                    world.getBlockState(belowPos).get(WEB_TYPE) != WebType.GROUND;

            WebType newType;
            if (chainIndex == 0 && hasBelow) {
                newType = WebType.HANGING_TOP;
            } else if (chainIndex == 0 && !hasBelow) {
                newType = WebType.HANGING_1;
            } else if (!hasBelow) {
                newType = WebType.HANGING_TIP;
            } else {
                newType = WebType.HANGING_MIDDLE;
            }

            BlockState currentState = world.getBlockState(currentPos);
            BlockState newState = currentState.with(WEB_TYPE, newType);
            world.setBlockState(currentPos, newState, Block.NOTIFY_ALL);

            currentPos = currentPos.down();
            chainIndex++;
        }
    }

    private BlockPos findTopOfChain(WorldAccess world, BlockPos pos) {
        BlockPos current = pos;
        while (true) {
            BlockPos above = current.up();
            BlockState aboveState = world.getBlockState(above);
            if (aboveState.getBlock() == this && aboveState.get(WEB_TYPE) != WebType.GROUND) {
                current = above;
            } else {
                break;
            }
        }
        return current;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        WebType webType = state.get(WEB_TYPE);

        if (webType == WebType.GROUND) {
            Vec3d vec3d = new Vec3d(0.25, 0.05000000074505806, 0.25);
            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity.hasStatusEffect(StatusEffects.WEAVING)) {
                    vec3d = new Vec3d(0.5, 0.25, 0.5);
                }
            }
            entity.slowMovement(state, vec3d);
        }
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    public boolean isInClimbableTag(BlockState state) {
        return state.get(WEB_TYPE) != WebType.GROUND;
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return getSpiderWebItem();
    }

    public ItemStack getSpiderWebItem() {
        return new ItemStack(WebItems.SPIDER_WEB);
    }
}