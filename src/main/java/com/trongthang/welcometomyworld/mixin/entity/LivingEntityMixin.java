package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.interfaces.IScaleEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IScaleEntity {

    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow
    public abstract StatusEffectInstance getStatusEffect(StatusEffect effect);

    @Shadow
    public abstract void setHealth(float health);

    @Shadow
    public abstract boolean isAlive();

    public LivingEntityMixin(net.minecraft.entity.EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    private static final ThreadLocal<Boolean> welcometomyworld$handlingDamage = ThreadLocal.withInitial(() -> false);

    @Unique
    private float welcometomyworld$scale = 1.0f;

    @Override
    @Unique
    public void setScale(float scale) {
        this.welcometomyworld$scale = scale;
    }

    @Override
    @Unique
    public float getScale() {
        return this.welcometomyworld$scale;
    }

    @Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
    private void onDamageHeadRecursionGuard(net.minecraft.entity.damage.DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> cir) {
        if (welcometomyworld$handlingDamage.get()) {
            cir.setReturnValue(false);
        } else {
            welcometomyworld$handlingDamage.set(true);
        }
    }

    @Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("RETURN"))
    private void onDamageReturnRecursionGuard(net.minecraft.entity.damage.DamageSource source, float amount,
            CallbackInfoReturnable<Boolean> cir) {
        welcometomyworld$handlingDamage.set(false);
    }

    private static boolean welcometomyworld$handling = false;

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void onHasStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> cir) {
        if (welcometomyworld$handling)
            return;

        if (effect == StatusEffects.NIGHT_VISION) {
            welcometomyworld$handling = true;
            try {
                LivingEntity entity = (LivingEntity) (Object) this;
                boolean inVoid = entity.getWorld().getRegistryKey().getValue().toString()
                        .equals("welcometomyworld:void_dim");
                boolean hasVoidSight = entity
                        .hasStatusEffect(com.trongthang.welcometomyworld.managers.EffectsManager.VOID_SIGHT);

                if (inVoid) {
                    cir.setReturnValue(hasVoidSight);
                } else if (hasVoidSight) {
                    cir.setReturnValue(true);
                }
            } finally {
                welcometomyworld$handling = false;
            }
        }
    }

    @Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
    private void onGetStatusEffect(StatusEffect effect, CallbackInfoReturnable<StatusEffectInstance> cir) {
        if (welcometomyworld$handling)
            return;

        if (effect == StatusEffects.NIGHT_VISION) {
            welcometomyworld$handling = true;
            try {
                LivingEntity entity = (LivingEntity) (Object) this;
                boolean inVoid = entity.getWorld().getRegistryKey().getValue().toString()
                        .equals("welcometomyworld:void_dim");
                StatusEffectInstance voidSight = entity
                        .getStatusEffect(com.trongthang.welcometomyworld.managers.EffectsManager.VOID_SIGHT);

                if (inVoid) {
                    cir.setReturnValue(voidSight);
                } else if (voidSight != null) {
                    StatusEffectInstance vanillaNV = entity.getStatusEffect(StatusEffects.NIGHT_VISION);
                    if (vanillaNV == null) {
                        cir.setReturnValue(voidSight);
                    }
                }
            } finally {
                welcometomyworld$handling = false;
            }
        }
    }

    // scale the players
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (welcometomyworld$scale < 1.0f) {
            welcometomyworld$scale += 0.01f;
            if (welcometomyworld$scale > 1.0f)
                welcometomyworld$scale = 1.0f;
        }

        // Guard against NaN health or "stuck alive" state (health <= 0 but not dead).
        // These states cause immortality because the entity never processes death
        // correctly.
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.getWorld().isClient()) {
            boolean isNaN = Float.isNaN(self.getHealth());
            boolean isStuckAlive = self.getHealth() <= 0 && self.isAlive();

            if (isNaN || isStuckAlive) {
                boolean debugLog = com.trongthang.welcometomyworld.ConfigLoader.getInstance().oneShotDebugLog;
                if (debugLog) {
                    String reason = isNaN ? "NaN health" : "stuck alive with 0 health";
                    WelcomeToMyWorld.LOGGER.error("[NaNHealthGuard] " + self.getType()
                            + " UUID=" + self.getUuidAsString() + " has " + reason
                            + " — killing it to prevent immortality.");
                }
                setHealth(0.0f);
                self.kill();
            }
        }
    }
}
