package potatowolfie.web.world.structure.nook;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import potatowolfie.web.world.structure.WebStructureTypes;
import potatowolfie.web.world.structure.nook.NookGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NookStructure extends Structure {
    public static final MapCodec<NookStructure> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    settingsCodec(instance),
                    Codec.intRange(-64, 320).optionalFieldOf("min_y", -50).forGetter((structure) -> structure.minY),
                    Codec.intRange(-64, 320).optionalFieldOf("max_y", 30).forGetter((structure) -> structure.maxY),
                    Codec.intRange(-64, 64).optionalFieldOf("y_offset", 0).forGetter((structure) -> structure.yOffset),
                    Codec.doubleRange(0.0, 1.0).optionalFieldOf("min_open_ratio", 0.4).forGetter((structure) -> structure.minOpenRatio)
            ).apply(instance, NookStructure::new)
    );

    private static final int HEADROOM = 3;
    private static final int SAMPLE_STEP = 4;
    private static final int FLOOR_WINDOW = 3;
    private static final int NO_FLOOR = Integer.MIN_VALUE;

    public final int minY;
    public final int maxY;
    public final int yOffset;
    public final double minOpenRatio;

    public NookStructure(Structure.StructureSettings config, int minY, int maxY, int yOffset, double minOpenRatio) {
        super(config);
        this.minY = minY;
        this.maxY = maxY;
        this.yOffset = yOffset;
        this.minOpenRatio = minOpenRatio;
    }

    private record Candidate(int x, int floorY, int z) {
    }

    private record Placement(BlockPos origin, double score) {
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();

        StructureTemplate template = context.structureTemplateManager().getOrCreate(NookGenerator.NOOK_TEMPLATE);
        Vec3i size = template.getSize();
        if (size.getY() <= 0) {
            return Optional.empty();
        }

        int worldMin = context.heightAccessor().getMinY();
        int worldMax = context.heightAccessor().getMaxY();
        int lowest = Math.max(this.minY, worldMin + 1);
        int highest = Math.min(this.maxY, worldMax - HEADROOM - 1);

        Rotation rotation = Rotation.getRandom(context.random());
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(Mirror.NONE);

        BlockPos centerOffset = StructureTemplate.transform(
                new BlockPos(NookGenerator.CENTER_X, 0, NookGenerator.CENTER_Z),
                Mirror.NONE, rotation, BlockPos.ZERO
        );

        Map<Long, NoiseColumn> columns = new HashMap<>();
        List<Candidate> candidates = new ArrayList<>();

        for (int dx = 2; dx < 16; dx += 4) {
            for (int dz = 2; dz < 16; dz += 4) {
                int x = chunkPos.getMinBlockX() + dx;
                int z = chunkPos.getMinBlockZ() + dz;
                NoiseColumn column = getColumn(context, columns, x, z);

                for (int y = lowest; y <= highest; y++) {
                    if (!isCaveFloor(column, y)) {
                        continue;
                    }
                    if (isInValidBiome(context, new BlockPos(x, y, z))) {
                        candidates.add(new Candidate(x, y, z));
                    }
                }
            }
        }

        Collections.shuffle(candidates, new java.util.Random(context.random().nextLong()));

        BlockPos bestOrigin = null;
        double bestScore = -1.0;

        for (Candidate candidate : candidates) {
            Placement placement = check(
                    context, columns, template, settings, centerOffset, candidate,
                    this.yOffset, this.minOpenRatio, worldMin, worldMax
            );
            if (placement != null && placement.score() > bestScore) {
                bestScore = placement.score();
                bestOrigin = placement.origin();
            }
        }

        if (bestOrigin == null) {
            return Optional.empty();
        }

        final BlockPos finalOrigin = bestOrigin;
        return Optional.of(new Structure.GenerationStub(finalOrigin, (collector) ->
                NookGenerator.addPieces(context.structureTemplateManager(), finalOrigin, rotation, collector)
        ));
    }

    private static Placement check(
            Structure.GenerationContext context,
            Map<Long, NoiseColumn> columns,
            StructureTemplate template,
            StructurePlaceSettings settings,
            BlockPos centerOffset,
            Candidate candidate,
            int yOffset,
            double minOpenRatio,
            int worldMin,
            int worldMax
    ) {
        int originX = candidate.x() - centerOffset.getX();
        int originZ = candidate.z() - centerOffset.getZ();

        BoundingBox flat = template.getBoundingBox(settings, new BlockPos(originX, candidate.floorY(), originZ));
        int[] xs = axisSamples(flat.minX() - 1, flat.maxX() + 1);
        int[] zs = axisSamples(flat.minZ() - 1, flat.maxZ() + 1);

        List<Integer> floors = new ArrayList<>();
        int total = 0;
        for (int x : xs) {
            for (int z : zs) {
                if (!isInside(flat, x, z)) {
                    continue;
                }
                total++;
                int floor = findFloor(getColumn(context, columns, x, z), candidate.floorY());
                if (floor != NO_FLOOR) {
                    floors.add(floor);
                }
            }
        }

        if (total == 0 || floors.isEmpty()) {
            return null;
        }
        double score = (double) floors.size() / (double) total;
        if (score < minOpenRatio) {
            return null;
        }

        Collections.sort(floors);
        int medianFloor = floors.get(floors.size() / 2);

        BlockPos origin = new BlockPos(originX, medianFloor + 1 - NookGenerator.SURFACE_Y + yOffset, originZ);
        BoundingBox box = template.getBoundingBox(settings, origin);
        if (box.minY() - 1 <= worldMin || box.maxY() + 1 >= worldMax) {
            return null;
        }

        for (int x : xs) {
            for (int z : zs) {
                NoiseColumn column = getColumn(context, columns, x, z);
                for (int y = box.minY() - 1; y <= box.maxY() + 1; y++) {
                    if (!column.getBlock(y).getFluidState().isEmpty()) {
                        return null;
                    }
                }
            }
        }

        return new Placement(origin, score);
    }

    private static int findFloor(NoiseColumn column, int aroundY) {
        for (int y = aroundY + FLOOR_WINDOW; y >= aroundY - FLOOR_WINDOW; y--) {
            BlockState state = column.getBlock(y);
            if (!state.isAir()
                    && state.getFluidState().isEmpty()
                    && column.getBlock(y + 1).isAir()
                    && column.getBlock(y + 2).isAir()) {
                return y;
            }
        }
        return NO_FLOOR;
    }

    private static boolean isInside(BoundingBox box, int x, int z) {
        return x >= box.minX() && x <= box.maxX() && z >= box.minZ() && z <= box.maxZ();
    }

    private static int[] axisSamples(int lo, int hi) {
        int n = (hi - lo) / SAMPLE_STEP + 1;
        boolean needsEnd = lo + (n - 1) * SAMPLE_STEP != hi;
        int[] result = new int[n + (needsEnd ? 1 : 0)];
        for (int i = 0; i < n; i++) {
            result[i] = lo + i * SAMPLE_STEP;
        }
        if (needsEnd) {
            result[n] = hi;
        }
        return result;
    }

    private static NoiseColumn getColumn(
            Structure.GenerationContext context,
            Map<Long, NoiseColumn> cache,
            int x,
            int z
    ) {
        long key = ((long) x << 32) | (z & 0xFFFFFFFFL);
        return cache.computeIfAbsent(key, k -> context.chunkGenerator().getBaseColumn(
                x, z, context.heightAccessor(), context.randomState()
        ));
    }

    private static boolean isCaveFloor(NoiseColumn column, int y) {
        BlockState floor = column.getBlock(y);
        if (floor.isAir() || !floor.getFluidState().isEmpty()) {
            return false;
        }

        for (int i = 1; i <= HEADROOM; i++) {
            if (!column.getBlock(y + i).isAir()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isInValidBiome(Structure.GenerationContext context, BlockPos pos) {
        Holder<Biome> biome = context.biomeResolver().getNoiseBiome(
                QuartPos.fromBlock(pos.getX()),
                QuartPos.fromBlock(pos.getY()),
                QuartPos.fromBlock(pos.getZ())
        );
        return context.validBiome().test(biome);
    }

    @Override
    public StructureType<?> type() {
        return WebStructureTypes.NOOK;
    }
}