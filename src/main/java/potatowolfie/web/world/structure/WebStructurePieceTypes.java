package potatowolfie.web.world.structure;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import potatowolfie.web.Web;
import potatowolfie.web.world.structure.nook.NookGenerator;

public class WebStructurePieceTypes {
    public static final StructurePieceType NOOK =
            register((StructurePieceType.StructureTemplateType) NookGenerator.Piece::new, "nook");

    private static StructurePieceType register(StructurePieceType type, String id) {
        return Registry.register(BuiltInRegistries.STRUCTURE_PIECE,
                Identifier.fromNamespaceAndPath("web", id), type);
    }

    private static StructurePieceType register(
            String id,
            StructurePieceType.ContextlessType type
    ) {
        return Registry.register(
                BuiltInRegistries.STRUCTURE_PIECE,
                Identifier.fromNamespaceAndPath(Web.MOD_ID, id),
                type
        );
    }

    public static void registerStructurePieceTypes() {
        Web.LOGGER.info("Registering structure piece types for " + Web.MOD_ID);
    }
}