package potatowolfie.web.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import potatowolfie.web.world.feature.WebPlacedFeatures;

public class WebBiomeModifications {

    public static void init() {
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_OVERWORLD)
                        .and(BiomeSelectors.foundInOverworld())
                        .and(BiomeSelectors.excludeByKey(
                                BiomeKeys.OCEAN,
                                BiomeKeys.DEEP_OCEAN,
                                BiomeKeys.LUKEWARM_OCEAN,
                                BiomeKeys.DEEP_LUKEWARM_OCEAN,
                                BiomeKeys.WARM_OCEAN,
                                BiomeKeys.COLD_OCEAN,
                                BiomeKeys.DEEP_COLD_OCEAN,
                                BiomeKeys.FROZEN_OCEAN,
                                BiomeKeys.DEEP_FROZEN_OCEAN,
                                BiomeKeys.DEEP_DARK
                        )),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                WebPlacedFeatures.SPIDER_EGG_CLUSTER_PLACED_KEY
        );
    }
}