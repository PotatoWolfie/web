package potatowolfie.web.world.feature;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import potatowolfie.web.Web;
import potatowolfie.web.world.feature.custom.SpiderEggClusterFeatureConfig;

public class WebConfiguredFeatures {

    public static final RegistryKey<ConfiguredFeature<?, ?>> SPIDER_EGG_CLUSTER_KEY =
            registerKey("spider_egg_cluster");

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
        register(context, SPIDER_EGG_CLUSTER_KEY, WebFeatures.SPIDER_EGG_CLUSTER,
                new SpiderEggClusterFeatureConfig(10, 15, 8, 0.6f));
    }

    public static RegistryKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(Web.MOD_ID, name));
    }

    private static <FC extends net.minecraft.world.gen.feature.FeatureConfig, F extends net.minecraft.world.gen.feature.Feature<FC>>
    void register(Registerable<ConfiguredFeature<?, ?>> context,
                  RegistryKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}