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
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.item.custom.FangDaggerItem;
import potatowolfie.web.item.custom.SpiderWebItem;

import java.util.List;

public class WebItems {

    public static final Item SPIDER_WEB = registerItem("spider_web",
            new SpiderWebItem(WebBlocks.SPIDER_WEB_BLOCK,
                    new Item.Properties()
                            .useBlockDescriptionPrefix()
                            .setId(createItemRegistryKey("spider_web"))));

    public static final Item SPIDER_FANG = registerItem("spider_fang",
            new Item(new Item.Properties()
                            .setId(createItemRegistryKey("spider_fang"))));

    public static final Item ABOMINABLE_SPIDER_QUEEN_SPAWN_EGG = registerItem("abominable_spider_queen_spawn_egg",
            new SpawnEggItem(new Item.Properties()
                    .spawnEgg(WebEntities.ABOMINABLE_SPIDER_QUEEN)
                    .setId(createItemRegistryKey("abominable_spider_queen_spawn_egg"))));

    public static final Item ABOMINABLE_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem("abominable_armor_trim_smithing_template",
            SmithingTemplateItem.createArmorTrimTemplate(new Item.Properties().rarity(Rarity.UNCOMMON)
                    .setId(createItemRegistryKey("abominable_armor_trim_smithing_template"))));

    public static final Item ABOMINABLE_SPIDER_EYE = registerItem("abominable_spider_eye",
            new Item(new Item.Properties().food(WebFoods.ABOMINABLE_SPIDER_EYE, WebFoods.ABOMINABLE_SPIDER_EYE_CONSUMABLE)
                            .setId(createItemRegistryKey("abominable_spider_eye"))));

    public static final Item FANG_DAGGER = registerItem("fang_dagger",
            new FangDaggerItem(
                    FangDaggerItem.createProperties(
                            WebToolMaterials.FANG,
                            3.0F,
                            -1.4F,
                            0.75F
                    ).setId(createItemRegistryKey("fang_dagger")),
                    50,
                    4.5F
            ));

    public static final Item ABOMINABLE_FANG_DAGGER = registerItem("abominable_fang_dagger",
            new FangDaggerItem(
                    FangDaggerItem.createProperties(
                            WebToolMaterials.ABOMINABLE_FANG,
                            4.5F,
                            -1.5F,
                            1.25F
                    ).setId(createItemRegistryKey("abominable_fang_dagger")),
                    50,
                    7.0F
            ));

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

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(output -> {

                    output.insertAfter(Items.MACE, List.of(
                            new ItemStack(FANG_DAGGER),
                            new ItemStack(ABOMINABLE_FANG_DAGGER)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(output -> {
                    output.insertAfter(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, List.of(
                            new ItemStack(ABOMINABLE_ARMOR_TRIM_SMITHING_TEMPLATE)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

                    output.insertAfter(Items.SLIME_BALL, List.of(
                            new ItemStack(SPIDER_FANG)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

                    output.insertAfter(Items.SPIDER_EYE, List.of(
                            new ItemStack(ABOMINABLE_SPIDER_EYE)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS)
                .register(output -> {

                    output.insertAfter(Items.COBWEB, List.of(
                            new ItemStack(SPIDER_WEB)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(output -> {

                    output.insertAfter(Items.SPIDER_SPAWN_EGG, List.of(
                            new ItemStack(ABOMINABLE_SPIDER_QUEEN_SPAWN_EGG)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });
    }
}