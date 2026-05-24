package com.trongthang.welcometomyworld.Utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks cumulative damage dealt to each LivingEntity by individual attackers.
 * Keyed by victim UUID. Cleared when the victim dies via getAndClear().
 * Server-side only.
 */
public class DamageTracker {

    // Map<victimUUID, Map<attackerUUID, totalDamageDealt>>
    private static final Map<UUID, Map<UUID, Float>> tracker = new HashMap<>();

    /**
     * Stores pending damage shares (playerUUID -> share 0.0-1.0) so that
     * LevelingLogicMixin can read them when WeaponLeveling fires updateForKill.
     * Populated by TameableEntityKillMobMixin on death, cleared after use.
     */
    public static final ThreadLocal<Map<UUID, Float>> PENDING_SHARES = ThreadLocal.withInitial(HashMap::new);

    public static void recordDamage(UUID victim, UUID attacker, float amount) {
        tracker.computeIfAbsent(victim, k -> new HashMap<>())
                .merge(attacker, amount, (a, b) -> a + b);
    }

    /**
     * Returns the damage map for the given victim and removes it from the tracker.
     * Returns an empty map if no data exists.
     */
    public static Map<UUID, Float> getAndClear(UUID victim) {
        Map<UUID, Float> data = tracker.remove(victim);
        return data != null ? data : new HashMap<>();
    }
}
