package potatowolfie.web.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class WebSounds {

    public static final SoundEvent WEB_THROW = registerSoundEvent("web_throw");
    public static final SoundEvent WEB_LAND = registerSoundEvent("web_land");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("web", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void registerSounds() {
        System.out.println("Registering Web Sounds");
    }
}