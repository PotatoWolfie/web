package potatowolfie.web.world.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

public class WebRegion extends Region {
    public WebRegion(Identifier name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        this.addModifiedVanillaOverworldBiomes(mapper, builder -> {});

        this.addBiome(mapper,
                Climate.Parameter.span(-0.45f, 0.55f),
                Climate.Parameter.span(-0.35f, 0.3f),
                Climate.Parameter.span(-0.11f, 1.0f),
                Climate.Parameter.span(-1.0f, 1.0f),
                Climate.Parameter.span(-1.0f, 1.0f),
                Climate.Parameter.span(0.2f, 0.9f),
                0.0f,
                WebBiomes.NESTED_CAVES);
    }
}