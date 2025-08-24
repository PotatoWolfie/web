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
import potatowolfie.web.block.custom.SpiderWebBlock;

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

        Set<BlockPos> mainEggPositions = new HashSet<>();
        boolean generatedMainNests = generateMainNestStructures(world, actualOrigin, random, mainEggPositions);
        if (!generatedMainNests) {
            return false;
        }

        int clusterSize = random.nextBetween(22, 45);
        Set<BlockPos> nestPositions = new HashSet<>();

        for (int i = 0; i < clusterSize; i++) {
            int x = actualOrigin.getX() + random.nextBetween(-config.nestSpreadRadius(), config.nestSpreadRadius());
            int z = actualOrigin.getZ() + random.nextBetween(-config.nestSpreadRadius(), config.nestSpreadRadius());

            BlockPos floorPos = findFloorPosition(world, new BlockPos(x, actualOrigin.getY(), z));
            if (floorPos != null && canPlaceNest(world, floorPos)) {
                world.setBlockState(floorPos, WebBlocks.SPIDER_MOSS.getDefaultState(), Block.NOTIFY_ALL);
                nestPositions.add(floorPos);

                if (random.nextFloat() < 0.7f) {
                    for (BlockPos nearbyPos : BlockPos.iterate(floorPos.add(-3, 0, -3), floorPos.add(3, 0, 3))) {
                        if (random.nextFloat() < 0.4f && canPlaceNest(world, nearbyPos)) {
                            world.setBlockState(nearbyPos, WebBlocks.SPIDER_MOSS.getDefaultState(), Block.NOTIFY_ALL);
                            nestPositions.add(nearbyPos.toImmutable());
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 18; i++) {
            int x = actualOrigin.getX() + random.nextBetween(-4, 4);
            int z = actualOrigin.getZ() + random.nextBetween(-4, 4);

            BlockPos floorPos = findFloorPosition(world, new BlockPos(x, actualOrigin.getY(), z));
            if (floorPos != null && canPlaceNest(world, floorPos)) {
                world.setBlockState(floorPos, WebBlocks.SPIDER_MOSS.getDefaultState(), Block.NOTIFY_ALL);
                nestPositions.add(floorPos);
            }
        }

        generateEggsOnNests(world, nestPositions, random, config, mainEggPositions);
        generateStandaloneEggs(world, actualOrigin, random, nestPositions, mainEggPositions);
        generateSpiderWebBlocks(world, actualOrigin, random, nestPositions);
        placeSpiderEggShells(world, actualOrigin, nestPositions, random, config);

        return !nestPositions.isEmpty();
    }

    private boolean generateMainNestStructures(StructureWorldAccess world, BlockPos origin, Random random, Set<BlockPos> mainEggPositions) {
        boolean placedAny = false;
        int attempts = 0;
        int structures = 0;

        while (structures < 2 && attempts < 20) {
            attempts++;

            int x = origin.getX() + random.nextBetween(-6, 6);
            int z = origin.getZ() + random.nextBetween(-6, 6);

            BlockPos floorPos = findFloorPosition(world, new BlockPos(x, origin.getY(), z));
            if (floorPos != null && canPlaceNest(world, floorPos)) {
                world.setBlockState(floorPos, WebBlocks.SPIDER_MOSS.getDefaultState(), Block.NOTIFY_ALL);

                BlockPos topNestPos = floorPos.up();
                if (world.getBlockState(topNestPos).isAir()) {
                    world.setBlockState(topNestPos, WebBlocks.SPIDER_MOSS.getDefaultState(), Block.NOTIFY_ALL);

                    BlockPos topEggPos = topNestPos.up();
                    if (world.getBlockState(topEggPos).isAir()) {
                        world.setBlockState(topEggPos, WebBlocks.SPIDER_EGG.getDefaultState(), Block.NOTIFY_ALL);
                        mainEggPositions.add(topEggPos);
                    }

                    Direction[] horizontalDirections = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
                    for (Direction direction : horizontalDirections) {
                        BlockPos sidePos = floorPos.offset(direction);
                        BlockPos sideEggPos = sidePos.up();
                        if (world.getBlockState(sideEggPos).isAir() && canReplaceForNest(world.getBlockState(sidePos))) {
                            world.setBlockState(sideEggPos, WebBlocks.SPIDER_EGG.getDefaultState(), Block.NOTIFY_ALL);
                            mainEggPositions.add(sideEggPos);
                        }
                    }

                    structures++;
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }

    private void generateEggsOnNests(StructureWorldAccess world, Set<BlockPos> nestPositions, Random random, SpiderEggClusterFeatureConfig config, Set<BlockPos> mainEggPositions) {
        int maxEggs = Math.max(6, nestPositions.size() / 4);
        int eggsPlaced = 0;
        Set<BlockPos> usedEggPositions = new HashSet<>(mainEggPositions);

        for (BlockPos nestPos : nestPositions) {
            if (eggsPlaced >= maxEggs) break;

            BlockPos eggPos = nestPos.up();
            if (mainEggPositions.contains(eggPos)) continue;

            if (random.nextFloat() < (config.eggChance() * 1.2f) && world.getBlockState(eggPos).isAir()) {
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
    }

    private void generateStandaloneEggs(StructureWorldAccess world, BlockPos origin, Random random, Set<BlockPos> nestPositions, Set<BlockPos> mainStructurePositions) {
        Set<BlockPos> usedEggPositions = new HashSet<>(mainStructurePositions);

        for (BlockPos nestPos : nestPositions) {
            BlockPos eggPos = nestPos.up();
            if (world.getBlockState(eggPos).isOf(WebBlocks.SPIDER_EGG)) {
                usedEggPositions.add(eggPos);
            }
        }

        for (int i = 0; i < 3; i++) {
            int x = origin.getX() + random.nextBetween(-5, 5);
            int z = origin.getZ() + random.nextBetween(-5, 5);
            BlockPos centerPos = new BlockPos(x, origin.getY(), z);

            BlockPos eggPos = findFloorPosition(world, centerPos);
            if (eggPos != null && world.getBlockState(eggPos.up()).isAir() && !mainStructurePositions.contains(eggPos.up())) {
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
    }

    private boolean isNearMainStructure(BlockPos pos, Set<BlockPos> mainStructurePositions) {
        for (BlockPos structurePos : mainStructurePositions) {
            if (pos.getSquaredDistance(structurePos) <= 9) {
                return true;
            }
        }
        return false;
    }

    private void generateSpiderWebBlocks(StructureWorldAccess world, BlockPos origin, Random random, Set<BlockPos> nestPositions) {
        if (random.nextFloat() < 0.3f) {
            generateHangingWebs(world, origin, random);

            int webCount = random.nextBetween(2, 3);
            int webBlocksPlaced = 0;

            for (int attempts = 0; attempts < 15 && webBlocksPlaced < webCount; attempts++) {
                int x = origin.getX() + random.nextBetween(-8, 8);
                int y = origin.getY() + random.nextBetween(-2, 4);
                int z = origin.getZ() + random.nextBetween(-8, 8);

                BlockPos webPos = new BlockPos(x, y, z);

                if (world.getBlockState(webPos).isAir() && hasValidWebSupport(world, webPos)) {
                    BlockState groundWebState = WebBlocks.SPIDER_WEB_BLOCK.getDefaultState()
                            .with(net.minecraft.state.property.Properties.FACING, Direction.UP)
                            .with(SpiderWebBlock.WEB_TYPE, SpiderWebBlock.WebType.GROUND);
                    world.setBlockState(webPos, groundWebState, Block.NOTIFY_ALL);
                    webBlocksPlaced++;
                }
            }
        }
    }

    private void generateHangingWebs(StructureWorldAccess world, BlockPos origin, Random random) {
        int hangingWebCount = random.nextBetween(4, 7);
        int hangingWebsPlaced = 0;

        for (int attempts = 0; attempts < 20 && hangingWebsPlaced < hangingWebCount; attempts++) {
            int x = origin.getX() + random.nextBetween(-10, 10);
            int z = origin.getZ() + random.nextBetween(-10, 10);

            BlockPos ceilingPos = findCeiling(world, new BlockPos(x, origin.getY() + 8, z));
            if (ceilingPos != null) {
                int webChainLength = random.nextBetween(2, 5);
                boolean placedAny = false;

                Direction facing = getRandomHorizontalDirection(random);

                for (int i = 0; i < webChainLength; i++) {
                    BlockPos webPos = ceilingPos.down(i + 1);

                    if (!world.getBlockState(webPos).isAir() || webPos.getY() <= origin.getY() - 3) {
                        break;
                    }

                    if (world.getBlockState(webPos.down()).isOf(WebBlocks.SPIDER_EGG)) {
                        break;
                    }

                    SpiderWebBlock.WebType webType;
                    if (webChainLength == 1) {
                        webType = SpiderWebBlock.WebType.HANGING_1;
                    } else if (i == 0) {
                        webType = SpiderWebBlock.WebType.HANGING_TOP;
                    } else if (i == webChainLength - 1) {
                        webType = SpiderWebBlock.WebType.HANGING_TIP;
                    } else {
                        webType = SpiderWebBlock.WebType.HANGING_MIDDLE;
                    }

                    BlockState hangingWebState = WebBlocks.SPIDER_WEB_BLOCK.getDefaultState()
                            .with(net.minecraft.state.property.Properties.FACING, facing)
                            .with(SpiderWebBlock.WEB_TYPE, webType);

                    world.setBlockState(webPos, hangingWebState, Block.NOTIFY_ALL);
                    placedAny = true;
                }

                if (placedAny) {
                    hangingWebsPlaced++;
                }
            }
        }
    }

    private Direction getRandomHorizontalDirection(Random random) {
        Direction[] horizontalDirections = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        return horizontalDirections[random.nextInt(horizontalDirections.length)];
    }

    private BlockPos findCeiling(StructureWorldAccess world, BlockPos startPos) {
        for (int y = startPos.getY(); y <= startPos.getY() + 15; y++) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
            BlockState state = world.getBlockState(checkPos);

            if (state.isSolidBlock(world, checkPos)) {
                BlockPos belowPos = checkPos.down();
                if (world.getBlockState(belowPos).isAir() &&
                        world.getBlockState(belowPos.down()).isAir()) {
                    return checkPos;
                }
            }
        }
        return null;
    }

    private boolean hasNearbySupport(StructureWorldAccess world, BlockPos pos) {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        for (Direction direction : directions) {
            BlockPos checkPos = pos.offset(direction);
            BlockState state = world.getBlockState(checkPos);
            if (state.isSolidBlock(world, checkPos) || state.isOf(WebBlocks.SPIDER_MOSS)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasValidWebSupport(StructureWorldAccess world, BlockPos pos) {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        for (Direction direction : directions) {
            BlockPos checkPos = pos.offset(direction);
            BlockState state = world.getBlockState(checkPos);

            if (state.isOf(Blocks.DEEPSLATE) ||
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
                    state.isOf(WebBlocks.SPIDER_MOSS)) {
                return true;
            }
        }
        return false;
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

        int guaranteedShells = Math.max(10, nestPositions.size() / 3);
        int shellsPlaced = 0;

        for (int attempts = 0; attempts < 80 && shellsPlaced < guaranteedShells + 20; attempts++) {
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
        int targetNestsWithShells = Math.max(5, nestPositions.size() / 3);

        for (BlockPos nestPos : nestPositions) {
            if (nestsWithShells >= targetNestsWithShells) break;

            boolean shouldPlaceShells = nestsWithShells < (targetNestsWithShells / 2) || random.nextFloat() < 0.6f;

            if (shouldPlaceShells) {
                int shellCount = random.nextBetween(1, 4);
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
                state.isOf(WebBlocks.SPIDER_MOSS);
    }
}