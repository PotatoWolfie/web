package potatowolfie.web.world.feature.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.block.custom.EggStage;
import potatowolfie.web.block.custom.SpiderEggBlock;
import potatowolfie.web.block.custom.SpiderEggShellsBlock;
import potatowolfie.web.block.custom.SpiderSucculentBlock;
import potatowolfie.web.block.custom.SpiderWebBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.chunk.ChunkGenerator;

public record SpiderEggClusterFeature(
        int minClusterSize,
        int maxClusterSize,
        int nestSpreadRadius,
        float eggChance,
        float oldClusterChance
) implements Feature {

    public static final MapCodec<SpiderEggClusterFeature> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.intRange(1, 20).fieldOf("min_cluster_size").forGetter(SpiderEggClusterFeature::minClusterSize),
                    Codec.intRange(1, 40).fieldOf("max_cluster_size").forGetter(SpiderEggClusterFeature::maxClusterSize),
                    Codec.intRange(1, 10).fieldOf("nest_spread_radius").forGetter(SpiderEggClusterFeature::nestSpreadRadius),
                    Codec.floatRange(0.0f, 1.0f).fieldOf("egg_chance").forGetter(SpiderEggClusterFeature::eggChance),
                    Codec.floatRange(0.0f, 1.0f).optionalFieldOf("old_cluster_chance", 0.05f).forGetter(SpiderEggClusterFeature::oldClusterChance)
            ).apply(instance, SpiderEggClusterFeature::new)
    );

    public MapCodec<SpiderEggClusterFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockPos actualOrigin = findValidOrigin(world, origin);
        if (actualOrigin == null) {
            return false;
        }

        if (!isValidCaveLocation(world, actualOrigin)) {
            return false;
        }

        boolean isOldCluster = random.nextFloat() < oldClusterChance();

        Set<BlockPos> mainEggPositions = new HashSet<>();
        boolean generatedMainNests = generateNestStructures(world, actualOrigin, random, mainEggPositions, isOldCluster);
        if (!generatedMainNests) {
            return false;
        }

        int clusterSize = random.nextIntBetweenInclusive(22, 45);
        Set<BlockPos> nestPositions = new HashSet<>();

        for (int i = 0; i < clusterSize; i++) {
            int x = actualOrigin.getX() + random.nextIntBetweenInclusive(-nestSpreadRadius(), nestSpreadRadius());
            int z = actualOrigin.getZ() + random.nextIntBetweenInclusive(-nestSpreadRadius(), nestSpreadRadius());

            BlockPos floorPos = findFloorPosition(world, new BlockPos(x, actualOrigin.getY(), z));
            if (floorPos != null && canPlaceNest(world, floorPos)) {
                world.setBlock(floorPos, WebBlocks.SPIDER_MOSS.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
                nestPositions.add(floorPos);

                if (random.nextFloat() < 0.7f) {
                    for (BlockPos nearbyPos : BlockPos.betweenClosed(floorPos.offset(-3, 0, -3), floorPos.offset(3, 0, 3))) {
                        if (random.nextFloat() < 0.4f && canPlaceNest(world, nearbyPos)) {
                            world.setBlock(nearbyPos, WebBlocks.SPIDER_MOSS.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
                            nestPositions.add(nearbyPos.immutable());
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 18; i++) {
            int x = actualOrigin.getX() + random.nextIntBetweenInclusive(-4, 4);
            int z = actualOrigin.getZ() + random.nextIntBetweenInclusive(-4, 4);

            BlockPos floorPos = findFloorPosition(world, new BlockPos(x, actualOrigin.getY(), z));
            if (floorPos != null && canPlaceNest(world, floorPos)) {
                world.setBlock(floorPos, WebBlocks.SPIDER_MOSS.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);
                nestPositions.add(floorPos);
            }
        }

        generateEggs(world, nestPositions, random, mainEggPositions, isOldCluster);
        generateStandaloneEggs(world, actualOrigin, random, nestPositions, mainEggPositions, isOldCluster);
        generateSpiderWebs(world, actualOrigin, random, nestPositions);
        generateSpiderGrass(world, nestPositions, random);
        generateSpiderEggShells(world, actualOrigin, nestPositions, random);
        generateSpiderSucculents(world, nestPositions, random);

        return !nestPositions.isEmpty();
    }

    private boolean generateNestStructures(WorldGenLevel world, BlockPos origin, RandomSource random, Set<BlockPos> mainEggPositions, boolean isOldCluster) {
        boolean placedAny = false;
        int attempts = 0;
        int structures = 0;

        while (structures < 2 && attempts < 20) {
            attempts++;

            int x = origin.getX() + random.nextIntBetweenInclusive(-6, 6);
            int z = origin.getZ() + random.nextIntBetweenInclusive(-6, 6);

            BlockPos floorPos = findFloorPosition(world, new BlockPos(x, origin.getY(), z));
            if (floorPos != null && canPlaceNest(world, floorPos)) {
                world.setBlock(floorPos, WebBlocks.SPIDER_MOSS.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);

                BlockPos topNestPos = floorPos.above();
                if (world.getBlockState(topNestPos).isAir()) {
                    world.setBlock(topNestPos, WebBlocks.SPIDER_MOSS.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE);

                    BlockPos topEggPos = topNestPos.above();
                    if (world.getBlockState(topEggPos).isAir()) {
                        world.setBlock(topEggPos, getEggBlockState(isOldCluster, random), Block.UPDATE_ALL);
                        mainEggPositions.add(topEggPos);
                    }

                    Direction[] horizontalDirections = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
                    for (Direction direction : horizontalDirections) {
                        BlockPos sidePos = floorPos.relative(direction);
                        BlockPos sideEggPos = sidePos.above();
                        if (world.getBlockState(sideEggPos).isAir() && canReplaceForNest(world.getBlockState(sidePos))) {
                            world.setBlock(sideEggPos, getEggBlockState(isOldCluster, random), Block.UPDATE_ALL);
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

    private void generateEggs(WorldGenLevel world, Set<BlockPos> nestPositions, RandomSource random, Set<BlockPos> mainEggPositions, boolean isOldCluster) {
        int maxEggs = Math.max(6, nestPositions.size() / 4);
        int eggsPlaced = 0;
        Set<BlockPos> usedEggPositions = new HashSet<>(mainEggPositions);

        for (BlockPos nestPos : nestPositions) {
            if (eggsPlaced >= maxEggs) break;

            BlockPos eggPos = nestPos.above();
            if (mainEggPositions.contains(eggPos)) continue;

            if (random.nextFloat() < (eggChance() * 1.2f) && world.getBlockState(eggPos).isAir()) {
                boolean canPlace = true;

                for (BlockPos usedPos : usedEggPositions) {
                    double distance = Math.sqrt(eggPos.distSqr(usedPos));
                    if (distance < 2.5) {
                        canPlace = false;
                        break;
                    }
                }

                if (canPlace) {
                    world.setBlock(eggPos, getEggBlockState(isOldCluster, random), Block.UPDATE_ALL);
                    usedEggPositions.add(eggPos);
                    eggsPlaced++;
                }
            }
        }
    }

    private void generateStandaloneEggs(WorldGenLevel world, BlockPos origin, RandomSource random, Set<BlockPos> nestPositions, Set<BlockPos> mainStructurePositions, boolean isOldCluster) {
        Set<BlockPos> usedEggPositions = new HashSet<>(mainStructurePositions);

        for (BlockPos nestPos : nestPositions) {
            BlockPos eggPos = nestPos.above();
            if (world.getBlockState(eggPos).is(WebBlocks.SPIDER_EGG)) {
                usedEggPositions.add(eggPos);
            }
        }

        for (int i = 0; i < 3; i++) {
            int x = origin.getX() + random.nextIntBetweenInclusive(-5, 5);
            int z = origin.getZ() + random.nextIntBetweenInclusive(-5, 5);
            BlockPos centerPos = new BlockPos(x, origin.getY(), z);

            BlockPos eggPos = findFloorPosition(world, centerPos);
            if (eggPos != null && world.getBlockState(eggPos.above()).isAir() && !mainStructurePositions.contains(eggPos.above())) {
                boolean canPlace = true;

                for (BlockPos usedPos : usedEggPositions) {
                    double distance = Math.sqrt(eggPos.above().distSqr(usedPos));
                    if (distance < 2.5) {
                        canPlace = false;
                        break;
                    }
                }

                if (canPlace) {
                    world.setBlock(eggPos.above(), getEggBlockState(isOldCluster, random), Block.UPDATE_ALL);
                    usedEggPositions.add(eggPos.above());
                }
            }
        }
    }

    private void generateSpiderWebs(WorldGenLevel world, BlockPos origin, RandomSource random, Set<BlockPos> nestPositions) {
        if (random.nextFloat() < 0.3f) {
            generateHangingWebs(world, origin, random);

            int webCount = random.nextIntBetweenInclusive(2, 3);
            int webBlocksPlaced = 0;

            for (int attempts = 0; attempts < 15 && webBlocksPlaced < webCount; attempts++) {
                int x = origin.getX() + random.nextIntBetweenInclusive(-8, 8);
                int y = origin.getY() + random.nextIntBetweenInclusive(-2, 4);
                int z = origin.getZ() + random.nextIntBetweenInclusive(-8, 8);

                BlockPos webPos = new BlockPos(x, y, z);

                if (world.getBlockState(webPos).isAir() && hasValidWebSupport(world, webPos)) {
                    BlockState groundWebState = WebBlocks.SPIDER_WEB_BLOCK.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.UP)
                            .setValue(SpiderWebBlock.WEB_TYPE, SpiderWebBlock.WebType.GROUND);
                    world.setBlock(webPos, groundWebState, Block.UPDATE_ALL);
                    webBlocksPlaced++;
                }
            }
        }
    }

    private void generateHangingWebs(WorldGenLevel world, BlockPos origin, RandomSource random) {
        int hangingWebCount = random.nextIntBetweenInclusive(4, 7);
        int hangingWebsPlaced = 0;

        for (int attempts = 0; attempts < 20 && hangingWebsPlaced < hangingWebCount; attempts++) {
            int x = origin.getX() + random.nextIntBetweenInclusive(-10, 10);
            int z = origin.getZ() + random.nextIntBetweenInclusive(-10, 10);

            BlockPos ceilingPos = findCeiling(world, new BlockPos(x, origin.getY() + 8, z));
            if (ceilingPos != null) {
                int webChainLength = random.nextIntBetweenInclusive(2, 5);
                boolean placedAny = false;

                Direction facing = getRandomHorizontalDirection(random);

                for (int i = 0; i < webChainLength; i++) {
                    BlockPos webPos = ceilingPos.below(i + 1);

                    if (!world.getBlockState(webPos).isAir() || webPos.getY() <= origin.getY() - 3) {
                        break;
                    }

                    if (world.getBlockState(webPos.below()).is(WebBlocks.SPIDER_EGG)) {
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

                    BlockState hangingWebState = WebBlocks.SPIDER_WEB_BLOCK.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, facing)
                            .setValue(SpiderWebBlock.WEB_TYPE, webType);

                    world.setBlock(webPos, hangingWebState, Block.UPDATE_ALL);
                    placedAny = true;
                }

                if (placedAny) {
                    hangingWebsPlaced++;
                }
            }
        }
    }

    private void generateSpiderGrass(WorldGenLevel world, Set<BlockPos> nestPositions, RandomSource random) {
        int targetGrassCount = Math.max(3, nestPositions.size() * random.nextIntBetweenInclusive(30, 45) / 100);
        int grassPlaced = 0;

        for (BlockPos nestPos : nestPositions) {
            if (grassPlaced >= targetGrassCount) break;

            BlockPos grassPos = nestPos.above();

            if (world.getBlockState(grassPos).isAir() &&
                    !world.getBlockState(grassPos).is(WebBlocks.SPIDER_EGG)) {

                if (random.nextFloat() < 0.35f) {
                    world.setBlock(grassPos, WebBlocks.SPIDER_GRASS.defaultBlockState(), Block.UPDATE_ALL);
                    grassPlaced++;
                }
            }
        }

        int additionalGrass = random.nextIntBetweenInclusive(2, 6);
        for (int i = 0; i < additionalGrass && grassPlaced < targetGrassCount + additionalGrass; i++) {
            if (nestPositions.isEmpty()) break;

            BlockPos[] nestArray = nestPositions.toArray(new BlockPos[0]);
            BlockPos baseNest = nestArray[random.nextInt(nestArray.length)];

            for (BlockPos nearbyPos : BlockPos.betweenClosed(
                    baseNest.offset(-2, -1, -2),
                    baseNest.offset(2, 1, 2))) {

                if (world.getBlockState(nearbyPos).is(WebBlocks.SPIDER_MOSS)) {
                    BlockPos grassPos = nearbyPos.above();

                    if (world.getBlockState(grassPos).isAir() &&
                            !world.getBlockState(grassPos).is(WebBlocks.SPIDER_EGG) &&
                            random.nextFloat() < 0.25f) {

                        world.setBlock(grassPos, WebBlocks.SPIDER_GRASS.defaultBlockState(), Block.UPDATE_ALL);
                        grassPlaced++;
                        break;
                    }
                }
            }
        }
    }

    private void generateSpiderSucculents(WorldGenLevel world, Set<BlockPos> nestPositions, RandomSource random) {
        if (nestPositions.isEmpty() || random.nextFloat() >= SUCCULENT_CLUSTER_CHANCE) {
            return;
        }

        int succulentCount = random.nextIntBetweenInclusive(2, 3);
        List<BlockPos> nests = new ArrayList<>(nestPositions);
        int placed = 0;

        for (int attempts = 0; attempts < 30 && placed < succulentCount; attempts++) {
            BlockPos nestPos = nests.get(random.nextInt(nests.size()));
            if (tryPlaceSucculent(world, nestPos, random)) {
                placed++;
            }
        }
    }

    private boolean tryPlaceSucculent(WorldGenLevel world, BlockPos floorPos, RandomSource random) {
        if (!world.getBlockState(floorPos).is(WebBlocks.SPIDER_MOSS)) {
            return false;
        }

        int targetRoots = random.nextInt(4);

        for (int i = 0; i <= targetRoots; i++) {
            if (!world.getBlockState(floorPos.above(i + 1)).isAir()) {
                return false;
            }
        }

        for (int i = 0; i <= targetRoots; i++) {
            BlockPos pos = floorPos.above(i + 1);
            SpiderSucculentBlock.Part part = (i < targetRoots)
                    ? SpiderSucculentBlock.Part.ROOT
                    : SpiderSucculentBlock.Part.BLOSSOM;

            BlockState state = WebBlocks.SPIDER_SUCCULENT.defaultBlockState()
                    .setValue(SpiderSucculentBlock.PART, part)
                    .setValue(SpiderSucculentBlock.TARGET_ROOTS, targetRoots);
            world.setBlock(pos, state, Block.UPDATE_ALL);

            if (i == targetRoots) {
                world.scheduleTick(pos, WebBlocks.SPIDER_SUCCULENT, 10 + random.nextInt(20));
            }
        }
        return true;
    }

    private Direction getRandomHorizontalDirection(RandomSource random) {
        Direction[] horizontalDirections = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        return horizontalDirections[random.nextInt(horizontalDirections.length)];
    }

    private BlockPos findCeiling(WorldGenLevel world, BlockPos startPos) {
        for (int y = startPos.getY(); y <= startPos.getY() + 15; y++) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
            BlockState state = world.getBlockState(checkPos);

            if (state.isRedstoneConductor(world, checkPos)) {
                BlockPos belowPos = checkPos.below();
                if (world.getBlockState(belowPos).isAir() &&
                        world.getBlockState(belowPos.below()).isAir()) {
                    return checkPos;
                }
            }
        }
        return null;
    }

    private boolean hasNearbySupport(WorldGenLevel world, BlockPos pos) {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        for (Direction direction : directions) {
            BlockPos checkPos = pos.relative(direction);
            BlockState state = world.getBlockState(checkPos);
            if (state.isRedstoneConductor(world, checkPos) || state.is(WebBlocks.SPIDER_MOSS)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasValidWebSupport(WorldGenLevel world, BlockPos pos) {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        for (Direction direction : directions) {
            BlockPos checkPos = pos.relative(direction);
            BlockState state = world.getBlockState(checkPos);

            if (state.is(Blocks.DEEPSLATE) ||
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
                    state.is(WebBlocks.SPIDER_MOSS)) {
                return true;
            }
        }
        return false;
    }

    private BlockPos findValidOrigin(WorldGenLevel world, BlockPos startPos) {
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
                    BlockPos cavePos = checkPos.above(offset);
                    if (isValidCaveLocation(world, cavePos)) {
                        return cavePos;
                    }
                }
            }
        }

        return null;
    }

    private boolean isValidCaveLocation(WorldGenLevel world, BlockPos pos) {
        if (pos.getY() > 55) return false;

        if (!world.getBlockState(pos).isAir()) return false;

        int solidBlocks = 0;
        int airBlocks = 0;

        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {
            BlockState state = world.getBlockState(checkPos);
            if (state.isAir()) {
                airBlocks++;
            } else if (state.isRedstoneConductor(world, checkPos)) {
                solidBlocks++;
            }
        }

        return airBlocks >= 5 && solidBlocks >= 8;
    }

    private BlockPos findFloorPosition(WorldGenLevel world, BlockPos startPos) {
        for (int y = startPos.getY() + 5; y >= startPos.getY() - 15; y--) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
            BlockState floorState = world.getBlockState(checkPos);
            BlockState aboveState = world.getBlockState(checkPos.above());

            if (canReplaceForNest(floorState) && aboveState.isAir()) {
                return checkPos;
            }
        }
        return null;
    }

    private boolean canPlaceNest(WorldGenLevel world, BlockPos pos) {
        BlockState floorState = world.getBlockState(pos);
        BlockState aboveState = world.getBlockState(pos.above());

        return canReplaceForNest(floorState) && aboveState.isAir();
    }

    private static final float SUCCULENT_CLUSTER_CHANCE = 0.05f;

    private static final int MIN_DEFAULT_HATCH_TICKS = 18 * 60 * 20;
    private static final int MAX_DEFAULT_HATCH_TICKS = 24 * 60 * 20;

    private BlockState getEggBlockState(boolean isOldCluster, RandomSource random) {
        BlockState state = WebBlocks.SPIDER_EGG.defaultBlockState();
        if (isOldCluster) {
            state = state.setValue(SpiderEggBlock.STAGE, EggStage.OLD);
        } else {
            int hatchTicks = MIN_DEFAULT_HATCH_TICKS + random.nextInt(MAX_DEFAULT_HATCH_TICKS - MIN_DEFAULT_HATCH_TICKS + 1);
            state = state.setValue(SpiderEggBlock.REMAINING_TIME, hatchTicks);
        }
        return state;
    }

    private boolean canReplaceForNest(BlockState state) {
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
                state.is(Blocks.MOSS_BLOCK) ||
                state.is(Blocks.CLAY) ||
                state.is(Blocks.COARSE_DIRT) ||
                state.is(Blocks.CALCITE) ||
                state.is(Blocks.SMOOTH_BASALT) ||
                state.is(Blocks.AMETHYST_BLOCK);
    }

    private void generateSpiderEggShells(WorldGenLevel world, BlockPos origin, Set<BlockPos> nestPositions, RandomSource random) {
        int shellRadius = nestSpreadRadius() + 4;

        int guaranteedShells = Math.max(10, nestPositions.size() / 3);
        int shellsPlaced = 0;

        for (int attempts = 0; attempts < 80 && shellsPlaced < guaranteedShells + 20; attempts++) {
            int x = origin.getX() + random.nextIntBetweenInclusive(-shellRadius, shellRadius);
            int y = origin.getY() + random.nextIntBetweenInclusive(-4, 5);
            int z = origin.getZ() + random.nextIntBetweenInclusive(-shellRadius, shellRadius);

            BlockPos shellPos = new BlockPos(x, y, z);

            float chance = shellsPlaced < guaranteedShells ? 0.85f : 0.6f;
            if (random.nextFloat() < chance) {
                if (tryPlaceShell(world, shellPos, random)) {
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
                int shellCount = random.nextIntBetweenInclusive(1, 4);
                for (int i = 0; i < shellCount; i++) {
                    int x = nestPos.getX() + random.nextIntBetweenInclusive(-5, 5);
                    int y = nestPos.getY() + random.nextIntBetweenInclusive(-3, 4);
                    int z = nestPos.getZ() + random.nextIntBetweenInclusive(-5, 5);

                    BlockPos shellPos = new BlockPos(x, y, z);
                    tryPlaceShell(world, shellPos, random);
                }
                nestsWithShells++;
            }
        }
    }

    private boolean tryPlaceShell(WorldGenLevel world, BlockPos pos, RandomSource random) {
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