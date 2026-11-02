package potatowolfie.web.particle.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class WebTrailParticle extends SingleQuadParticle {

    protected WebTrailParticle(ClientLevel world, double x, double y, double z,
                               double vx, double vy, double vz,
                               SpriteSet sprites, float r, float g, float b) {
        super(world, x, y, z, 0.0, 0.0, 0.0, sprites.first());
        this.friction = 0.9F;
        this.gravity = 0.0F;
        this.hasPhysics = false;

        this.xd = vx + (this.random.nextDouble() - 0.5) * 0.01;
        this.yd = vy - 0.005 - this.random.nextDouble() * 0.01;
        this.zd = vz + (this.random.nextDouble() - 0.5) * 0.01;

        this.quadSize = 0.15F + this.random.nextFloat() * 0.2F;
        this.lifetime = 20 + this.random.nextInt(11);
        this.setColor(r, g, b);
        this.alpha = 1.0F;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        float life = (float) this.age / (float) this.lifetime;
        this.alpha = 1.0F - life * life;
    }

    @Environment(EnvType.CLIENT)
    public static class WhiteFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public WhiteFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz, RandomSource random) {
            return new WebTrailParticle(level, x, y, z, vx, vy, vz, sprites, 1.0F, 1.0F, 1.0F);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class PoisonFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public PoisonFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz, RandomSource random) {
            return new WebTrailParticle(level, x, y, z, vx, vy, vz, sprites, 0.55F, 0.85F, 0.4F);
        }
    }
}