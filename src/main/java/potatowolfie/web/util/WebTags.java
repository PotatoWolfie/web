package potatowolfie.web.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import potatowolfie.web.Web;

public class WebTags {
    public static class Blocks {
        public static final TagKey<Block> INCORRECT_FOR_FANG_TOOL = createTag("incorrect_for_fang_tool");
        public static final TagKey<Block> INCORRECT_FOR_ABOMINABLE_FANG_TOOL = createTag("incorrect_for_abominable_fang_tool");

        private static TagKey<Block> createTag(String name) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Web.MOD_ID, name));
        }
    }

    public static class Item {
        public static final TagKey<net.minecraft.world.item.Item> FANG_REPAIR = createTag("fang_repair");
        public static final TagKey<net.minecraft.world.item.Item> ABOMINABLE_FANG_REPAIR = createTag("abominable_fang_repair");

        private static TagKey<net.minecraft.world.item.Item> createTag(String name) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Web.MOD_ID, name));
        }
    }
}