package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.world.level.block.Blocks;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.item.WebItems;

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
        itemModelGenerator.generateFlatItem(WebItems.ABOMINABLE_SPIDER_EYE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(WebItems.SPIDER_FANG, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(WebItems.FANG_DAGGER, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(WebItems.ABOMINABLE_FANG_DAGGER, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(WebItems.ABOMINABLE_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(WebItems.ABOMINABLE_SPIDER_QUEEN_SPAWN_EGG, ModelTemplates.FLAT_ITEM);
    }
}