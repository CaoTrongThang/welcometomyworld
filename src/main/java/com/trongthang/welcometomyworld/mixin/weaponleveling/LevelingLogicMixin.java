package com.trongthang.welcometomyworld.mixin.weaponleveling;

import com.trongthang.welcometomyworld.Utilities.DamageTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.weaponleveling.util.LevelingLogic")
public class LevelingLogicMixin {

    /**
     * Stores the health of the mob currently being processed for XP grants.
     * ThreadLocal ensures safety even if multiple entities die on the same tick.
     */
    private static final ThreadLocal<Float> CURRENT_MOB_HEALTH = new ThreadLocal<>();

    /**
     * Stores the damage share (0.0–1.0) for the player currently receiving gear XP.
     * 1.0 = full XP (solo kill or no tracking data). Set in captureContext, cleared
     * in releaseContext.
     */
    private static final ThreadLocal<Float> CURRENT_DAMAGE_SHARE = new ThreadLocal<>();

    @Inject(method = "updateForKill", at = @At("HEAD"))
    private static void welcometomyworld_captureContext(LivingEntity victim, DamageSource source,
            ItemStack specificStack,
            CallbackInfo ci) {
        if (victim != null) {
            CURRENT_MOB_HEALTH.set(victim.getMaxHealth());
        }
        // Resolve damage share for this player attacker
        if (source != null && source.getAttacker() instanceof PlayerEntity player) {
            Float share = DamageTracker.PENDING_SHARES.get().get(player.getUuid());
            CURRENT_DAMAGE_SHARE.set(share != null ? share : 1.0f);
        } else {
            CURRENT_DAMAGE_SHARE.set(1.0f);
        }
    }

    @Inject(method = "updateForKill", at = @At("RETURN"))
    private static void welcometomyworld_releaseContext(LivingEntity victim, DamageSource source,
            ItemStack specificStack,
            CallbackInfo ci) {
        CURRENT_MOB_HEALTH.remove();
        CURRENT_DAMAGE_SHARE.remove();
    }

    /**
     * Intercepts XP being given to an individual item and adds the health-based
     * bonus.
     * This allows a Level 1 helmet to get a different bonus than a Level 50 sword
     * from the same kill.
     */
    private static java.lang.reflect.Method GET_MAX_LEVEL_METHOD = null;
    private static java.lang.reflect.Method GET_MAX_PROGRESS_METHOD = null;
    private static boolean reflectionInitialized = false;

    private static void initReflection() {
        if (reflectionInitialized)
            return;
        try {
            Class<?> modUtilsClass = Class.forName("net.weaponleveling.util.ModUtils");
            GET_MAX_LEVEL_METHOD = modUtilsClass.getMethod("getMaxLevel", ItemStack.class);

            Class<?> levelingAPIClass = Class.forName("net.weaponleveling.api.LevelingAPI");
            GET_MAX_PROGRESS_METHOD = levelingAPIClass.getMethod("getMaxProgress", int.class, ItemStack.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        reflectionInitialized = true;
    }

    @ModifyVariable(method = "updateProgressItem", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static int welcometomyworld_applyPerItemScaling(int amount, LivingEntity attacker, ItemStack stack) {
        initReflection();

        Float health = CURRENT_MOB_HEALTH.get();
        if (health != null && stack != null && !stack.isEmpty()) {
            // 1. Base Quadratic Scaling
            double baseBonus = (health * health) / 72.0;

            // 2. High-Health Cubic Boost
            double extraHealth = Math.max(0, health - 80);
            double highHealthBonus = (extraHealth * extraHealth * extraHealth) / 30000.0;

            // 3. Safety Cap
            int bonus = (int) Math.min(2000000, baseBonus + highHealthBonus);
            int totalXP = amount + bonus;

            // 4. Multiply by damage share (proportional contribution to the kill)
            Float share = CURRENT_DAMAGE_SHARE.get();
            if (share != null) {
                totalXP = (int) (totalXP * share);
            }

            // 5. Strict Level Cap via Reflection
            // Prevents WeaponLeveling's "while-loop" bypassing the level 500 config cap on
            // massive XP drops.
            try {
                int currentLevel = 1;
                long currentProgress = 0;

                if (stack.hasNbt() && stack.getNbt().contains("level")) {
                    currentLevel = Math.max(1, stack.getNbt().getInt("level"));
                }
                if (stack.hasNbt() && stack.getNbt().contains("levelprogress")) {
                    currentProgress = stack.getNbt().getLong("levelprogress");
                }

                if (GET_MAX_LEVEL_METHOD != null && GET_MAX_PROGRESS_METHOD != null) {
                    int maxLevel = (int) GET_MAX_LEVEL_METHOD.invoke(null, stack);

                    if (currentLevel >= maxLevel) {
                        return 0; // Already max level
                    }

                    long xpNeededToMax = 0;
                    for (int l = currentLevel; l < maxLevel; l++) {
                        long maxProgressForLevel = (long) GET_MAX_PROGRESS_METHOD.invoke(null, l, stack);
                        xpNeededToMax += maxProgressForLevel;
                    }

                    xpNeededToMax -= currentProgress;

                    if (totalXP > xpNeededToMax) {
                        totalXP = (int) Math.max(0, xpNeededToMax);
                    }
                }
            } catch (Exception e) {
                // Ignore if reflection breaks, it just means we don't strict cap it.
            }

            return totalXP;
        }
        return amount;
    }

    @Inject(method = "updateProgressItem", at = @At("RETURN"))
    private static void welcometomyworld_hardClampLevel(LivingEntity attacker, ItemStack stack, int updateamount,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (stack == null || stack.isEmpty() || !stack.hasNbt())
            return;

        initReflection();

        try {
            if (GET_MAX_LEVEL_METHOD != null) {
                int maxLevel = (int) GET_MAX_LEVEL_METHOD.invoke(null, stack);
                int currentLevel = stack.getNbt().getInt("level");

                if (currentLevel > maxLevel) {
                    // Set back to max level and clear progress
                    stack.getNbt().putInt("level", maxLevel);
                    stack.getNbt().putLong("levelprogress", 0);

                    // Log it if needed, or just silently fix
                }
            }
        } catch (Exception e) {
            // Silently fail
        }
    }

    @Inject(method = "getXPForHit", at = @At("RETURN"), cancellable = true)
    private static void welcometomyworld_cleanHitXP(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        // We keep the original hit XP logic but ensure we return the value correctly.
        // This injector is here to maintain the hook if the user wants to add
        // hit-scaling later.
        cir.setReturnValue(cir.getReturnValue());
    }
}
