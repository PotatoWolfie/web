package potatowolfie.web.world.feature.custom;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.block.custom.SpiderEggShellsBlock;

import java.util.HashSet;
import java.util.Set;

public class SpiderEggClusterFeature extends Feature<SpiderEggClusterFeatureConfig> {

    public SpiderEggClusterFeature(Codec<SpiderEggClusterFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<SpiderEggClusterFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();
        SpiderEggClusterFeatureConfig config = context.getConfig();

        BlockPos actualOrigin = findValidOrigin(world, origin);
        if (actualOrigin == null) {
            return false;
        }

        if (!isValidCaveLocation(world, actualOrigin)) {
            return false;
        }

        int clusterSize = random.nextBetween(20, 40);
        Set<BlockPos> nestPositions = new HashSet<>();

        for (int i = 0; i < clusterSize; i++) {
            int x = actualOrigin.getX() + random.nextBetween(-config.nestSpreadRadius(), config.nestSpreadRadius());
            int z = actualOrigin.getZ() + random.nextBetween(-config.nestSpreadRadius(), config.nestSpreadRadius());

            BlockPos floorPos = findFloorPosition(world, new BlockPos(x, actualOrigin.getY(), z));
            if (floorPos != null && canPlaceNest(world, floorPos)) {
                world.setBlockState(floorPos, WebBlocks.SPIDER_NEST.getDefaultState(), Block.NOTIFY_ALL);
                nestPositions.add(floorPos);

                if (random.nextFloat() < 0.4f) {
                    for (BlockPos nearbyPos : BlockPos.iterate(floorPos.add(-3, 0, -3), floorPos.add(3, 0, 3))) {
                        if (random.nextFloat() < 0.25f && canPlaceNest(world, nearbyPos)) {
                            world.setBlockState(nearbyPos, WebBlocks.SPIDER_NEST.getDefaultState(), Block.NOTIFY_ALL);
                            nestPositions.add(nearbyPos.toImmutable());
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 8; i++) {
            int x = actualOrigin.getX() + random.nextBetween(-3, 3);
            int z = actualOrigin.getZ() + random.nextBetween(-3, 3);

            BlockPos floorPos = findFloorPosition(world, new BlockPos(x, actualOrigin.getY(), z));
            if (floorPos != null && canPlaceNest(world, floorPos)) {
                world.setBlockState(floorPos, WebBlocks.SPIDER_NEST.getDefaultState(), Block.NOTIFY_ALL);
                nestPositions.add(floorPos);
            }
        }

        int maxEggs = Math.max(8, nestPositions.size() / 3);
        int eggsPlaced = 0;
        Set<BlockPos> usedEggPositions = new HashSet<>();

        for (BlockPos nestPos : nestPositions) {
            if (eggsPlaced >= maxEggs) break;

            BlockPos eggPos = nestPos.up();
            if (random.nextFloat() < (config.eggChance() * 1.5f) && world.getBlockState(eggPos).isAir()) {
                boolean canPlace = true;

                for (BlockPos usedPos : usedEggPositions) {
                    double distance = Math.sqrt(eggPos.getSquaredDistance(usedPos));
                    if (distance < 2.5) {
                        canPlace = false;
                        break;
                    }
                }

                if (canPlace) {
                    world.setBlockState(eggPos, WebBlocks.SPIDER_EGG.getDefaultState(), Block.NOTIFY_ALL);
                    usedEggPositions.add(eggPos);
                    eggsPlaced++;
                }
            }
        }

        for (int i = 0; i < 6; i++) {
            int x = actualOrigin.getX() + random.nextBetween(-4, 4);
            int z = actualOrigin.getZ() + random.nextBetween(-4, 4);
            BlockPos centerPos = new BlockPos(x, actualOrigin.getY(), z);

            BlockPos eggPos = findFloorPosition(world, centerPos);
            if (eggPos != null && world.getBlockState(eggPos.up()).isAir()) {
                boolean canPlace = true;

                for (BlockPos usedPos : usedEggPositions) {
                    double distance = Math.sqrt(eggPos.up().getSquaredDistance(usedPos));
                    if (distance < 2.5) {
                        canPlace = false;
                        break;
                    }
                }

                if (canPlace) {
                    world.setBlockState(eggPos.up(), WebBlocks.SPIDER_EGG.getDefaultState(), Block.NOTIFY_ALL);
                    usedEggPositions.add(eggPos.up());
                }
            }
        }

        placeSpiderEggShells(world, actualOrigin, nestPositions, random, config);

        return !nestPositions.isEmpty();
    }

    private BlockPos findValidOrigin(StructureWorldAccess world, BlockPos startPos) {
        if (!world.getBlockState(startPos).isAir() || isValidCaveLocation(world, startPos)) {
            return startPos;
        }

        for (int y = startPos.getY(); y >= -64; y--) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());

            if (isValidCaveLocation(world, checkPos)) {
                return checkPos;
            }

            if (!world.getBlockState(checkPos).isAir()) {
                for (int offset = 1; offset <= 5; offset++) {
                    BlockPos cavePos = checkPos.up(offset);
                    if (isValidCaveLocation(world, cavePos)) {
                        return cavePos;
                    }
                }
            }
        }

        return null;
    }

    private boolean isValidCaveLocation(StructureWorldAccess world, BlockPos pos) {
        if (pos.getY() > 55) return false;

        if (!world.getBlockState(pos).isAir()) return false;

        int solidBlocks = 0;
        int airBlocks = 0;

        for (BlockPos checkPos : BlockPos.iterate(pos.add(-2, -1, -2), pos.add(2, 1, 2))) {
            BlockState state = world.getBlockState(checkPos);
            if (state.isAir()) {
                airBlocks++;
            } else if (state.isSolidBlock(world, checkPos)) {
                solidBlocks++;
            }
        }

        return airBlocks >= 5 && solidBlocks >= 8;
    }

    private BlockPos findFloorPosition(StructureWorldAccess world, BlockPos startPos) {
        for (int y = startPos.getY() + 5; y >= startPos.getY() - 15; y--) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
            BlockState floorState = world.getBlockState(checkPos);
            BlockState aboveState = world.getBlockState(checkPos.up());

            if (canReplaceForNest(floorState) && aboveState.isAir()) {
                return checkPos;
            }
        }
        return null;
    }

    private boolean canPlaceNest(StructureWorldAccess world, BlockPos pos) {
        BlockState floorState = world.getBlockState(pos);
        BlockState aboveState = world.getBlockState(pos.up());

        return canReplaceForNest(floorState) && aboveState.isAir();
    }

    private boolean canReplaceForNest(BlockState state) {
        return state.isOf(Blocks.STONE) ||
                state.isOf(Blocks.DEEPSLATE) ||
                state.isOf(Blocks.GRANITE) ||
                state.isOf(Blocks.DIORITE) ||
                state.isOf(Blocks.ANDESITE) ||
                state.isOf(Blocks.TUFF) ||
                state.isOf(Blocks.COBBLESTONE) ||
                state.isOf(Blocks.COBBLED_DEEPSLATE) ||
                state.isOf(Blocks.GRAVEL) ||
                state.isOf(Blocks.DIRT) ||
                state.isOf(Blocks.MOSS_BLOCK) ||
                state.isOf(Blocks.CLAY) ||
                state.isOf(Blocks.COARSE_DIRT) ||
                state.isOf(Blocks.CALCITE) ||
                state.isOf(Blocks.SMOOTH_BASALT) ||
                state.isOf(Blocks.AMETHYST_BLOCK);
    }

    private void placeSpiderEggShells(StructureWorldAccess world, BlockPos origin, Set<BlockPos> nestPositions, Random random, SpiderEggClusterFeatureConfig config) {
        int shellRadius = config.nestSpreadRadius() + 4;

        int guaranteedShells = Math.max(8, nestPositions.size() / 3);
        int shellsPlaced = 0;

        for (int attempts = 0; attempts < 60 && shellsPlaced < guaranteedShells + 15; attempts++) {
            int x = origin.getX() + random.nextBetween(-shellRadius, shellRadius);
            int y = origin.getY() + random.nextBetween(-4, 5);
            int z = origin.getZ() + random.nextBetween(-shellRadius, shellRadius);

            BlockPos shellPos = new BlockPos(x, y, z);

            float chance = shellsPlaced < guaranteedShells ? 0.85f : 0.6f;
            if (random.nextFloat() < chance) {
                if (tryPlaceShellOnSurface(world, shellPos, random)) {
                    shellsPlaced++;
                }
            }
        }

        int nestsWithShells = 0;
        int targetNestsWithShells = Math.max(4, nestPositions.size() / 3);

        for (BlockPos nestPos : nestPositions) {
            if (nestsWithShells >= targetNestsWithShells) break;

            boolean shouldPlaceShells = nestsWithShells < (targetNestsWithShells / 2) || random.nextFloat() < 0.6f;

            if (shouldPlaceShells) {
                int shellCount = random.nextBetween(1, 3);
                for (int i = 0; i < shellCount; i++) {
                    int x = nestPos.getX() + random.nextBetween(-5, 5);
                    int y = nestPos.getY() + random.nextBetween(-3, 4);
                    int z = nestPos.getZ() + random.nextBetween(-5, 5);

                    BlockPos shellPos = new BlockPos(x, y, z);
                    tryPlaceShellOnSurface(world, shellPos, random);
                }
                nestsWithShells++;
            }
        }
    }

    private boolean tryPlaceShellOnSurface(StructureWorldAccess world, BlockPos pos, Random random) {
        if (!world.getBlockState(pos).isAir()) {
            return false;
        }

        Direction[] directions = {
                Direction.DOWN,
                Direction.NORTH,
                Direction.SOUTH,
                Direction.EAST,
                Direction.WEST,
                Direction.UP
        };

        for (Direction direction : directions) {
            BlockPos attachPos = pos.offset(direction);
            BlockState attachState = world.getBlockState(attachPos);

            if (canAttachShellTo(attachState)) {
                SpiderEggShellsBlock shellBlock = (SpiderEggShellsBlock) WebBlocks.SPIDER_EGG_SHELLS;
                BlockState shellState = shellBlock.getDefaultState();

                switch (direction) {
                    case DOWN -> shellState = shellState.with(net.minecraft.state.property.Properties.DOWN, true);
                    case UP -> shellState = shellState.with(net.minecraft.state.property.Properties.UP, true);
                    case NORTH -> shellState = shellState.with(net.minecraft.state.property.Properties.NORTH, true);
                    case SOUTH -> shellState = shellState.with(net.minecraft.state.property.Properties.SOUTH, true);
                    case EAST -> shellState = shellState.with(net.minecraft.state.property.Properties.EAST, true);
                    case WEST -> shellState = shellState.with(net.minecraft.state.property.Properties.WEST, true);
                }

                shellState = shellBlock.getInitializedState(shellState, random);

                world.setBlockState(pos, shellState, Block.NOTIFY_ALL);
                return true;
            }
        }

        return false;
    }

    private boolean canAttachShellTo(BlockState state) {
        return state.isOf(Blocks.STONE) ||
                state.isOf(Blocks.DEEPSLATE) ||
                state.isOf(Blocks.GRANITE) ||
                state.isOf(Blocks.DIORITE) ||
                state.isOf(Blocks.ANDESITE) ||
                state.isOf(Blocks.TUFF) ||
                state.isOf(Blocks.COBBLESTONE) ||
                state.isOf(Blocks.COBBLED_DEEPSLATE) ||
                state.isOf(Blocks.GRAVEL) ||
                state.isOf(Blocks.DIRT) ||
                state.isOf(Blocks.COARSE_DIRT) ||
                state.isOf(Blocks.MOSS_BLOCK) ||
                state.isOf(Blocks.CLAY) ||
                state.isOf(WebBlocks.SPIDER_NEST);
    }
}