package potatowolfie.web.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;

public class WebItems {

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Web.MOD_ID, name), item);
    }
    private static void customIngredients(FabricItemGroupEntries entries) {

    }

    private static void customCombat(FabricItemGroupEntries entries) {

    }

    private static void customSpawnEggs(FabricItemGroupEntries entries) {

    }

    public static void registerModItems() {
        Web.LOGGER.info("Registering Mod Items for " + Web.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(WebItems::customIngredients);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(WebItems::customCombat);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(WebItems::customSpawnEggs);
    }
}