package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import potatowolfie.web.Web;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.item.WebItems;
import potatowolfie.web.trim.WebTrimPatterns;

import java.util.concurrent.CompletableFuture;

public class WebRecipeGenerator extends FabricRecipeProvider {
    public WebRecipeGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, BootstrapContext<Recipe<?>> bootstrapContext, BootstrapContext<Advancement> bootstrapContext1) {
        return new RecipeProvider(bootstrapContext, bootstrapContext1) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.BUILDING_BLOCKS, WebBlocks.SPIDER_WEB_BLOCK, 4)
                        .pattern("XX")
                        .pattern("XX")
                        .define('X', Blocks.COBWEB)
                        .unlockedBy(getHasName(Blocks.COBWEB), has(Blocks.COBWEB))
                        .save(output);

                carpet(WebBlocks.SPIDER_MOSS_CARPET, WebBlocks.SPIDER_MOSS);

                trimSmithing(WebItems.ABOMINABLE_ARMOR_TRIM_SMITHING_TEMPLATE, WebTrimPatterns.ABOMINABLE,
                        ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Web.MOD_ID, "abominable")));
            }
        };
    }

    @Override
    public String getName() {
        return "Web Recipes";
    }
}