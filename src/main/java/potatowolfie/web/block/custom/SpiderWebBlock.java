package potatowolfie.web.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import potatowolfie.web.entity.custom.SpiderWebBlockEntity;
import potatowolfie.web.item.WebItems;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class SpiderWebBlock extends BaseEntityBlock {
    public static final MapCodec<SpiderWebBlock> CODEC = simpleCodec(SpiderWebBlock::new);
    public static final EnumProperty<WebType> WEB_TYPE = EnumProperty.create("web_type", WebType.class);

    @Override
    public MapCodec<SpiderWebBlock> codec() {
        return CODEC;
    }

    private static final VoxelShape GROUND_SHAPE = Block.box(-10, 0, -10, 26, 8, 26);
    private static final VoxelShape HANGING_SHAPE_NS = Block.box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape HANGING_SHAPE_EW = Block.box(6, 0, 0, 10, 16, 16);
    private static final VoxelShape HANGING_1_SHAPE_NS = Block.box(0, 2, 6, 16, 16, 10);
    private static final VoxelShape HANGING_1_SHAPE_EW = Block.box(6, 2, 0, 10, 16, 16);
    private static final VoxelShape HANGING_TIP_SHAPE_NS = Block.box(0, 10, 6, 16, 16, 10);
    private static final VoxelShape HANGING_TIP_SHAPE_EW = Block.box(6, 10, 0, 10, 16, 16);

    public enum WebType implements StringRepresentable {
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
        public String getSerializedName() {
            return this.name;
        }
    }

    public SpiderWebBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(WEB_TYPE, WebType.GROUND));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WEB_TYPE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(WEB_TYPE) == WebType.GROUND) {
            return new SpiderWebBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        if (state.getValue(WEB_TYPE) == WebType.GROUND) {
            return RenderShape.INVISIBLE;
        }
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        WebType webType = state.getValue(WEB_TYPE);
        Direction facing = state.getValue(FACING);

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
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction clickedSide = ctx.getClickedFace();
        BlockPos pos = ctx.getClickedPos();
        Level world = ctx.getLevel();

        if (clickedSide == Direction.UP) {
            return this.defaultBlockState()
                    .setValue(FACING, Direction.UP)
                    .setValue(WEB_TYPE, WebType.GROUND);
        } else if (clickedSide == Direction.DOWN) {
            Direction facingDirection = getHangingWebFacing(world, pos, ctx);
            WebType webType = determineHangingType(world, pos);
            return this.defaultBlockState()
                    .setValue(FACING, facingDirection)
                    .setValue(WEB_TYPE, webType);
        } else if (clickedSide.getAxis().isHorizontal()) {
            BlockPos belowPos = pos.below();
            BlockState belowState = world.getBlockState(belowPos);

            if (belowState.isRedstoneConductor(world, belowPos) && belowState.getBlock() != this) {
                return this.defaultBlockState()
                        .setValue(FACING, Direction.UP)
                        .setValue(WEB_TYPE, WebType.GROUND);
            }
            return null;
        }
        return null;
    }

    private Direction getHangingWebFacing(Level world, BlockPos pos, BlockPlaceContext ctx) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = world.getBlockState(abovePos);

        if (aboveState.getBlock() == this && aboveState.getValue(WEB_TYPE) != WebType.GROUND) {
            return aboveState.getValue(FACING);
        }

        BlockPos checkPos = abovePos.above();
        for (int i = 0; i < 10 && checkPos.getY() < world.getHeight(); i++) {
            BlockState checkState = world.getBlockState(checkPos);
            if (checkState.getBlock() == this && checkState.getValue(WEB_TYPE) != WebType.GROUND) {
                return checkState.getValue(FACING);
            }
            if (!checkState.isAir() && checkState.getBlock() != this) {
                break;
            }
            checkPos = checkPos.above();
        }

        return ctx.getHorizontalDirection().getOpposite();
    }

    private WebType determineHangingType(Level world, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = world.getBlockState(belowPos);

        if (belowState.getBlock() == this && belowState.getValue(WEB_TYPE) != WebType.GROUND) {
            WebType belowType = belowState.getValue(WEB_TYPE);
            if (belowType == WebType.HANGING_1) {
                return WebType.HANGING_TOP;
            } else {
                return WebType.HANGING_TOP;
            }
        }
        return WebType.HANGING_1;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess scheduledTickView,
                                                   BlockPos pos, Direction direction, BlockPos neighborPos,
                                                   BlockState neighborState, net.minecraft.util.RandomSource random) {
        WebType currentType = state.getValue(WEB_TYPE);

        if (currentType != WebType.GROUND) {
            if (direction == Direction.UP && !canSupportHangingWeb(world, neighborPos, neighborState)) {
                if (world instanceof LevelAccessor worldAccess && worldAccess instanceof ServerLevel serverWorld) {
                    serverWorld.scheduleTick(pos, this, 1);
                }
                return Blocks.AIR.defaultBlockState();
            }

            if (world instanceof LevelAccessor worldAccess) {
                updateHangingWebChain(worldAccess, pos);
            }
        }
        return super.updateShape(state, world, scheduledTickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (state.getValue(WEB_TYPE) != WebType.GROUND) {
            breakHangingChain(world, pos);
        }
    }

    private boolean canSupportHangingWeb(LevelReader world, BlockPos pos, BlockState state) {
        if (state.getBlock() == this) {
            return state.getValue(WEB_TYPE) != WebType.GROUND;
        }
        return state.isRedstoneConductor(world, pos);
    }

    private void breakHangingChain(ServerLevel world, BlockPos startPos) {
        BlockPos currentPos = startPos;

        while (world.getBlockState(currentPos).getBlock() == this &&
                world.getBlockState(currentPos).getValue(WEB_TYPE) != WebType.GROUND) {
            world.destroyBlock(currentPos, true);
            currentPos = currentPos.below();
        }
    }

    private void updateHangingWebChain(LevelAccessor world, BlockPos startPos) {
        BlockPos topPos = findTopOfChain(world, startPos);
        BlockPos currentPos = topPos;
        int chainIndex = 0;

        while (world.getBlockState(currentPos).getBlock() == this &&
                world.getBlockState(currentPos).getValue(WEB_TYPE) != WebType.GROUND) {

            BlockPos belowPos = currentPos.below();
            boolean hasBelow = world.getBlockState(belowPos).getBlock() == this &&
                    world.getBlockState(belowPos).getValue(WEB_TYPE) != WebType.GROUND;

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
            BlockState newState = currentState.setValue(WEB_TYPE, newType);
            world.setBlock(currentPos, newState, Block.UPDATE_ALL);

            currentPos = currentPos.below();
            chainIndex++;
        }
    }

    private BlockPos findTopOfChain(LevelAccessor world, BlockPos pos) {
        BlockPos current = pos;
        while (true) {
            BlockPos above = current.above();
            BlockState aboveState = world.getBlockState(above);
            if (aboveState.getBlock() == this && aboveState.getValue(WEB_TYPE) != WebType.GROUND) {
                current = above;
            } else {
                break;
            }
        }
        return current;
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean bl) {
        WebType webType = state.getValue(WEB_TYPE);

        if (webType == WebType.GROUND) {
            Vec3 vec3d = new Vec3(0.25, 0.05000000074505806, 0.25);
            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity.hasEffect(MobEffects.WEAVING)) {
                    vec3d = new Vec3(0.5, 0.25, 0.5);
                }
            }
            entity.makeStuckInBlock(state, vec3d);
        }
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    public boolean isInClimbableTag(BlockState state) {
        return state.getValue(WEB_TYPE) != WebType.GROUND;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return getSpiderWebItem();
    }

    public ItemStack getSpiderWebItem() {
        return new ItemStack(WebItems.SPIDER_WEB);
    }
}