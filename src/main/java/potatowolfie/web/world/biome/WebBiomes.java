package potatowolfie.web.world.biome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import potatowolfie.web.Web;

public class WebBiomes {
    public static final ResourceKey<Biome> NESTED_CAVES = ResourceKey.create(
            Registries.BIOME, Identifier.fromNamespaceAndPath(Web.MOD_ID, "nested_caves"));
}