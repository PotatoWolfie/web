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
import potatowolfie.web.block.custom.EggStage;
import potatowolfie.web.block.custom.SpiderEggBlock;
import potatowolfie.web.block.custom.SpiderEggShellsBlock;

public class SpiderEggPatchFeature implements Feature {

    public static final MapCodec<SpiderEggPatchFeature> CODEC = MapCodec.unit(SpiderEggPatchFeature::new);
    private static final float OLD_BATCH_CHANCE = 0.05f;

    public MapCodec<SpiderEggPatchFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockPos floorPos = findFloorPosition(world, origin);
        if (floorPos == null || !world.getBlockState(floorPos.above()).isAir()) {
            return false;
        }

        boolean oldBatch = random.nextFloat() < OLD_BATCH_CHANCE;
        boolean isLargeFiveEgg = random.nextFloat() < 0.35f;

        if (isLargeFiveEgg) {
            BlockPos topNestPos = floorPos.above();
            BlockPos topEggPos = topNestPos.above();

            if (!world.getBlockState(topNestPos).isAir() || !world.getBlockState(topEggPos).isAir()) {
                return false;
            }

            world.setBlock(floorPos, WebBlocks.SPIDER_MOSS.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
            world.setBlock(topNestPos, WebBlocks.SPIDER_MOSS.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
            world.setBlock(topEggPos, getEggBlockState(random, oldBatch), Block.UPDATE_ALL);

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos sideFloor = floorPos.relative(dir);
                BlockPos sideEgg = sideFloor.above();
                if (world.getBlockState(sideEgg).isAir() && canReplaceForNest(world.getBlockState(sideFloor))) {
                    world.setBlock(sideEgg, getEggBlockState(random, oldBatch), Block.UPDATE_ALL);
                }
            }

            if (random.nextFloat() < 0.6f) {
                tryPlaceShells(world, floorPos, random);
            }
        } else {
            int eggCount = random.nextIntBetweenInclusive(2, 3);
            BlockPos currentPos = floorPos.above();
            world.setBlock(currentPos, getEggBlockState(random, oldBatch), Block.UPDATE_ALL);

            for (int i = 1; i < eggCount; i++) {
                Direction randomDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockPos neighborEgg = currentPos.relative(randomDir);
                if (world.getBlockState(neighborEgg).isAir() && world.getBlockState(neighborEgg.below()).isSolid()) {
                    world.setBlock(neighborEgg, getEggBlockState(random, oldBatch), Block.UPDATE_ALL);
                }
            }

            if (random.nextFloat() < 0.3f) {
                tryPlaceShells(world, floorPos, random);
            }
        }

        return true;
    }

    private BlockState getEggBlockState(RandomSource random, boolean old) {
        BlockState state = WebBlocks.SPIDER_EGG.defaultBlockState();
        if (old) {
            return state.setValue(SpiderEggBlock.STAGE, EggStage.OLD);
        }
        int hatchTicks = 18 * 60 * 20 + random.nextInt(6 * 60 * 20);
        return state.setValue(SpiderEggBlock.REMAINING_TIME, hatchTicks);
    }

    private BlockPos findFloorPosition(WorldGenLevel world, BlockPos startPos) {
        for (int y = startPos.getY() + 3; y >= startPos.getY() - 5; y--) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
            if (canReplaceForNest(world.getBlockState(checkPos)) && world.getBlockState(checkPos.above()).isAir()) {
                return checkPos;
            }
        }
        return null;
    }

    private boolean canReplaceForNest(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.TUFF) ||
                state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE) ||
                state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.DIRT) ||
                state.is(WebBlocks.DAMP_SOIL) || state.is(WebBlocks.SPROUTING_DAMP_SOIL) ||
                state.is(WebBlocks.DAMP_SOIL_PATH) || state.is(WebBlocks.SPIDER_MOSS) ||
                state.is(WebBlocks.SPIDER_MOSS_CARPET);
    }

    private void tryPlaceShells(WorldGenLevel world, BlockPos pos, RandomSource random) {
        BlockPos shellPos = pos.above().relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
        if (world.getBlockState(shellPos).isAir()) {
            BlockState shellState = ((SpiderEggShellsBlock) WebBlocks.SPIDER_EGG_SHELLS).defaultBlockState()
                    .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN, true);
            world.setBlock(shellPos, shellState, Block.UPDATE_ALL);
        }
    }
}