package potatowolfie.web.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import potatowolfie.web.world.feature.WebPlacedFeatures;

public class WebBiomeModifications {

    public static void init() {
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_OVERWORLD)
                        .and(BiomeSelectors.foundInOverworld())
                        .and(BiomeSelectors.excludeByKey(
                                Biomes.OCEAN,
                                Biomes.DEEP_OCEAN,
                                Biomes.LUKEWARM_OCEAN,
                                Biomes.DEEP_LUKEWARM_OCEAN,
                                Biomes.WARM_OCEAN,
                                Biomes.COLD_OCEAN,
                                Biomes.DEEP_COLD_OCEAN,
                                Biomes.FROZEN_OCEAN,
                                Biomes.DEEP_FROZEN_OCEAN,
                                Biomes.DEEP_DARK
                        )),
                GenerationStep.Decoration.UNDERGROUND_DECORATION,
                WebPlacedFeatures.SPIDER_EGG_CLUSTER_PLACED_KEY
        );
    }
}