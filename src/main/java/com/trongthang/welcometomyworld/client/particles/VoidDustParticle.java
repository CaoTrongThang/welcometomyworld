package com.trongthang.welcometomyworld.client.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(EnvType.CLIENT)
public class VoidDustParticle extends SpriteBillboardParticle {

    protected final boolean isGlow;

    protected VoidDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY,
            double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);

        // Slow movement
        this.velocityX = velocityX + (Math.random() * 2.0 - 1.0) * 0.01;
        this.velocityY = velocityY + (Math.random() * 2.0 - 1.0) * 0.01;
        this.velocityZ = velocityZ + (Math.random() * 2.0 - 1.0) * 0.01;

        // Size like very fine dust
        this.scale = 0.05F + this.random.nextFloat() * 0.05F;

        // Age based on Ash particle (lifetime)
        this.maxAge = (int) (20.0 / (Math.random() * 0.8 + 0.2));
        this.collidesWithWorld = false;

        // 30% chance to glow
        this.isGlow = this.random.nextFloat() < 0.3F;

        // Randomly pick a color from white, gray, or #024050
        int colorChoice = this.random.nextInt(3);
        if (colorChoice == 0) {
            this.setColor(1.0F, 1.0F, 1.0F); // White
        } else if (colorChoice == 1) {
            this.setColor(128F / 255F, 128F / 255F, 128F / 255F); // Gray
        } else {
            this.setColor(2F / 255F, 64F / 255F, 80F / 255F); // #024050 (Teal/Sculk)
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getBrightness(float tint) {
        return this.isGlow ? 15728880 : super.getBrightness(tint);
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        // Float / drift physics upwards
        this.velocityX += (this.random.nextFloat() - this.random.nextFloat()) * 0.005F;
        this.velocityY += 0.005F; // Drifting upward instead of downward
        this.velocityZ += (this.random.nextFloat() - this.random.nextFloat()) * 0.005F;

        this.move(this.velocityX, this.velocityY, this.velocityZ);

        this.velocityX *= 0.95;
        this.velocityY *= 0.95;
        this.velocityZ *= 0.95;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d,
                double e, double f, double g, double h, double i) {
            VoidDustParticle particle = new VoidDustParticle(clientWorld, d, e, f, g, h, i);
            particle.setSprite(this.spriteProvider);
            return particle;
        }
    }
}
