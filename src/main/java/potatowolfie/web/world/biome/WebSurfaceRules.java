package potatowolfie.web.world.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import potatowolfie.web.block.WebBlocks;

public class WebSurfaceRules {
    public static MaterialRule makeRules(RegistryAccess registries) {
        HolderGetter<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);

        MaterialRule tuff = MaterialRules.state(Blocks.TUFF.defaultBlockState());
        MaterialRule dampSoil = MaterialRules.state(WebBlocks.DAMP_SOIL.defaultBlockState());
        MaterialRule spiderMoss = MaterialRules.state(WebBlocks.SPIDER_MOSS.defaultBlockState());
        MaterialRule sproutingDampSoil = MaterialRules.state(WebBlocks.SPROUTING_DAMP_SOIL.defaultBlockState());

        MaterialRule dampSoilSubRegion = MaterialRules.sequence(
                MaterialRules.ifTrue(
                        MaterialRules.noiseCondition2d(WebNoises.NESTED_CAVE_GRADIENT, 0.15, 0.33),
                        spiderMoss
                ),
                MaterialRules.ifTrue(
                        MaterialRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
                        MaterialRules.ifTrue(
                                MaterialRules.noiseCondition2d(WebNoises.NESTED_CAVE_GRADIENT, 0.11, 0.15),
                                sproutingDampSoil
                        )
                ),
                MaterialRules.ifTrue(
                        MaterialRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
                        MaterialRules.ifTrue(
                                MaterialRules.noiseCondition2d(WebNoises.NESTED_CAVE_GRADIENT, 0.33, 0.37),
                                sproutingDampSoil
                        )
                ),
                dampSoil
        );

        MaterialRule bands = MaterialRules.sequence(
                MaterialRules.ifTrue(MaterialRules.noiseCondition3d(
                        WebNoises.NESTED_CAVE_GRADIENT, -0.4, -0.1), tuff),

                MaterialRules.ifTrue(MaterialRules.noiseCondition3d(
                        WebNoises.NESTED_CAVE_GRADIENT, 0.0, 0.4), dampSoilSubRegion),

                MaterialRules.ifTrue(MaterialRules.noiseCondition3d(
                        WebNoises.NESTED_CAVE_GRADIENT, 0.4), tuff)
        );

        return MaterialRules.ifTrue(
                MaterialRules.isBiome(biomes, WebBiomes.NESTED_CAVES), bands);
    }
}