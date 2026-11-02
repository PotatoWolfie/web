package potatowolfie.web.world.biome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import potatowolfie.web.Web;

public class WebNoises {
    public static final ResourceKey<NormalNoise> NESTED_CAVE_GRADIENT = ResourceKey.create(
            Registries.NOISE, Identifier.fromNamespaceAndPath(Web.MOD_ID, "nested_cave_gradient"));
}