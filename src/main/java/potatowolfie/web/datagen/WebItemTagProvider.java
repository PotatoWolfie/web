package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import potatowolfie.web.block.WebBlocks;
import potatowolfie.web.item.WebItems;
import potatowolfie.web.util.WebTags;

import java.util.concurrent.CompletableFuture;

public class WebItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public WebItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(ItemTags.TRIM_MATERIALS)
                .add(key(Item.byBlock(WebBlocks.SUCCULENT_GEL)));

        builder(ItemTags.DIRT)
                .add(
                        key(Item.byBlock(WebBlocks.SPIDER_MOSS)),
                        key(Item.byBlock(WebBlocks.DAMP_SOIL)),
                        key(Item.byBlock(WebBlocks.SPROUTING_DAMP_SOIL)),
                        key(Item.byBlock(WebBlocks.DAMP_SOIL_PATH))
                );

        builder(ItemTags.WEAPON_ENCHANTABLE)
                .add(
                        key(WebItems.FANG_DAGGER),
                        key(WebItems.ABOMINABLE_FANG_DAGGER));

        builder(ItemTags.MELEE_WEAPON_ENCHANTABLE)
                .add(
                        key(WebItems.FANG_DAGGER),
                        key(WebItems.ABOMINABLE_FANG_DAGGER));

        builder(ItemTags.SHARP_WEAPON_ENCHANTABLE)
                .add(
                        key(WebItems.FANG_DAGGER),
                        key(WebItems.ABOMINABLE_FANG_DAGGER));

        builder(ItemTags.FIRE_ASPECT_ENCHANTABLE)
                .add(
                        key(WebItems.FANG_DAGGER),
                        key(WebItems.ABOMINABLE_FANG_DAGGER));

        builder(ItemTags.DURABILITY_ENCHANTABLE)
                .add(
                        key(WebItems.FANG_DAGGER),
                        key(WebItems.ABOMINABLE_FANG_DAGGER));

        builder(ItemTags.BREAKS_DECORATED_POTS)
                .add(
                        key(WebItems.FANG_DAGGER),
                        key(WebItems.ABOMINABLE_FANG_DAGGER));

        builder(WebTags.Item.FANG_REPAIR)
                .add(key(WebItems.SPIDER_FANG));

        builder(WebTags.Item.ABOMINABLE_FANG_REPAIR)
                .add(key(WebItems.SPIDER_FANG));
    }

    private static ResourceKey<Item> key(Item item) {
        return BuiltInRegistries.ITEM.wrapAsHolder(item).unwrapKey().orElseThrow();
    }
}