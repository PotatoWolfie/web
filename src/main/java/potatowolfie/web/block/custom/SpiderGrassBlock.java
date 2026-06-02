package potatowolfie.web.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class SpiderGrassBlock extends PlantBlock {
    public static final MapCodec<SpiderGrassBlock> CODEC = createCodec(SpiderGrassBlock::new);
    private static final VoxelShape SHAPE = Block.createColumnShape(12.0, 0.0, 3.0);

    public MapCodec<SpiderGrassBlock> getCodec() {
        return CODEC;
    }

    public SpiderGrassBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return super.canPlantOnTop(floor, world, pos);
    }
}