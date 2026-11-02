package potatowolfie.web.particle.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class SpiderSucculentCloudParticle extends SingleQuadParticle {
    private static final double HORIZONTAL_DRAG = 0.95;
    private static final double VERTICAL_DRAG = 0.90;
    private static final double FALL_ACCEL = 0.002;

    private final SpriteSet sprites;

    protected SpiderSucculentCloudParticle(ClientLevel world, double x, double y, double z,
                                           double velocityX, double velocityY, double velocityZ,
                                           SpriteSet sprites) {
        super(world, x, y, z, 0.0, 0.0, 0.0, sprites.first());
        this.sprites = sprites;

        this.friction = 1.0F;
        this.gravity = 0.0F;
        this.hasPhysics = true;

        this.quadSize *= 1.75F;
        this.lifetime = 100 + this.random.nextInt(40);
        this.alpha = 0.8F;
        this.setColor(0.55F, 0.85F, 0.4F);

        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double speed = 0.04 + this.random.nextDouble() * 0.05;
        this.xd = Math.cos(angle) * speed + velocityX;
        this.zd = Math.sin(angle) * speed + velocityZ;
        this.yd = 0.06 + this.random.nextDouble() * 0.04 + velocityY;

        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTick) {
        float t = Mth.clamp((this.age + partialTick) / this.lifetime * 2.0F, 0.0F, 1.0F);
        return this.quadSize * Mth.lerp(t, 0.6F, 1.2F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }

        this.xd *= HORIZONTAL_DRAG;
        this.zd *= HORIZONTAL_DRAG;
        this.yd = this.yd * VERTICAL_DRAG - FALL_ACCEL;

        float life = (float) this.age / (float) this.lifetime;
        this.alpha = life < 0.5F ? 0.8F : 0.8F * (1.0F - (life - 0.5F) * 2.0F);

        this.setSpriteFromAge(this.sprites);
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double vx, double vy, double vz, RandomSource random) {
            return new SpiderSucculentCloudParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}