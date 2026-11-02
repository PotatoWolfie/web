package potatowolfie.web.world.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.world.feature.custom.SpiderEggClusterFeature;
import potatowolfie.web.world.feature.custom.SpiderEggPatchFeature;
import potatowolfie.web.world.feature.custom.SpiderEggShellsPatchFeature;
import potatowolfie.web.world.feature.custom.SpiderSucculentPatchFeature;

public class WebFeatures {

    public static final MapCodec<SpiderEggClusterFeature> SPIDER_EGG_CLUSTER =
            Registry.register(BuiltInRegistries.FEATURE_TYPE,
                    Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_egg_cluster"),
                    SpiderEggClusterFeature.CODEC);

    public static final MapCodec<SpiderEggPatchFeature> SPIDER_EGG_PATCH =
            Registry.register(BuiltInRegistries.FEATURE_TYPE,
                    Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_egg_patch"),
                    SpiderEggPatchFeature.CODEC);

    public static final MapCodec<SpiderSucculentPatchFeature> SPIDER_SUCCULENT_PATCH =
            Registry.register(BuiltInRegistries.FEATURE_TYPE,
                    Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_succulent_patch"),
                    SpiderSucculentPatchFeature.CODEC);

    public static final MapCodec<SpiderEggShellsPatchFeature> SPIDER_EGG_SHELL_PATCH =
            Registry.register(BuiltInRegistries.FEATURE_TYPE,
                    Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_egg_shell_patch"),
                    SpiderEggShellsPatchFeature.CODEC);

    public static void registerFeatures() {
        Web.LOGGER.info("Registering Web Features");
    }
}