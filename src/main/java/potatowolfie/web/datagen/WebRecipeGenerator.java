package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import potatowolfie.web.block.WebBlocks;

import java.util.concurrent.CompletableFuture;

public class WebRecipeGenerator extends FabricRecipeProvider {
    public WebRecipeGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            @Override
            public void generate() {
                createShaped(RecipeCategory.BUILDING_BLOCKS, WebBlocks.SPIDER_WEB_BLOCK, 4)
                        .pattern("XX")
                        .pattern("XX")
                        .input('X', Blocks.COBWEB)
                        .criterion(hasItem(Blocks.COBWEB), conditionsFromItem(Blocks.COBWEB))
                        .offerTo(recipeExporter);
            }
        };
    }

    @Override
    public String getName() {
        return "Web Recipes";
    }
}