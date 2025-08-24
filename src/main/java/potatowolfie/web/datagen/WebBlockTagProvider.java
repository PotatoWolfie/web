package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import potatowolfie.web.block.WebBlocks;

import java.util.concurrent.CompletableFuture;

public class WebBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public WebBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        valueLookupBuilder(BlockTags.HOE_MINEABLE)
                .add(WebBlocks.SPIDER_MOSS);

        valueLookupBuilder(BlockTags.SWORD_EFFICIENT)
                .add(WebBlocks.SPIDER_MOSS)
                .add(WebBlocks.SPIDER_WEB_BLOCK);

        valueLookupBuilder(BlockTags.DIRT)
                .add(WebBlocks.SPIDER_MOSS);
    }
}