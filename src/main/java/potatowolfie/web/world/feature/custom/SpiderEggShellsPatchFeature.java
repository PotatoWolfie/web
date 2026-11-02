package potatowolfie.web.world.feature.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.chunk.ChunkGenerator;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.block.custom.SpiderEggShellsBlock;

public class SpiderEggShellsPatchFeature implements Feature {

    public static final MapCodec<SpiderEggShellsPatchFeature> CODEC = MapCodec.unit(SpiderEggShellsPatchFeature::new);

    public MapCodec<SpiderEggShellsPatchFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        boolean placedAny = false;
        int patchSize = random.nextIntBetweenInclusive(3, 7);

        for (int i = 0; i < patchSize; i++) {
            int x = origin.getX() + random.nextIntBetweenInclusive(-4, 4);
            int y = origin.getY() + random.nextIntBetweenInclusive(-3, 3);
            int z = origin.getZ() + random.nextIntBetweenInclusive(-4, 4);

            BlockPos targetPos = new BlockPos(x, y, z);
            if (tryPlaceShell(world, targetPos, random)) {
                placedAny = true;
            }
        }

        return placedAny;
    }

    private boolean tryPlaceShell(WorldGenLevel world, BlockPos pos, RandomSource random) {
        if (!world.getBlockState(pos).isAir()) {
            return false;
        }

        Direction[] directions = {
                Direction.DOWN,
                Direction.UP,
                Direction.NORTH,
                Direction.SOUTH,
                Direction.EAST,
                Direction.WEST
        };

        for (Direction direction : directions) {
            BlockPos attachPos = pos.relative(direction);
            BlockState attachState = world.getBlockState(attachPos);

            if (canAttachShellTo(attachState)) {
                SpiderEggShellsBlock shellBlock = (SpiderEggShellsBlock) WebBlocks.SPIDER_EGG_SHELLS;
                BlockState shellState = shellBlock.defaultBlockState();

                switch (direction) {
                    case DOWN -> shellState = shellState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN, true);
                    case UP -> shellState = shellState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.UP, true);
                    case NORTH -> shellState = shellState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH, true);
                    case SOUTH -> shellState = shellState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH, true);
                    case EAST -> shellState = shellState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST, true);
                    case WEST -> shellState = shellState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST, true);
                }

                shellState = shellBlock.getInitializedState(shellState, random);
                world.setBlock(pos, shellState, Block.UPDATE_ALL);
                return true;
            }
        }

        return false;
    }

    private boolean canAttachShellTo(BlockState state) {
        return state.is(Blocks.STONE) ||
                state.is(Blocks.DEEPSLATE) ||
                state.is(Blocks.GRANITE) ||
                state.is(Blocks.DIORITE) ||
                state.is(Blocks.ANDESITE) ||
                state.is(Blocks.TUFF) ||
                state.is(Blocks.COBBLESTONE) ||
                state.is(Blocks.COBBLED_DEEPSLATE) ||
                state.is(Blocks.GRAVEL) ||
                state.is(Blocks.DIRT) ||
                state.is(Blocks.COARSE_DIRT) ||
                state.is(Blocks.MOSS_BLOCK) ||
                state.is(Blocks.CLAY) ||
                state.is(WebBlocks.SPIDER_MOSS) ||
                state.is(WebBlocks.SPIDER_MOSS_CARPET) ||
                state.is(WebBlocks.DAMP_SOIL) ||
                state.is(WebBlocks.SPROUTING_DAMP_SOIL) ||
                state.is(WebBlocks.DAMP_SOIL_PATH);
    }
}