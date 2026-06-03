package potatowolfie.web.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpiderGrassBlock extends VegetationBlock {
    public static final MapCodec<SpiderGrassBlock> CODEC = simpleCodec(SpiderGrassBlock::new);
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 3.0);

    public MapCodec<SpiderGrassBlock> codec() {
        return CODEC;
    }

    public SpiderGrassBlock(Properties settings) {
        super(settings);
    }

    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        return super.mayPlaceOn(floor, world, pos);
    }
}