package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import potatowolfie.web.block.WebBlocks;

import java.util.concurrent.CompletableFuture;

public class WebBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public WebBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(BlockTags.MINEABLE_WITH_HOE)
                .add(key(WebBlocks.SPIDER_MOSS));

        builder(BlockTags.SWORD_EFFICIENT)
                .add(
                        key(WebBlocks.SPIDER_MOSS),
                        key(WebBlocks.SPIDER_WEB_BLOCK)
                        );

        builder(BlockTags.DIRT)
                .add(key(WebBlocks.SPIDER_MOSS));
    }

    private static ResourceKey<Block> key(Block block) {
        return BuiltInRegistries.BLOCK.wrapAsHolder(block).unwrapKey().orElseThrow();
    }
}