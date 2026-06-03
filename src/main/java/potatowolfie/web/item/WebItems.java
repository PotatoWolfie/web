package potatowolfie.web.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import potatowolfie.web.Web;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.item.custom.SpiderWebItem;

import java.util.List;

public class WebItems {

    public static final Item SPIDER_WEB = registerItem("spider_web",
            new SpiderWebItem(WebBlocks.SPIDER_WEB_BLOCK,
                    new Item.Properties()
                            .useBlockDescriptionPrefix()
                            .setId(createItemRegistryKey("spider_web"))));

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, createItemRegistryKey(name), item);
    }

    private static ResourceKey<Item> createItemRegistryKey(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Web.MOD_ID, name));
    }

    public static void registerModItems() {
        Web.LOGGER.info("Registering Mod Items for " + Web.MOD_ID);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(output -> {

                    output.insertAfter(Items.CROSSBOW, List.of(
                            new ItemStack(SPIDER_WEB)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS)
                .register(output -> {

                    output.insertAfter(Items.COBWEB, List.of(
                            new ItemStack(SPIDER_WEB)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });
    }
}