package potatowolfie.web.block;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import potatowolfie.web.Web;
import potatowolfie.web.block.custom.*;

import java.util.List;

public class WebBlocks {

    public static final Block SPIDER_EGG = registerBlock("spider_egg",
            new SpiderEggBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_egg")))
                    .mapColor(MapColor.COLOR_YELLOW)
                    .instrument(NoteBlockInstrument.XYLOPHONE)
                    .sound(SoundType.HONEY_BLOCK)
                    .strength(0.5f, 0.5f)
                    .destroyTime(0.5f)
                    .lightLevel(state -> 3)
            ));

    public static final Block SPIDER_EGG_SHELLS = registerBlock("spider_egg_shells",
            new SpiderEggShellsBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_egg_shells")))
                    .mapColor(MapColor.COLOR_YELLOW)
                    .instrument(NoteBlockInstrument.XYLOPHONE)
                    .sound(SoundType.HONEY_BLOCK)
                    .strength(0.0f, 0.0f)
                    .destroyTime(0.0f)
                    .instabreak()
                    .noOcclusion()
                    .noCollision()
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> 3)
            ));

    public static final Block SPIDER_MOSS = registerBlock("spider_moss",
            new SpiderMossBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_moss")))
                    .mapColor(MapColor.WARPED_STEM)
                    .sound(SoundType.MOSS)
                    .strength(0.1f, 0.1f)
                    .destroyTime(0.1f)
                    .ignitedByLava()
            ));

    public static final Block SPIDER_GRASS = registerBlock("spider_grass",
            new SpiderGrassBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_grass")))
                    .mapColor(MapColor.WARPED_STEM)
                    .sound(SoundType.GRASS)
                    .instabreak()
                    .pushReaction(PushReaction.DESTROY)
                    .noCollision()
                    .replaceable()
                    .ignitedByLava()
            ));

    public static final Block SPIDER_WEB_BLOCK = registerBlock("spider_web_block",
            new SpiderWebBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBWEB)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_web_block")))
            ));


    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(Web.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(Web.MOD_ID, name),
                new BlockItem(block, new Item.Properties()
                        .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, name)))
                ));
    }

    public static void registerModBlocks() {
        Web.LOGGER.info("Registering Mod Blocks for " + Web.MOD_ID);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS)
                .register(output -> {

                    output.insertAfter(Items.SNIFFER_EGG, List.of(
                            new ItemStack(SPIDER_EGG),
                            new ItemStack(SPIDER_EGG_SHELLS)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

                    output.insertAfter(Items.PALE_HANGING_MOSS, List.of(
                            new ItemStack(SPIDER_MOSS),
                            new ItemStack(SPIDER_GRASS)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(output -> {

                    output.insertAfter(Items.CREAKING_HEART, List.of(
                            new ItemStack(SPIDER_EGG)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });
    }
}