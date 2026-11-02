package potatowolfie.web.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;

public class WebParticles {

    public static final SimpleParticleType WEB_BURST = FabricParticleTypes.simple();

    public static final SimpleParticleType SPIDER_EGG_POP = FabricParticleTypes.simple();

    public static final SimpleParticleType SPIDER_SUCCULENT_CLOUD = FabricParticleTypes.simple();

    public static final SimpleParticleType WEB_TRAIL = FabricParticleTypes.simple();
    public static final SimpleParticleType POISON_WEB_TRAIL = FabricParticleTypes.simple();

    public static void registerModParticles() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Web.MOD_ID, "web_burst"), WEB_BURST);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_egg_pop"), SPIDER_EGG_POP);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_succulent_cloud"), SPIDER_SUCCULENT_CLOUD);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Web.MOD_ID, "web_trail"), WEB_TRAIL);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Web.MOD_ID, "poison_web_trail"), POISON_WEB_TRAIL);

        Web.LOGGER.info("Registering particles for " + Web.MOD_ID);
    }
}