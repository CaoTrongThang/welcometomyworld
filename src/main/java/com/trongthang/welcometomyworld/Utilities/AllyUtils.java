package com.trongthang.welcometomyworld.Utilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AllyUtils {
    // Cache methods for different classes to improve performance
    private static final Map<Class<?>, Method> GET_TRUE_OWNER_CACHE = new ConcurrentHashMap<>();
    private static final Method NULL_METHOD;

    static {
        try {
            // Placeholder for "no method found" to avoid repetitive failed lookups
            NULL_METHOD = AllyUtils.class.getDeclaredMethod("placeholder");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("AllyUtils failed to initialize placeholder method", e);
        }
    }

    private static void placeholder() {
    }

    /**
     * Centralized ally check for all entities.
     *
     * @param source The entity performing the check (e.g., the attacker or target
     *               selector)
     * @param target The potential ally/target
     * @return true if target should be treated as an ally of source
     */
    public static boolean isAlly(Entity source, Entity target) {
        if (target == null || source == null)
            return false;
        if (target == source)
            return true;

        // 1. Check if they share the same owner or if target is the owner
        if (source instanceof TameableEntity tameableSource) {
            LivingEntity owner = tameableSource.getOwner();
            if (owner != null && target.equals(owner))
                return true;

            if (target instanceof TameableEntity tameableTarget && tameableTarget.isTamed()) {
                LivingEntity targetOwner = tameableTarget.getOwner();
                if (owner != null && owner.equals(targetOwner))
                    return true;
            }
        }

        // 2. Player is always an ally to tamed things
        if (target instanceof PlayerEntity)
            return true;

        // 3. Tamed animals are generally allies to players and other tamed things
        if (target instanceof TameableEntity tameableTarget && tameableTarget.isTamed()) {
            return true;
        }

        // 4. Support Goety mod entities summoned by players
        if (Registries.ENTITY_TYPE.getId(target.getType()).getNamespace().equals("goety")) {
            LivingEntity goetyOwner = getGoetyOwner(target);
            if (goetyOwner instanceof PlayerEntity) {
                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to retrieve the owner of a Goety entity using reflection.
     */
    private static LivingEntity getGoetyOwner(Entity entity) {
        Class<?> clazz = entity.getClass();
        Method method = GET_TRUE_OWNER_CACHE.get(clazz);

        if (method == null) {
            try {
                // We assume there's a getTrueOwner() method based on the Summoned/Owned code
                // provided
                method = clazz.getMethod("getTrueOwner");
                GET_TRUE_OWNER_CACHE.put(clazz, method);
            } catch (NoSuchMethodException e) {
                GET_TRUE_OWNER_CACHE.put(clazz, NULL_METHOD);
                return null;
            }
        }

        if (method == NULL_METHOD)
            return null;

        try {
            Object result = method.invoke(entity);
            if (result instanceof LivingEntity) {
                return (LivingEntity) result;
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
