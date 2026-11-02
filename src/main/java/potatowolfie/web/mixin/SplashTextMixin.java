package potatowolfie.web.mixin;

import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;

@Mixin(SplashManager.class)
public class SplashTextMixin {

    @Shadow
    private List<Component> splashes;

    @Inject(method = "apply*", at = @At("TAIL"))
    private void addWebModSplashes(CallbackInfo ci) {
        splashes = new ArrayList<>(splashes);

        Style yellowStyle = Style.EMPTY.withColor(0xFFFF00);

        splashes.add(Component.translatable("splash.web.thwip").setStyle(yellowStyle));
        splashes.add(Component.translatable("splash.web.friendly_neighborhood").setStyle(yellowStyle));
        splashes.add(Component.translatable("splash.web.cave_spiders").setStyle(yellowStyle));
        splashes.add(Component.translatable("splash.web.reminder").setStyle(yellowStyle));
        splashes.add(Component.translatable("splash.web.looks").setStyle(yellowStyle));
        splashes.add(Component.translatable("splash.web.lunguinis").setStyle(yellowStyle));
    }
}