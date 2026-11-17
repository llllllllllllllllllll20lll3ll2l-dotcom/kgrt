package com.manyl.mod.client.particle;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * Клиентские классы кастомных частиц: руны призыва, рабочие искры, сердце ядра.
 */
public final class ManylParticles {

    public static class RuneParticle extends SingleQuadParticle {
        private final FabricSpriteProvider sprites;

        protected RuneParticle(ClientLevel level, double x, double y, double z,
                               double vx, double vy, double vz, FabricSpriteProvider sprites) {
            super(level, x, y, z, vx, vy + 0.04D, vz, sprites.get(0, 1));
            this.sprites = sprites;
            this.lifetime = 22 + level.getRandom().nextInt(10);
            this.quadSize = 0.22F;
            this.gravity = -0.02F;
            this.setSpriteFromAge(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            this.setSpriteFromAge(this.sprites);
            this.alpha = Math.max(0.0F, 1.0F - (float) this.age / (float) this.lifetime);
        }

        @Override
        protected Layer getLayer() {
            return Layer.TRANSLUCENT;
        }
    }

    public static class SparkParticle extends SingleQuadParticle {
        protected SparkParticle(ClientLevel level, double x, double y, double z,
                                double vx, double vy, double vz, TextureAtlasSprite sprite) {
            super(level, x, y, z, vx, vy, vz, sprite);
            this.lifetime = 12 + level.getRandom().nextInt(8);
            this.quadSize = 0.14F;
            this.gravity = 0.15F;
        }

        @Override
        public void tick() {
            super.tick();
            this.alpha = Math.max(0.0F, 1.0F - (float) this.age / (float) this.lifetime);
        }

        @Override
        protected Layer getLayer() {
            return Layer.TRANSLUCENT;
        }
    }

    public record RuneFactory(FabricSpriteProvider sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double vx, double vy, double vz, RandomSource rng) {
            return new RuneParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }

    public record SparkFactory(FabricSpriteProvider sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double vx, double vy, double vz, RandomSource rng) {
            return new SparkParticle(level, x, y, z, vx, vy, vz, sprites.get(rng));
        }
    }
}
