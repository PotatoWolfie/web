package potatowolfie.web.world.structure.nook;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.LootTable;
import potatowolfie.web.Web;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.block.custom.EggStage;
import potatowolfie.web.block.custom.SpiderEggBlock;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.abominable_spider_queen.AbominableSpiderQueenEntity;
import potatowolfie.web.world.structure.WebStructurePieceTypes;

public class NookGenerator {
    public static final Identifier NOOK_TEMPLATE =
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "nook");

    private static final ResourceKey<LootTable> NOOK_LOOT_TABLE = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "chests/nook")
    );

    public static final int CENTER_X = 11;
    public static final int CENTER_Z = 11;
    public static final int SURFACE_Y = 2;

    private static final int MIN_EGG_HATCH_TICKS = 18 * 60 * 20;
    private static final int MAX_EGG_HATCH_TICKS = 24 * 60 * 20;
    private static final int MAX_SUPPORT_DEPTH = 48;

    public static void addPieces(
            StructureTemplateManager manager,
            BlockPos pos,
            Rotation rotation,
            StructurePiecesBuilder holder
    ) {
        holder.addPiece(new Piece(manager, NOOK_TEMPLATE, pos, rotation));
    }

    public static class Piece extends TemplateStructurePiece {

        public Piece(
                StructureTemplateManager manager,
                Identifier template,
                BlockPos pos,
                Rotation rotation
        ) {
            super(
                    WebStructurePieceTypes.NOOK,
                    0,
                    manager,
                    template,
                    template.toString(),
                    createPlacementData(rotation),
                    pos
            );
        }

        public Piece(StructureTemplateManager manager, CompoundTag nbt) {
            super(
                    WebStructurePieceTypes.NOOK,
                    nbt,
                    manager,
                    (identifier) -> createPlacementData(
                            nbt.read("Rot", Rotation.CODEC).orElse(Rotation.NONE)
                    )
            );
        }

        private static StructurePlaceSettings createPlacementData(Rotation rotation) {
            return new StructurePlaceSettings()
                    .setRotation(rotation)
                    .setMirror(Mirror.NONE)
                    .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag nbt) {
            super.addAdditionalSaveData(context, nbt);
            nbt.store("Rot", Rotation.CODEC, this.placeSettings.getRotation());
        }

        @Override
        public void postProcess(
                WorldGenLevel world,
                StructureManager structureManager,
                ChunkGenerator chunkGenerator,
                RandomSource random,
                BoundingBox chunkBox,
                ChunkPos chunkPos,
                BlockPos pivot
        ) {
            super.postProcess(world, structureManager, chunkGenerator, random, chunkBox, chunkPos, pivot);
            supportFloatingColumns(world, chunkBox, random);
            sealFluids(world, chunkBox, random);
            spawnSpiderQueen(world, chunkBox);
        }

        private void supportFloatingColumns(WorldGenLevel world, BoundingBox chunkBox, RandomSource random) {
            BoundingBox box = this.getBoundingBox();
            int baseY = box.minY();
            int worldMin = world.getMinY();
            int lowestFill = Math.max(worldMin, baseY - MAX_SUPPORT_DEPTH);

            int minX = Math.max(box.minX(), chunkBox.minX());
            int maxX = Math.min(box.maxX(), chunkBox.maxX());
            int minZ = Math.max(box.minZ(), chunkBox.minZ());
            int maxZ = Math.min(box.maxZ(), chunkBox.maxZ());

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, baseY, z);
                    BlockState base = world.getBlockState(pos);

                    if (base.isAir() || isPureFluid(base)) {
                        continue;
                    }

                    for (int y = baseY - 1; y >= lowestFill; y--) {
                        pos.set(x, y, z);
                        if (!isReplaceable(world.getBlockState(pos))) {
                            break;
                        }
                        world.setBlock(pos, mainStone(y, random), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }

        private void sealFluids(WorldGenLevel world, BoundingBox chunkBox, RandomSource random) {
            BoundingBox box = this.getBoundingBox();

            int minX = Math.max(box.minX() - 1, chunkBox.minX());
            int maxX = Math.min(box.maxX() + 1, chunkBox.maxX());
            int minZ = Math.max(box.minZ() - 1, chunkBox.minZ());
            int maxZ = Math.min(box.maxZ() + 1, chunkBox.maxZ());
            int minY = Math.max(box.minY() - 1, world.getMinY());
            int maxY = Math.min(box.maxY() + 1, world.getMaxY());

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        pos.set(x, y, z);
                        if (isPureFluid(world.getBlockState(pos))) {
                            world.setBlock(pos, mainStone(y, random), Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }

        private static boolean isPureFluid(BlockState state) {
            return state.is(Blocks.WATER) || state.is(Blocks.LAVA);
        }

        private static boolean isReplaceable(BlockState state) {
            return state.isAir() || !state.getFluidState().isEmpty() || state.canBeReplaced();
        }

        private static BlockState mainStone(int y, RandomSource random) {
            if (y < 0) {
                return Blocks.DEEPSLATE.defaultBlockState();
            }
            if (y >= 8) {
                return Blocks.STONE.defaultBlockState();
            }
            return random.nextInt(8) < (8 - y)
                    ? Blocks.DEEPSLATE.defaultBlockState()
                    : Blocks.STONE.defaultBlockState();
        }

        private void spawnSpiderQueen(WorldGenLevel world, BoundingBox chunkBox) {
            BlockPos spawnPos = StructureTemplate.transform(
                    new BlockPos(CENTER_X, SURFACE_Y, CENTER_Z),
                    this.placeSettings.getMirror(),
                    this.placeSettings.getRotation(),
                    this.placeSettings.getRotationPivot()
            ).offset(this.templatePosition);

            if (!chunkBox.isInside(spawnPos)) {
                return;
            }

            AbominableSpiderQueenEntity queen = new AbominableSpiderQueenEntity(
                    WebEntities.ABOMINABLE_SPIDER_QUEEN, world.getLevel()
            );
            queen.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0F, 0.0F);
            queen.finalizeSpawn(
                    world,
                    world.getCurrentDifficultyAt(spawnPos),
                    EntitySpawnReason.STRUCTURE,
                    null
            );
            queen.setPersistenceRequired();
            world.addFreshEntity(queen);
        }

        @Override
        protected void handleDataMarker(
                String metadata,
                BlockPos pos,
                ServerLevelAccessor world,
                RandomSource random,
                BoundingBox boundingBox
        ) {
            switch (metadata) {
                case "chest" -> placeLootChest(world, pos, random, boundingBox);
                case "egg" -> placeSpiderEgg(world, pos, random, boundingBox);
                default -> {
                }
            }
        }

        private void placeLootChest(
                ServerLevelAccessor world,
                BlockPos markerPos,
                RandomSource random,
                BoundingBox boundingBox
        ) {
            if (boundingBox.isInside(markerPos)) {
                world.setBlock(markerPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }

            BlockPos chestPos = markerPos.below();
            if (!boundingBox.isInside(chestPos)) {
                return;
            }

            BlockState existing = world.getBlockState(chestPos);
            if (!existing.is(Blocks.CHEST)) {
                world.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);
            }

            BlockEntity blockEntity = world.getBlockEntity(chestPos);
            if (blockEntity instanceof ChestBlockEntity chestEntity) {
                chestEntity.setLootTable(NOOK_LOOT_TABLE, random.nextLong());
            }
        }

        private void placeSpiderEgg(
                ServerLevelAccessor world,
                BlockPos pos,
                RandomSource random,
                BoundingBox boundingBox
        ) {
            if (!boundingBox.isInside(pos)) {
                return;
            }

            int hatchTicks = MIN_EGG_HATCH_TICKS
                    + random.nextInt(MAX_EGG_HATCH_TICKS - MIN_EGG_HATCH_TICKS + 1);

            BlockState eggState = WebBlocks.SPIDER_EGG.defaultBlockState()
                    .setValue(SpiderEggBlock.STAGE, EggStage.ACTIVE)
                    .setValue(SpiderEggBlock.PREVENTED, false)
                    .setValue(SpiderEggBlock.SPAWNED_PROTECTORS, false)
                    .setValue(SpiderEggBlock.REMAINING_TIME, hatchTicks)
                    .setValue(SpiderEggBlock.COVERED, false);

            world.setBlock(pos, eggState, Block.UPDATE_CLIENTS);
            world.scheduleTick(pos, WebBlocks.SPIDER_EGG, 20);
        }
    }
}