package potatowolfie.web.block;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.block.custom.*;

public class WebBlocks {

    public static final Block SPIDER_EGG = registerBlock("spider_egg",
            new SpiderEggBlock(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(Registries.BLOCK.getKey(), Identifier.of(Web.MOD_ID, "spider_egg")))
                    .mapColor(MapColor.YELLOW)
                    .instrument(NoteBlockInstrument.XYLOPHONE)
                    .sounds(BlockSoundGroup.HONEY)
                    .strength(0.5f)
                    .hardness(0.5f)
            ));

    public static final Block SPIDER_EGG_SHELLS = registerBlock("spider_egg_shells",
            new SpiderEggShellsBlock(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(Registries.BLOCK.getKey(), Identifier.of(Web.MOD_ID, "spider_egg_shells")))
                    .mapColor(MapColor.YELLOW)
                    .instrument(NoteBlockInstrument.XYLOPHONE)
                    .sounds(BlockSoundGroup.HONEY)
                    .strength(0.0f)
                    .hardness(0.0f)
                    .breakInstantly()
                    .nonOpaque()
                    .noCollision()
                    .pistonBehavior(PistonBehavior.DESTROY)
            ));

    public static final Block SPIDER_MOSS = registerBlock("spider_moss",
            new SpiderMossBlock(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(Registries.BLOCK.getKey(), Identifier.of(Web.MOD_ID, "spider_moss")))
                    .mapColor(MapColor.DARK_AQUA)
                    .sounds(BlockSoundGroup.MOSS_BLOCK)
                    .strength(0.1f)
                    .hardness(0.1f)
                    .burnable()
            ));

    public static final Block SPIDER_GRASS = registerBlock("spider_grass",
            new SpiderGrassBlock(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(Registries.BLOCK.getKey(), Identifier.of(Web.MOD_ID, "spider_grass")))
                    .mapColor(MapColor.DARK_AQUA)
                    .sounds(BlockSoundGroup.GRASS)
                    .breakInstantly()
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .noCollision()
                    .replaceable()
                    .burnable()
            ));

    public static final Block SPIDER_WEB_BLOCK = registerBlock("spider_web_block",
            new SpiderWebBlock(AbstractBlock.Settings.copy(Blocks.COBWEB)
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .registryKey(RegistryKey.of(Registries.BLOCK.getKey(), Identifier.of(Web.MOD_ID, "spider_web_block")))
            ));


    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(Web.MOD_ID, name), block);
    }

    private static void customNaturalBlocks(FabricItemGroupEntries entries) {
        entries.addAfter(Blocks.SNIFFER_EGG, SPIDER_EGG);
        entries.addAfter(SPIDER_EGG, SPIDER_EGG_SHELLS);
        entries.addAfter(Blocks.PALE_HANGING_MOSS, SPIDER_MOSS);
        entries.addAfter(SPIDER_MOSS, SPIDER_GRASS);

    }

    private static void customSpawnEggs(FabricItemGroupEntries entries) {
        entries.addAfter(Blocks.CREAKING_HEART, SPIDER_EGG);

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
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(WebBlocks::customSpawnEggs);
    }
}