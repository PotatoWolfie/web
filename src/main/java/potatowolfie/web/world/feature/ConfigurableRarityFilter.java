package potatowolfie.web.world.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import potatowolfie.web.config.WebConfig;

import java.util.function.Consumer;

public class ConfigurableRarityFilter implements PlacementModifier {

    public static final ConfigurableRarityFilter INSTANCE = new ConfigurableRarityFilter();

    public static final MapCodec<ConfigurableRarityFilter> CODEC = MapCodec.unit(() -> INSTANCE);

    private ConfigurableRarityFilter() {
    }

    @Override
    public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        int rarity = Math.max(1, WebConfig.get().spiderEggClusterRarity);
        if (random.nextInt(rarity) == 0) {
            output.accept(origin);
        }
    }

    @Override
    public MapCodec<? extends PlacementModifier> codec() {
        return CODEC;
    }
}