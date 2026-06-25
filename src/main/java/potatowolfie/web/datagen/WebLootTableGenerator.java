package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import potatowolfie.web.block.WebBlocks;

import java.util.concurrent.CompletableFuture;

public class WebLootTableGenerator extends FabricBlockLootSubProvider {
    public WebLootTableGenerator(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        HolderLookup.RegistryLookup<net.minecraft.world.item.Item> itemLookup = registries.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM);
        dropSelf(WebBlocks.SPIDER_MOSS);

        add(WebBlocks.SPIDER_EGG, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0f, 1.0f))
                        .add(LootItem.lootTableItem(WebBlocks.SPIDER_EGG)
                                .when(hasSilkTouch())
                        )
                        .add(LootItem.lootTableItem(WebBlocks.SPIDER_EGG_SHELLS)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 3.0f)))
                                .when(hasSilkTouch().invert())
                        )
                )
        );

        add(WebBlocks.SPIDER_EGG_SHELLS, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0f, 1.0f))
                        .add(LootItem.lootTableItem(WebBlocks.SPIDER_EGG_SHELLS)
                                .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                        .of(itemLookup, Items.SHEARS)
                                ).or(hasSilkTouch()))
                        )
                )
        );

        add(WebBlocks.SPIDER_WEB_BLOCK, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0f, 1.0f))
                        .add(LootItem.lootTableItem(WebBlocks.SPIDER_WEB_BLOCK)
                                .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                        .of(itemLookup, Items.SHEARS)
                                ).or(hasSilkTouch()))
                        )
                        .add(LootItem.lootTableItem(Items.STRING)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 2.0f)))
                                .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                        .of(itemLookup, Items.SHEARS)
                                ).or(hasSilkTouch()).invert())
                        )
                )
        );
    }
}