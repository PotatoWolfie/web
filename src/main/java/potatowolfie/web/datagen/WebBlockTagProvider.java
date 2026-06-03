package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import potatowolfie.web.block.WebBlocks;

import java.util.concurrent.CompletableFuture;

public class WebBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public WebBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        valueLookupBuilder(BlockTags.MINEABLE_WITH_HOE)
                .add(WebBlocks.SPIDER_MOSS);

        valueLookupBuilder(BlockTags.SWORD_EFFICIENT)
                .add(WebBlocks.SPIDER_MOSS)
                .add(WebBlocks.SPIDER_WEB_BLOCK);

        valueLookupBuilder(BlockTags.DIRT)
                .add(WebBlocks.SPIDER_MOSS);
    }
}