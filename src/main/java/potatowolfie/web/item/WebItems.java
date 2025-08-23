package potatowolfie.web.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.item.custom.SpiderWebItem;

public class WebItems {

    public static final Item SPIDER_WEB = registerItem("spider_web",
            new SpiderWebItem(WebBlocks.SPIDER_WEB_BLOCK,
                    new Item.Settings()
                            .useBlockPrefixedTranslationKey()
                            .registryKey(createItemRegistryKey("spider_web"))));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, createItemRegistryKey(name), item);
    }
    private static void customIngredients(FabricItemGroupEntries entries) {

    }

    private static void customCombat(FabricItemGroupEntries entries) {
        entries.addAfter(Items.CROSSBOW, SPIDER_WEB);
    }

    private static void customSpawnEggs(FabricItemGroupEntries entries) {

    }

    private static void customNatural(FabricItemGroupEntries entries) {
        entries.addAfter(Items.COBWEB, SPIDER_WEB);
    }

    private static RegistryKey<Item> createItemRegistryKey(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Web.MOD_ID, name));
    }

    public static void registerModItems() {
        Web.LOGGER.info("Registering Mod Items for " + Web.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(WebItems::customIngredients);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(WebItems::customCombat);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(WebItems::customSpawnEggs);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(WebItems::customNatural);
    }
}