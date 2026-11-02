package potatowolfie.web.world.feature;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.*;
import potatowolfie.web.Web;
import potatowolfie.web.block.WebBlocks;

import java.util.List;

public class WebPlacedFeatures {

    public static final ResourceKey<PlacedFeature> SPIDER_EGG_CLUSTER_PLACED_KEY = registerKey("spider_egg_cluster_placed");
    public static final ResourceKey<PlacedFeature> SPIDER_GRASS_PLACED_KEY = registerKey("spider_grass_placed");
    public static final ResourceKey<PlacedFeature> SPIDER_EGG_PATCH_PLACED_KEY = registerKey("spider_egg_patch_placed");
    public static final ResourceKey<PlacedFeature> SPIDER_EGG_SHELL_PATCH_PLACED_KEY = registerKey("spider_egg_shell_patch_placed");
    public static final ResourceKey<PlacedFeature> SPIDER_SUCCULENT_PATCH_PLACED_KEY = registerKey("spider_succulent_patch_placed");
    public static final ResourceKey<PlacedFeature> SPIDER_WEB_PATCH_PLACED_KEY = registerKey("spider_web_patch_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<Feature> configuredFeatures = context.lookup(Registries.FEATURE);

        register(context, SPIDER_EGG_CLUSTER_PLACED_KEY,
                configuredFeatures.getOrThrow(WebConfiguredFeatures.SPIDER_EGG_CLUSTER_KEY),
                CountPlacement.of(2),
                InSquarePlacement.spread(),
                ConfigurableRarityFilter.INSTANCE,
                BiomeFilter.biome());

        register(context, SPIDER_GRASS_PLACED_KEY,
                configuredFeatures.getOrThrow(WebConfiguredFeatures.SPIDER_GRASS_PATCH_KEY),
                CountPlacement.of(128),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(55)),
                EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), 32),
                OffsetPlacement.of(0, 1, 0),
                BiomeFilter.biome(),
                BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                        BlockPredicate.matchesBlocks(new Vec3i(0, -1, 0), List.of(
                                WebBlocks.DAMP_SOIL,
                                WebBlocks.SPROUTING_DAMP_SOIL,
                                WebBlocks.SPIDER_MOSS,
                                WebBlocks.SPIDER_MOSS_CARPET
                        ))
                ))
        );

        register(context, SPIDER_WEB_PATCH_PLACED_KEY,
                configuredFeatures.getOrThrow(WebConfiguredFeatures.SPIDER_WEB_PATCH_KEY),
                CountPlacement.of(4),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(55)),
                EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), 32),
                OffsetPlacement.of(0, 1, 0),
                BiomeFilter.biome(),
                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)
        );

        register(context, SPIDER_EGG_PATCH_PLACED_KEY,
                configuredFeatures.getOrThrow(WebConfiguredFeatures.SPIDER_EGG_PATCH_KEY),
                CountPlacement.of(6),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(50)),
                EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), 32),
                BiomeFilter.biome());

        register(context, SPIDER_EGG_SHELL_PATCH_PLACED_KEY,
                configuredFeatures.getOrThrow(WebConfiguredFeatures.SPIDER_EGG_SHELL_PATCH_KEY),
                CountPlacement.of(UniformInt.of(16, 24)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(55)),
                EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), 32),
                BiomeFilter.biome());

        register(context, SPIDER_SUCCULENT_PATCH_PLACED_KEY,
                configuredFeatures.getOrThrow(WebConfiguredFeatures.SPIDER_SUCCULENT_PATCH_KEY),
                CountPlacement.of(12),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(50)),
                EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), 32),
                BiomeFilter.biome());
    }

    public static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(Web.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                 Holder<Feature> configuration,
                                 PlacementModifier... modifiers) {
        context.register(key, new PlacedFeature(configuration, List.of(modifiers)));
    }
}