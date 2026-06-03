package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.block.Blocks;
import potatowolfie.web.block.WebBlocks;

import java.util.concurrent.CompletableFuture;

public class WebRecipeGenerator extends FabricRecipeProvider {
    public WebRecipeGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider wrapperLookup, RecipeOutput recipeExporter) {
        return new RecipeProvider(wrapperLookup, recipeExporter) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.BUILDING_BLOCKS, WebBlocks.SPIDER_WEB_BLOCK, 4)
                        .pattern("XX")
                        .pattern("XX")
                        .define('X', Blocks.COBWEB)
                        .unlockedBy(getHasName(Blocks.COBWEB), has(Blocks.COBWEB))
                        .save(recipeExporter);
            }
        };
    }

    @Override
    public String getName() {
        return "Web Recipes";
    }
}