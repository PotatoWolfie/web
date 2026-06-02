package potatowolfie.web.world.feature.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;

public record SpiderEggClusterFeatureConfig(
        int minClusterSize,
        int maxClusterSize,
        int nestSpreadRadius,
        float eggChance
) implements FeatureConfig {

    public static final Codec<SpiderEggClusterFeatureConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.intRange(1, 20).fieldOf("min_cluster_size").forGetter(SpiderEggClusterFeatureConfig::minClusterSize),
                    Codec.intRange(1, 40).fieldOf("max_cluster_size").forGetter(SpiderEggClusterFeatureConfig::maxClusterSize),
                    Codec.intRange(1, 10).fieldOf("nest_spread_radius").forGetter(SpiderEggClusterFeatureConfig::nestSpreadRadius),
                    Codec.floatRange(0.0f, 1.0f).fieldOf("egg_chance").forGetter(SpiderEggClusterFeatureConfig::eggChance)
            ).apply(instance, SpiderEggClusterFeatureConfig::new)
    );
}