package potatowolfie.web.world.gen;

import net.minecraft.registry.Registerable;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import potatowolfie.web.world.feature.WebConfiguredFeatures;
import potatowolfie.web.world.feature.WebPlacedFeatures;

public class WebWorldGeneration {
    public static void registerConfiguredFeatures(Registerable<ConfiguredFeature<?, ?>> context) {
        WebConfiguredFeatures.bootstrap(context);
    }

    public static void registerPlacedFeatures(Registerable<PlacedFeature> context) {
        WebPlacedFeatures.bootstrap(context);
    }

    public static void init() {

    }
}