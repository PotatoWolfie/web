package potatowolfie.web.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import potatowolfie.web.advancement.ShellOnHandler;
import potatowolfie.web.block.WebBlocks;

public class SucculentGelBlock extends MultifaceBlock {
    public static final BooleanProperty SHELLS = BooleanProperty.create("shells");

    public static final int CEILING_STICK_TICKS = 90;
    public static final int CEILING_STICK_TICKS_WITH_SHELLS = 160;

    private static final double PLATE_THICKNESS = 1.0 / 16.0;
    private static final double TOP_TOUCH_EPSILON = 0.01;

    private static final Vec3 WALKING_SLOWDOWN = new Vec3(0.8, 1.0, 0.8);

    public SucculentGelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(SHELLS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SHELLS);
    }

    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return this.getShape(state, level, pos, CollisionContext.empty());
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!(entity instanceof LivingEntity living) || !(entity instanceof GelClimber climber)) {
            return;
        }
        if (living.isSpectator()) {
            return;
        }

        AABB box = living.getBoundingBox();
        boolean airborne = !living.onGround();
        boolean sneaking = living.isShiftKeyDown();
        boolean hasUp = hasFace(state, Direction.UP);
        boolean hasWall = hasWallFace(state);
        boolean pressedTop = hasUp && isPressedAgainstTop(pos, box);

        boolean released = false;

        if (pressedTop && airborne) {
            if (sneaking) {
                climber.web$releaseGelHold();
                released = true;
            } else {
                int limit = state.getValue(SHELLS) ? CEILING_STICK_TICKS_WITH_SHELLS : CEILING_STICK_TICKS;
                int stuck = climber.web$getStickTicks();
                if (stuck < limit) {
                    if (climber.web$getLastCountTick() != living.tickCount) {
                        climber.web$setLastCountTick(living.tickCount);
                        climber.web$setStickTicks(stuck + 1);
                    }
                    climber.web$allowGelHold();
                    return;
                }
                climber.web$releaseGelHold();
                released = true;
            }
        }

        if (hasWall && !(pressedTop && released) && overlapsWallPlate(state, pos, box)) {
            if (!pressedTop) {
                climber.web$setStickTicks(0);
            }
            climber.web$allowGelClimb();
            return;
        }

        if (!sneaking && climber.web$isGelHolding()) {
            return;
        }

        entity.makeStuckInBlock(state, WALKING_SLOWDOWN);
    }

    private static boolean isPressedAgainstTop(BlockPos pos, AABB box) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        return box.maxY >= y + 1.0 - TOP_TOUCH_EPSILON
                && box.intersects(x, y + 1.0 - PLATE_THICKNESS, z, x + 1.0, y + 1.0, z + 1.0);
    }

    private static boolean overlapsWallPlate(BlockState state, BlockPos pos, AABB box) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (hasFace(state, Direction.NORTH) && box.intersects(x, y, z, x + 1.0, y + 1.0, z + PLATE_THICKNESS)) {
            return true;
        }
        if (hasFace(state, Direction.SOUTH) && box.intersects(x, y, z + 1.0 - PLATE_THICKNESS, x + 1.0, y + 1.0, z + 1.0)) {
            return true;
        }
        if (hasFace(state, Direction.WEST) && box.intersects(x, y, z, x + PLATE_THICKNESS, y + 1.0, z + 1.0)) {
            return true;
        }
        return hasFace(state, Direction.EAST) && box.intersects(x + 1.0 - PLATE_THICKNESS, y, z, x + 1.0, y + 1.0, z + 1.0);
    }

    private static boolean hasWallFace(BlockState state) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (hasFace(state, direction)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(WebBlocks.SPIDER_EGG_SHELLS.asItem())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        if (state.getValue(SHELLS)) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(SHELLS, true), Block.UPDATE_ALL);
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
            spawnWaxOnParticles(level, pos);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

            if (player instanceof ServerPlayer serverPlayer) {
                ShellOnHandler.grantShellOnAdvancement(serverPlayer);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static void spawnWaxOnParticles(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        RandomSource random = level.getRandom();
        for (Direction direction : Direction.values()) {
            for (int i = 0; i < 4; i++) {
                double x = direction.getStepX() == 0 ? random.nextDouble() : 0.5 + direction.getStepX() * 0.55;
                double y = direction.getStepY() == 0 ? random.nextDouble() : 0.5 + direction.getStepY() * 0.55;
                double z = direction.getStepZ() == 0 ? random.nextDouble() : 0.5 + direction.getStepZ() * 0.55;
                serverLevel.sendParticles(ParticleTypes.WAX_ON,
                        pos.getX() + x, pos.getY() + y, pos.getZ() + z,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    private static boolean touchesWater(BlockGetter level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockState oldState, BlockGetter level, BlockPos placementPos,
                                                     Direction placementDirection) {
        if (!oldState.getFluidState().isEmpty() || touchesWater(level, placementPos)) {
            return null;
        }
        return super.getStateForPlacement(oldState, level, placementPos, placementDirection);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction directionToNeighbor, BlockPos neighborPos, BlockState neighborState,
                                     RandomSource random) {
        if (state.getValue(WATERLOGGED) || neighborState.getFluidState().is(FluidTags.WATER)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbor, neighborPos, neighborState, random);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && (state.getValue(WATERLOGGED) || touchesWater(level, pos))) {
            level.destroyBlock(pos, true);
        }
    }
}