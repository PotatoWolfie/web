package potatowolfie.web.world.feature;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.Feature;
import potatowolfie.web.Web;
import potatowolfie.web.world.feature.custom.SpiderEggClusterFeature;
import potatowolfie.web.world.feature.custom.SpiderEggClusterFeatureConfig;

public class WebFeatures {
    public static final Feature<SpiderEggClusterFeatureConfig> SPIDER_EGG_CLUSTER =
            Registry.register(Registries.FEATURE,
                    Identifier.of(Web.MOD_ID, "spider_egg_cluster"),
                    new SpiderEggClusterFeature(SpiderEggClusterFeatureConfig.CODEC));

    public static void registerFeatures() {
        Web.LOGGER.info("Registering Web Features");
    }
}