package potatowolfie.web.world.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import potatowolfie.web.Web;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.world.feature.custom.SpiderEggClusterFeature;
import potatowolfie.web.world.feature.custom.SpiderEggPatchFeature;
import potatowolfie.web.world.feature.custom.SpiderEggShellsPatchFeature;
import potatowolfie.web.world.feature.custom.SpiderSucculentPatchFeature;

public class WebConfiguredFeatures {

    public static final ResourceKey<Feature> SPIDER_EGG_CLUSTER_KEY = registerKey("spider_egg_cluster");
    public static final ResourceKey<Feature> SPIDER_GRASS_PATCH_KEY = registerKey("spider_grass_patch");
    public static final ResourceKey<Feature> SPIDER_EGG_PATCH_KEY = registerKey("spider_egg_patch");
    public static final ResourceKey<Feature> SPIDER_EGG_SHELL_PATCH_KEY = registerKey("spider_egg_shell_patch");
    public static final ResourceKey<Feature> SPIDER_SUCCULENT_PATCH_KEY = registerKey("spider_succulent_patch");
    public static final ResourceKey<Feature> SPIDER_WEB_PATCH_KEY = registerKey("spider_web_patch");

    public static void bootstrap(BootstrapContext<Feature> context) {
        context.register(SPIDER_EGG_CLUSTER_KEY, new SpiderEggClusterFeature(10, 15, 8, 0.6f, 0.05f));
        context.register(SPIDER_GRASS_PATCH_KEY, new SimpleBlockFeature(BlockStateProvider.of(WebBlocks.SPIDER_GRASS)));
        context.register(SPIDER_EGG_PATCH_KEY, new SpiderEggPatchFeature());
        context.register(SPIDER_EGG_SHELL_PATCH_KEY, new SpiderEggShellsPatchFeature());
        context.register(SPIDER_SUCCULENT_PATCH_KEY, new SpiderSucculentPatchFeature());
        context.register(SPIDER_WEB_PATCH_KEY, new SimpleBlockFeature(BlockStateProvider.of(WebBlocks.SPIDER_WEB_BLOCK)));
    }

    public static ResourceKey<Feature> registerKey(String name) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Web.MOD_ID, name));
    }
}