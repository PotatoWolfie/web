package potatowolfie.web.world.feature;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;
import potatowolfie.web.Web;
import potatowolfie.web.world.feature.custom.SpiderEggClusterFeature;
import potatowolfie.web.world.feature.custom.SpiderEggClusterFeatureConfig;

public class WebFeatures {
    public static final Feature<SpiderEggClusterFeatureConfig> SPIDER_EGG_CLUSTER =
            Registry.register(BuiltInRegistries.FEATURE,
                    Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_egg_cluster"),
                    new SpiderEggClusterFeature(SpiderEggClusterFeatureConfig.CODEC));

    public static void registerFeatures() {
        Web.LOGGER.info("Registering Web Features");
    }
}