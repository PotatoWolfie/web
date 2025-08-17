package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.TexturedModel;
import potatowolfie.web.block.WebBlocks;

public class WebModelProvider extends FabricModelProvider {
    public WebModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerRandomHorizontalRotations(TexturedModel.CUBE_ALL, WebBlocks.SPIDER_NEST);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {

    }
}