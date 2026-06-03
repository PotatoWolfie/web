package potatowolfie.web.world.gen;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import potatowolfie.web.world.feature.WebConfiguredFeatures;
import potatowolfie.web.world.feature.WebPlacedFeatures;

public class WebWorldGeneration {
    public static void registerConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        WebConfiguredFeatures.bootstrap(context);
    }

    public static void registerPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        WebPlacedFeatures.bootstrap(context);
    }

    public static void init() {

    }
}