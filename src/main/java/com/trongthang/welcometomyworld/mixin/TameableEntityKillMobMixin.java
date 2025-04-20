package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.tameablePacket.TameableEntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.GlobalVariables.DEFAULT_XP_TAMEABLE_MOB;
import static com.trongthang.welcometomyworld.GlobalVariables.EXP_MULTIPLIER_EACH_LEVEL_MOB;

// every tameable mob has a leveling system
// This class use to scaled the xp the tameable mob get when kill hostile mobs
@Mixin(LivingEntity.class)
public class TameableEntityKillMobMixin {
    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        // Ensure we're on the server side
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity.getWorld().isClient) return;

        // Get the attacker
        Entity attacker = damageSource.getAttacker();

        // Check if the attacker is a tameable mob
        if (attacker instanceof TameableEntity tameableEntity) {

            TameableEntityInterface entityInterface = (TameableEntityInterface) tameableEntity;

            float targetHealth = livingEntity.getMaxHealth();
            float scaleFactor = 10;
            int maxAttempts = 200;
            int counter = 0;

            // Prevent division by zero
            float expGained = Math.max(1, (targetHealth) / WelcomeToMyWorld.random.nextFloat(5f, scaleFactor));
            expGained += (float) (livingEntity.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR));

            double stateRate = tameableEntity.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR) + tameableEntity.getMaxHealth();
            double scaleXpByStateRate = 1000 / (stateRate + 1);

            float newExp = (float) (entityInterface.getCurrentLevelExp() + (expGained + (expGained * scaleXpByStateRate)));

            entityInterface.setCurrentLevelExp(newExp);

            float currentExp = entityInterface.getCurrentLevelExp();
            float requiredExp = entityInterface.getNextLevelRequireExp();
            int levelsGained = 0;

            while (currentExp >= requiredExp && counter < maxAttempts) {
                // Calculate overflow EXP
                currentExp -= requiredExp;
                levelsGained++;

                // Update required EXP for next level
                requiredExp = (float) (DEFAULT_XP_TAMEABLE_MOB * (Math.pow(EXP_MULTIPLIER_EACH_LEVEL_MOB, entityInterface.getCurrentLevel())));
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
                            .append(Text.literal("Your " + mobName + " leveled up to "))
                            .append(Text.literal(String.valueOf(newLevel))
                                    .formatted(Formatting.YELLOW));

                    player.sendMessage(message, false);
                }
            }
        }
    }
}
