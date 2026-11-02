package potatowolfie.web.block;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import potatowolfie.web.Web;
import potatowolfie.web.block.custom.*;
import potatowolfie.web.trim.WebTrimMaterials;

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
                    .lightLevel(state -> {
                        EggStage stage = state.getValue(SpiderEggBlock.STAGE);
                        boolean covered = state.getValue(SpiderEggBlock.COVERED);
                        if (stage == EggStage.OLD) {
                            return 2;
                        }
                        if (stage == EggStage.ACTIVE || covered) {
                            return 3;
                        }
                        return 0;
                    })
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
                    .pushReaction(PushReaction.POPPED)
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

    public static final Block SPIDER_MOSS_CARPET = registerBlock("spider_moss_carpet",
            new CarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_CARPET)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_moss_carpet")))
                    .mapColor(MapColor.WARPED_STEM)
                    .ignitedByLava()
            ));

    public static final Block SPIDER_GRASS = registerBlock("spider_grass",
            new SpiderGrassBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_grass")))
                    .mapColor(MapColor.WARPED_STEM)
                    .sound(SoundType.GRASS)
                    .instabreak()
                    .pushReaction(PushReaction.POPPED)
                    .noCollision()
                    .replaceable()
                    .ignitedByLava()
            ));

    public static final Block SPIDER_WEB_BLOCK = registerBlock("spider_web_block",
            new SpiderWebBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBWEB)
                    .ignitedByLava()
                    .pushReaction(PushReaction.POPPED)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_web_block")))
            ));

    public static final Block DAMP_SOIL = registerBlock("damp_soil",
            new DampSoilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "damp_soil")))
            ));

    public static final Block DAMP_SOIL_PATH = registerBlock("damp_soil_path",
            new DampSoilPathBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "damp_soil_path")))
            ));

    public static final Block SPROUTING_DAMP_SOIL = registerBlock("sprouting_damp_soil",
            new DampSoilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "sprouting_damp_soil")))
            ));

    public static final Block SUCCULENT_GEL = registerBlock("succulent_gel",
            new SucculentGelBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "succulent_gel")))
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .sound(SoundType.SLIME_BLOCK)
                    .strength(0.2f)
                    .noCollision()
                    .noOcclusion()
                    .pushReaction(PushReaction.POPPED)
            ),
            new Item.Properties()
                    .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "succulent_gel")))
                    .trimMaterial(WebTrimMaterials.SUCCULENT_GEL)
    );

    public static final Block SPIDER_SUCCULENT = registerBlock("spider_succulent",
            new SpiderSucculentBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_succulent")))
                    .mapColor(MapColor.WARPED_STEM)
                    .sound(SoundType.WOOL)
                    .strength(0.4f)
                    .randomTicks()
                    .noOcclusion()
                    .pushReaction(PushReaction.POPPED)
            ));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(Web.MOD_ID, name), block);
    }

    private static Block registerBlock(String name, Block block, Item.Properties itemProperties) {
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(Web.MOD_ID, name),
                new BlockItem(block, itemProperties));
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
                            new ItemStack(SPIDER_EGG_SHELLS),
                            new ItemStack(SPIDER_SUCCULENT)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

                    output.insertAfter(Items.ROOTED_DIRT, List.of(
                            new ItemStack(DAMP_SOIL),
                            new ItemStack(SPROUTING_DAMP_SOIL),
                            new ItemStack(DAMP_SOIL_PATH)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

                    output.insertAfter(Items.PALE_HANGING_MOSS, List.of(
                            new ItemStack(SPIDER_MOSS),
                            new ItemStack(SPIDER_MOSS_CARPET),
                            new ItemStack(SPIDER_GRASS)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(output -> {

                    output.insertAfter(Items.CREAKING_HEART, List.of(
                            new ItemStack(SPIDER_EGG)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(output -> {

                    output.insertAfter(Items.SLIME_BALL, List.of(
                            new ItemStack(SUCCULENT_GEL)
                    ), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                });
    }
}