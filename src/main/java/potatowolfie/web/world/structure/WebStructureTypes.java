package potatowolfie.web.world.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import potatowolfie.web.Web;
import potatowolfie.web.world.structure.nook.NookStructure;

public class WebStructureTypes {
    public static final StructureType<NookStructure> NOOK =
            register("nook", NookStructure.CODEC);

    private static <S extends Structure> StructureType<S> register(
            String id,
            MapCodec<S> codec
    ) {
        return Registry.register(
                BuiltInRegistries.STRUCTURE_TYPE,
                Identifier.fromNamespaceAndPath(Web.MOD_ID, id),
                () -> codec
        );
    }

    public static void registerStructureTypes() {
        Web.LOGGER.info("Registering structure types for " + Web.MOD_ID);
    }
}