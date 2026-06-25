package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.world.level.block.Blocks;
import potatowolfie.web.block.WebBlocks;

public class WebModelProvider extends FabricModelProvider {
    public WebModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.createFullAndCarpetBlocks(WebBlocks.SPIDER_MOSS, WebBlocks.SPIDER_MOSS_CARPET);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {

    }
}