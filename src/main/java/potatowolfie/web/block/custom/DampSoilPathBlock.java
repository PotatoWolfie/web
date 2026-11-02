package potatowolfie.web.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import potatowolfie.web.block.WebBlocks;

public class DampSoilPathBlock extends Block {
    protected static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);

    public DampSoilPathBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return !this.defaultBlockState().canSurvive(ctx.getLevel(), ctx.getClickedPos())
                ? Block.pushEntitiesUp(this.defaultBlockState(), WebBlocks.DAMP_SOIL.defaultBlockState(), ctx.getLevel(), ctx.getClickedPos())
                : super.getStateForPlacement(ctx);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.above());
        return blockState.isAir() || blockState.getBlock() instanceof FenceGateBlock;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.UP && !state.canSurvive(world, pos)) {
            tickView.scheduleTick(pos, this, 1);
        }

        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        DampSoilPathBlock.setToDampSoil(null, state, world, pos);
    }

    public static void setToDampSoil(@Nullable Entity entity, BlockState state, Level world, BlockPos pos) {
        BlockState blockState = Block.pushEntitiesUp(state, WebBlocks.DAMP_SOIL.defaultBlockState(), world, pos);
        world.setBlock(pos, blockState, 3);
        world.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}