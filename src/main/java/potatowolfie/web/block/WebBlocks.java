package potatowolfie.web.block;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.block.custom.SpiderEggBlock;
import potatowolfie.web.block.custom.SpiderEggShellsBlock;

public class WebBlocks {

    public static final Block SPIDER_EGG = registerBlock("spider_egg",
            new SpiderEggBlock(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(Registries.BLOCK.getKey(), Identifier.of(Web.MOD_ID, "spider_egg")))
                    .mapColor(MapColor.YELLOW)
                    .instrument(NoteBlockInstrument.XYLOPHONE)
                    .sounds(BlockSoundGroup.HONEY)
                    .strength(0.5f)
            ));

    public static final Block SPIDER_EGG_SHELLS = registerBlock("spider_egg_shells",
            new SpiderEggShellsBlock(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(Registries.BLOCK.getKey(), Identifier.of(Web.MOD_ID, "spider_egg_shells")))
                    .mapColor(MapColor.YELLOW)
                    .instrument(NoteBlockInstrument.XYLOPHONE)
                    .sounds(BlockSoundGroup.HONEY)
                    .strength(0.0f)
                    .breakInstantly()
                    .nonOpaque()
                    .noCollision()
            ));

    public static final Block SPIDER_NEST = registerBlock("spider_nest",
            new Block(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(Registries.BLOCK.getKey(), Identifier.of(Web.MOD_ID, "spider_nest")))
                    .mapColor(MapColor.DARK_AQUA)
                    .sounds(BlockSoundGroup.MOSS_BLOCK)
                    .strength(0.1f)
                    .burnable()
            ));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(Web.MOD_ID, name), block);
    }

    private static void customNaturalBlocks(FabricItemGroupEntries entries) {
        entries.addAfter(Blocks.SNIFFER_EGG, SPIDER_EGG);
        entries.addAfter(SPIDER_EGG, SPIDER_EGG_SHELLS);
        entries.addAfter(Blocks.PALE_HANGING_MOSS, SPIDER_NEST);

    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(Web.MOD_ID, name),
                new BlockItem(block, new Item.Settings()
                        .registryKey(RegistryKey.of(Registries.ITEM.getKey(), Identifier.of(Web.MOD_ID, name)))
                ));
    }

    public static void registerModBlocks() {
        Web.LOGGER.info("Registering Mod Blocks for " + Web.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(WebBlocks::customNaturalBlocks);
    }
}