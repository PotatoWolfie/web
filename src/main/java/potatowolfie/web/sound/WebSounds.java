package potatowolfie.web.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class WebSounds {

    public static final SoundEvent WEB_THROW = registerSoundEvent("web_throw");
    public static final SoundEvent WEB_LAND = registerSoundEvent("web_land");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of("web", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        System.out.println("Registering Web Sounds");
    }
}