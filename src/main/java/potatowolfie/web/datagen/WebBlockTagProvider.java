package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.util.WebTags;

import java.util.concurrent.CompletableFuture;

public class WebBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public WebBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(BlockTags.MINEABLE_WITH_HOE)
                .add(
                        key(WebBlocks.SPIDER_MOSS),
                        key(WebBlocks.SPIDER_MOSS_CARPET)
                );

        builder(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(
                        key(WebBlocks.DAMP_SOIL),
                        key(WebBlocks.SPROUTING_DAMP_SOIL),
                        key(WebBlocks.DAMP_SOIL_PATH)
                );

        builder(BlockTags.SWORD_EFFICIENT)
                .add(
                        key(WebBlocks.SPIDER_MOSS),
                        key(WebBlocks.SPIDER_MOSS_CARPET),
                        key(WebBlocks.SPIDER_WEB_BLOCK),
                        key(WebBlocks.SPIDER_GRASS),
                        key(WebBlocks.SPIDER_SUCCULENT)
                );

        builder(BlockTags.DIRT)
                .add(
                        key(WebBlocks.SPIDER_MOSS),
                        key(WebBlocks.DAMP_SOIL),
                        key(WebBlocks.SPROUTING_DAMP_SOIL),
                        key(WebBlocks.DAMP_SOIL_PATH)
                );

        builder(BlockTags.SUPPORTS_VEGETATION)
                .add(
                        key(WebBlocks.SPIDER_MOSS),
                        key(WebBlocks.DAMP_SOIL),
                        key(WebBlocks.SPROUTING_DAMP_SOIL)
                );

        builder(BlockTags.REPLACEABLE)
                .add(key(WebBlocks.SPIDER_GRASS));

        builder(BlockTags.REPLACEABLE_BY_TREES)
                .add(key(WebBlocks.SPIDER_GRASS));

        builder(BlockTags.REPLACEABLE_BY_MUSHROOMS)
                .add(key(WebBlocks.SPIDER_GRASS));

        builder(BlockTags.WASHED_AWAY_BY_FLUIDS)
                .add(
                        key(WebBlocks.SPIDER_GRASS),
                        key(WebBlocks.SPIDER_EGG_SHELLS)
                );

        builder(BlockTags.FALL_DAMAGE_RESETTING)
                .add(key(WebBlocks.SPIDER_WEB_BLOCK));

        builder(WebTags.Blocks.INCORRECT_FOR_FANG_TOOL)
                .forceAddTag(BlockTags.NEEDS_DIAMOND_TOOL);

        builder(WebTags.Blocks.INCORRECT_FOR_ABOMINABLE_FANG_TOOL)
                .forceAddTag(BlockTags.NEEDS_DIAMOND_TOOL);
    }

    private static ResourceKey<Block> key(Block block) {
        return BuiltInRegistries.BLOCK.wrapAsHolder(block).unwrapKey().orElseThrow();
    }
}