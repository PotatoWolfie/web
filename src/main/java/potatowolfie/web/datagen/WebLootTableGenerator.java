package potatowolfie.web.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import potatowolfie.web.block.WebBlocks;

import java.util.concurrent.CompletableFuture;

public class WebLootTableGenerator extends FabricBlockLootSubProvider {
    public WebLootTableGenerator(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        dropSelf(WebBlocks.SPIDER_MOSS);
        dropSelf(WebBlocks.DAMP_SOIL);
        dropOther(WebBlocks.DAMP_SOIL_PATH, WebBlocks.DAMP_SOIL);
        dropSelf(WebBlocks.SPROUTING_DAMP_SOIL);
        dropSelf(WebBlocks.SUCCULENT_GEL);

        add(WebBlocks.SPIDER_EGG, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(1, 1))
                        .add(LootItem.lootTableItem(WebBlocks.SPIDER_EGG)
                                .when(hasSilkTouch())
                        )
                        .add(LootItem.lootTableItem(WebBlocks.SPIDER_EGG_SHELLS)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 3)))
                                .when(doesNotHaveSilkTouch())
                        )
                )
        );

        add(WebBlocks.SPIDER_SUCCULENT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(1, 1))
                        .add(LootItem.lootTableItem(WebBlocks.SPIDER_SUCCULENT)
                                .when(hasSilkTouch())
                        )
                        .add(LootItem.lootTableItem(WebBlocks.SUCCULENT_GEL)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 2)))
                                .when(doesNotHaveSilkTouch())
                        )
                )
        );

        add(WebBlocks.SPIDER_EGG_SHELLS, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(1, 1))
                        .add(LootItem.lootTableItem(WebBlocks.SPIDER_EGG_SHELLS)
                                .when(hasShearsOrSilkTouch())
                        )
                )
        );

        add(WebBlocks.SPIDER_WEB_BLOCK, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(1, 1))
                        .add(LootItem.lootTableItem(WebBlocks.SPIDER_WEB_BLOCK)
                                .when(hasShearsOrSilkTouch())
                        )
                        .add(LootItem.lootTableItem(Items.STRING)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 2)))
                                .when(doesNotHaveShearsOrSilkTouch())
                        )
                )
        );
    }
}