package potatowolfie.web.trim;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import potatowolfie.web.Web;
import potatowolfie.web.item.WebItems;

public class WebTrimPatterns {
    public static final ResourceKey<TrimPattern> ABOMINABLE = ResourceKey.create(Registries.TRIM_PATTERN, Identifier.fromNamespaceAndPath(Web.MOD_ID, "abominable"));

    public static void bootstrap(BootstrapContext<TrimPattern> context) {
        register(context, WebItems.ABOMINABLE_ARMOR_TRIM_SMITHING_TEMPLATE, ABOMINABLE);
    }

    private static void register(BootstrapContext<TrimPattern> context, Item item, ResourceKey<TrimPattern> key) {
        TrimPattern trimPattern = new TrimPattern(key.identifier(),
                Component.translatable(Util.makeDescriptionId("trim_pattern", key.identifier())), false);

        context.register(key, trimPattern);
    }
}