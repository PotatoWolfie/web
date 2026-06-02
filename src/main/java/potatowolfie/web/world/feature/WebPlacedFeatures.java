package potatowolfie.web.world.feature;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.*;
import potatowolfie.web.Web;

import java.util.List;

public class WebPlacedFeatures {

    public static final RegistryKey<PlacedFeature> SPIDER_EGG_CLUSTER_PLACED_KEY =
            registerKey("spider_egg_cluster_placed");

    public static void bootstrap(Registerable<PlacedFeature> context) {
        var configuredFeatureRegistryEntryLookup = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        register(context, SPIDER_EGG_CLUSTER_PLACED_KEY,
                configuredFeatureRegistryEntryLookup.getOrThrow(WebConfiguredFeatures.SPIDER_EGG_CLUSTER_KEY),
                List.of(
                        CountPlacementModifier.of(2),
                        SquarePlacementModifier.of(),
                        RarityFilterPlacementModifier.of(8),
                        HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(55)),
                        BiomePlacementModifier.of()
                )
        );
    }

    public static RegistryKey<PlacedFeature> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Web.MOD_ID, name));
    }

    private static void register(Registerable<PlacedFeature> context, RegistryKey<PlacedFeature> key,
                                 RegistryEntry<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}