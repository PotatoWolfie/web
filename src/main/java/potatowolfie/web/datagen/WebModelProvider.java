package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.TexturedModel;
import potatowolfie.web.block.WebBlocks;

public class WebModelProvider extends FabricModelProvider {
    public WebModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.createColoredBlockWithRandomRotations(TexturedModel.CUBE, WebBlocks.SPIDER_MOSS);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {

    }
}