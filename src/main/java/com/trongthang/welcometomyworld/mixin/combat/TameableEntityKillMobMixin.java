package com.trongthang.welcometomyworld.mixin.combat;

import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.Utilities.DamageTracker;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.tameablePacket.TameableEntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

import static com.trongthang.welcometomyworld.GlobalVariables.DEFAULT_XP_TAMEABLE_MOB;
import static com.trongthang.welcometomyworld.GlobalVariables.EXP_MULTIPLIER_EACH_LEVEL_MOB;

/**
 * On mob death, distributes XP to ALL tamed mobs that contributed damage,
 * proportional to their damage share (damageDealt / maxHealth).
 * Also stores damage shares into PENDING_SHARES so LevelingLogicMixin
 * can scale gear XP for players by the same proportion.
 */
@Mixin(LivingEntity.class)
public class TameableEntityKillMobMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity victim = (LivingEntity) (Object) this;
        if (victim.getWorld().isClient)
            return;
        if (!(victim.getWorld() instanceof ServerWorld serverWorld))
            return;

        float maxHealth = victim.getMaxHealth();
        if (maxHealth <= 0)
            return;

        // Pull all damage contributions and clear the tracker entry
        Map<UUID, Float> contributions = DamageTracker.getAndClear(victim.getUuid());
        if (contributions.isEmpty())
            return;

        // Pre-fill pending shares map for LevelingLogicMixin
        Map<UUID, Float> pendingShares = DamageTracker.PENDING_SHARES.get();
        pendingShares.clear();

        for (Map.Entry<UUID, Float> entry : contributions.entrySet()) {
            UUID attackerUuid = entry.getKey();
            float damageDealt = entry.getValue();
            float share = Math.min(1.0f, damageDealt / maxHealth);

            Entity attacker = serverWorld.getEntity(attackerUuid);
            if (attacker == null)
                continue;

            if (attacker instanceof PlayerEntity player) {
                // Gear XP for players is handled by LevelingLogicMixin via PENDING_SHARES
                pendingShares.put(attackerUuid, share);

                // Force WeaponLeveling to process this player if they are not the actual killer
                Entity actualKiller = damageSource.getAttacker();
                if (actualKiller == null || !actualKiller.getUuid().equals(attackerUuid)) {
                    try {
                        Class<?> clazz = Class.forName("net.weaponleveling.util.LevelingLogic");
                        java.lang.reflect.Method method = clazz.getMethod("updateForKill", LivingEntity.class,
                                DamageSource.class, net.minecraft.item.ItemStack.class);
                        DamageSource fakeSource = serverWorld.getDamageSources().playerAttack(player);
                        method.invoke(null, victim, fakeSource, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } else if (attacker instanceof TameableEntity tameableEntity) {
                String tameableId = net.minecraft.registry.Registries.ENTITY_TYPE
                        .getId(tameableEntity.getType()).toString();
                if (Utils.matchesPattern(tameableId, ConfigLoader.getInstance().excludedUpgradeMobs)) {
                    continue;
                }

                TameableEntityInterface entityInterface = (TameableEntityInterface) tameableEntity;

                float scaleFactor = 10;
                float baseExpGained = Math.max(1, maxHealth / WelcomeToMyWorld.random.nextFloat(5f, scaleFactor));
                baseExpGained += (float) victim.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR);

                double stateRate = tameableEntity.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR)
                        + tameableEntity.getMaxHealth();
                double scaleXpByStateRate = 1000.0 / (stateRate + 1);

                float expGained = (float) ((baseExpGained + (baseExpGained * scaleXpByStateRate)) * share);

                float newExp = entityInterface.getCurrentLevelExp() + expGained;
                entityInterface.setCurrentLevelExp(newExp);

                float currentExp = entityInterface.getCurrentLevelExp();
                float requiredExp = entityInterface.getNextLevelRequireExp();
                int levelsGained = 0;
                int counter = 0;

                while (currentExp >= requiredExp && counter < 200) {
                    currentExp -= requiredExp;
                    levelsGained++;
                    requiredExp = (float) (DEFAULT_XP_TAMEABLE_MOB
                            * Math.pow(EXP_MULTIPLIER_EACH_LEVEL_MOB, entityInterface.getCurrentLevel()));
                    counter++;
                }

                if (levelsGained > 0) {
                    entityInterface.setCurrentLevel(entityInterface.getCurrentLevel() + levelsGained);
                    entityInterface.setPointAvailalble(entityInterface.getPointAvailalble() + levelsGained);
                    entityInterface.setCurrentLevelExp(currentExp);
                    entityInterface.setNextLevelRequireExp(requiredExp);

                    LivingEntity owner = tameableEntity.getOwner();
                    if (owner instanceof PlayerEntity player) {
                        String mobName = tameableEntity.getName().getString();
                        int newLevel = (int) entityInterface.getCurrentLevel();

                        Text message = Text.literal("")
                                .formatted(Formatting.WHITE)
                                .append(Text.literal("Your "))
                                .append(Text.literal(mobName).formatted(Formatting.YELLOW))
                                .append(Text.literal(" leveled up to "))
                                .append(Text.literal(String.valueOf(newLevel)).formatted(Formatting.YELLOW))
                                .append(Text.literal(", look at it and press ["))
                                .append(Text.keybind("key.welcometomyworld.open_mob_stats")
                                        .formatted(Formatting.YELLOW))
                                .append(Text.literal("] to open the upgrade menu!"));

                        player.sendMessage(message, false);
                    }
                }
            }
        }
        // PENDING_SHARES stays populated; LevelingLogicMixin cleans it up after
        // updateForKill
    }
}
