package potatowolfie.web.advancement;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import potatowolfie.web.block.WebBlocks;

public class BurnTheNestHandler {

    public static void grantBurnTheNestAdvancement(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        Identifier advId = Identifier.of("web", "burn_spider_egg");
        AdvancementEntry advancement = server.getAdvancementLoader().get(advId);

        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
            if (!progress.isDone()) {
                player.getAdvancementTracker().grantCriterion(advancement, "burned_spider_egg");
            }
        }
    }
}