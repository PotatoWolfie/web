package potatowolfie.web.advancement;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

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