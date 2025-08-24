package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.RegistryWrapper;
import potatowolfie.web.block.WebBlocks;

import java.util.concurrent.CompletableFuture;

public class WebLootTableGenerator extends FabricBlockLootTableProvider {
    public WebLootTableGenerator(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        RegistryWrapper.Impl<net.minecraft.item.Item> itemLookup = registries.getOrThrow(net.minecraft.registry.RegistryKeys.ITEM);
        addDrop(WebBlocks.SPIDER_MOSS);

        addDrop(WebBlocks.SPIDER_EGG, LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(1.0f, 1.0f))
                        .with(ItemEntry.builder(WebBlocks.SPIDER_EGG)
                                .conditionally(createSilkTouchCondition())
                        )
                        .with(ItemEntry.builder(WebBlocks.SPIDER_EGG_SHELLS)
                                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 3.0f)))
                                .conditionally(createSilkTouchCondition().invert())
                        )
                )
        );

        addDrop(WebBlocks.SPIDER_EGG_SHELLS, LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(1.0f, 1.0f))
                        .with(ItemEntry.builder(WebBlocks.SPIDER_EGG_SHELLS)
                                .conditionally(MatchToolLootCondition.builder(ItemPredicate.Builder.create()
                                        .items(itemLookup, Items.SHEARS)
                                ).or(createSilkTouchCondition()))
                        )
                )
        );

        addDrop(WebBlocks.SPIDER_WEB_BLOCK, LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(1.0f, 1.0f))
                        .with(ItemEntry.builder(WebBlocks.SPIDER_WEB_BLOCK)
                                .conditionally(MatchToolLootCondition.builder(ItemPredicate.Builder.create()
                                        .items(itemLookup, Items.SHEARS)
                                ).or(createSilkTouchCondition()))
                        )
                        .with(ItemEntry.builder(Items.STRING)
                                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 2.0f)))
                                .conditionally(MatchToolLootCondition.builder(ItemPredicate.Builder.create()
                                        .items(itemLookup, Items.SHEARS)
                                ).or(createSilkTouchCondition()).invert())
                        )
                )
        );
    }
}