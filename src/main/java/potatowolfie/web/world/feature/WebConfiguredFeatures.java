package potatowolfie.web.world.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import potatowolfie.web.Web;
import potatowolfie.web.world.feature.custom.SpiderEggClusterFeatureConfig;

public class WebConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> SPIDER_EGG_CLUSTER_KEY =
            registerKey("spider_egg_cluster");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        register(context, SPIDER_EGG_CLUSTER_KEY, WebFeatures.SPIDER_EGG_CLUSTER,
                new SpiderEggClusterFeatureConfig(10, 15, 8, 0.6f));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Web.MOD_ID, name));
    }

    private static <FC extends net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration, F extends net.minecraft.world.level.levelgen.feature.Feature<FC>>
    void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                  ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}