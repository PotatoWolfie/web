package potatowolfie.web.world.feature;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;

public class WebPlacementModifierTypes {

    public static void init() {
        Registry.register(
                BuiltInRegistries.PLACEMENT_MODIFIER_TYPE,
                Identifier.fromNamespaceAndPath(Web.MOD_ID, "configurable_rarity_filter"),
                ConfigurableRarityFilter.CODEC
        );
    }
}