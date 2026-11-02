package potatowolfie.web.world.feature.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.chunk.ChunkGenerator;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.block.custom.SpiderSucculentBlock;

public class SpiderSucculentPatchFeature implements Feature {

    public static final MapCodec<SpiderSucculentPatchFeature> CODEC = MapCodec.unit(SpiderSucculentPatchFeature::new);

    public MapCodec<SpiderSucculentPatchFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockPos floorPos = findVegetationFloor(world, origin);
        if (floorPos == null) return false;

        int targetRoots = random.nextInt(4);
        BlockPos currentPos = floorPos.above();

        for (int i = 0; i <= targetRoots; i++) {
            if (!world.getBlockState(currentPos).isAir()) return false;

            BlockState state;
            if (i < targetRoots) {
                state = WebBlocks.SPIDER_SUCCULENT.defaultBlockState()
                        .setValue(SpiderSucculentBlock.PART, SpiderSucculentBlock.Part.ROOT)
                        .setValue(SpiderSucculentBlock.TARGET_ROOTS, targetRoots);
            } else {
                state = WebBlocks.SPIDER_SUCCULENT.defaultBlockState()
                        .setValue(SpiderSucculentBlock.PART, SpiderSucculentBlock.Part.BLOSSOM)
                        .setValue(SpiderSucculentBlock.TARGET_ROOTS, targetRoots);
            }
            world.setBlock(currentPos, state, Block.UPDATE_ALL);
            if (i == targetRoots) {
                world.scheduleTick(currentPos, WebBlocks.SPIDER_SUCCULENT, 10 + random.nextInt(20));
            }
            currentPos = currentPos.above();
        }

        return true;
    }

    private BlockPos findVegetationFloor(WorldGenLevel world, BlockPos startPos) {
        for (int y = startPos.getY() + 3; y >= startPos.getY() - 5; y--) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
            BlockState floorState = world.getBlockState(checkPos);
            BlockState aboveState = world.getBlockState(checkPos.above());

            if (aboveState.isAir() && (
                    floorState.is(BlockTags.SUPPORTS_VEGETATION) ||
                            floorState.is(WebBlocks.DAMP_SOIL) ||
                            floorState.is(WebBlocks.SPROUTING_DAMP_SOIL) ||
                            floorState.is(WebBlocks.DAMP_SOIL_PATH) ||
                            floorState.is(WebBlocks.SPIDER_MOSS) ||
                            floorState.is(WebBlocks.SPIDER_MOSS_CARPET)
            )) {
                return checkPos;
            }
        }
        return null;
    }
}