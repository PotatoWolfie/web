package potatowolfie.web.trim;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import potatowolfie.web.Web;

import java.util.Optional;

public class WebTrimMaterials {
    public static final ResourceKey<TrimMaterial> SUCCULENT_GEL = ResourceKey.create(Registries.TRIM_MATERIAL,
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "succulent_gel"));

    public static void bootstrap(BootstrapContext<TrimMaterial> registry) {
        register(registry, SUCCULENT_GEL, Style.EMPTY.withColor(0xBFEB6D), WebTrimAssets.SUCCULENT_GEL);
    }

    public static Optional<Holder<TrimMaterial>> get(ItemStack stack) {
        Holder<TrimMaterial> material = stack.get(DataComponents.PROVIDES_TRIM_MATERIAL);
        return material != null ? Optional.of(material) : Optional.empty();
    }

    private static void register(BootstrapContext<TrimMaterial> registry, ResourceKey<TrimMaterial> key, Style style, Identifier paletteId) {
        Component text = Component.translatable(Util.makeDescriptionId("trim_material", key.identifier())).withStyle(style);
        registry.register(key, new TrimMaterial(paletteId, text));
    }

    private static ResourceKey<TrimMaterial> of(String id) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, Identifier.withDefaultNamespace(id));
    }
}