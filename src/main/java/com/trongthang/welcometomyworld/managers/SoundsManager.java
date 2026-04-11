package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class SoundsManager {

    public static final SoundEvent TIRED_SOUND = registerSoundEvent("tired_sound");

    public static final SoundEvent ENDERCHESTER_AMBIENT = registerSoundEvent("enderchester_ambient");
    public static final SoundEvent ENDERCHESTER_AMBIENT2 = registerSoundEvent("enderchester_ambient2");
    public static final SoundEvent ENDERCHESTER_HURT = registerSoundEvent("enderchester_hurt");
    public static final SoundEvent ENDERCHESTER_MUNCH = registerSoundEvent("enderchester_munch");

    public static final SoundEvent PORTALER_SPIN = registerSoundEvent("portaler_spin");
    public static final SoundEvent PORTALER_STEP = registerSoundEvent("portaler_step");
    public static final SoundEvent PORTALER_PORTAL_CHANGE = registerSoundEvent("portaler_portal_change");
    public static final SoundEvent PORTALER_HURT = registerSoundEvent("portaler_hurt");
    public static final SoundEvent PORTALER_DEATH = registerSoundEvent("portaler_death");

    public static final SoundEvent ENDER_PEST_MOUTH_OPEN = registerSoundEvent("ender_pest_mouth_open");
    public static final SoundEvent ENDER_PEST_SHAKE1 = registerSoundEvent("ender_pest_shake1");
    public static final SoundEvent ENDER_PEST_SHAKE2 = registerSoundEvent("ender_pest_shake2");
    public static final SoundEvent ENDER_PEST_SHAKE3 = registerSoundEvent("ender_pest_shake3");

    public static final SoundEvent FALLEN_KNIGHT_ARMOR_SHAKING = registerSoundEvent("fallen_knight_armor_shaking");
    public static final SoundEvent FALLEN_KNIGHT_FALL = registerSoundEvent("fallen_knight_fall");
    public static final SoundEvent FALLEN_KNIGHT_SWING = registerSoundEvent("fallen_knight_swing");
    public static final SoundEvent FALLEN_KNIGHT_SWING_UP = registerSoundEvent("fallen_knight_swing_up");
    public static final SoundEvent FALLEN_KNIGHT_GROUND_IMPACT = registerSoundEvent("fallen_knight_ground_impact");
    public static final SoundEvent FALLEN_KNIGHT_GROUND_IMPACT_NO_DELAY = registerSoundEvent(
            "fallen_knight_ground_impact_no_delay");
    public static final SoundEvent FALLEN_KNIGHT_STEP = registerSoundEvent("fallen_knight_step");
    public static final SoundEvent FALLEN_KNIGHT_PORTAL_AMBIENT = registerSoundEvent("fallen_knight_portal_ambient");
    public static final SoundEvent FALLEN_KNIGHT_PORTAL_OPEN = registerSoundEvent("fallen_knight_portal_open");

    public static final SoundEvent WANDERER_BACKFLIP = registerSoundEvent("wanderer_backflip");
    public static final SoundEvent WANDERER_BOW_ATTACK = registerSoundEvent("wanderer_bow_attack");
    public static final SoundEvent WANDERER_POTION_DRINKING = registerSoundEvent("wanderer_potion_drinking");
    public static final SoundEvent WANDERER_SWORD_SLASH = registerSoundEvent("wanderer_sword_slash");
    public static final SoundEvent WANDERER_SWORD_CHARGE = registerSoundEvent("wanderer_sword_charge");
    public static final SoundEvent WANDERER_WALK = registerSoundEvent("wanderer_walk");
    public static final SoundEvent WANDERER_BLOCK = registerSoundEvent("wanderer_block");

    public static final SoundEvent HOSTILE_MOB_BUFF = registerSoundEvent("hostile_mob_buff");

    public static final SoundEvent BLOSSOM_BUFF = registerSoundEvent("blossom_buff");
    public static final SoundEvent BLOSSOM_LAUGH = registerSoundEvent("blossom_laugh");
    public static final SoundEvent BLOSSOM_ULTIMATE = registerSoundEvent("blossom_ultimate");
    public static final SoundEvent BLOSSOM_WALK = registerSoundEvent("blossom_walk");
    public static final SoundEvent BLOSSOM_RISE = registerSoundEvent("blossom_rise");
    public static final SoundEvent BLOSSOM_AMBIENT = registerSoundEvent("blossom_ambient");
    public static final SoundEvent BLOSSOM_HURT = registerSoundEvent("blossom_hurt");

    public static final SoundEvent DASH_CAPE = registerSoundEvent("dash_cape");
    public static final SoundEvent SPEAR_ATTACK_1 = registerSoundEvent("spear_attack_1");
    public static final SoundEvent SPEAR_ATTACK_2 = registerSoundEvent("spear_attack_2");
    public static final SoundEvent SPEAR_ATTACK_3 = registerSoundEvent("spear_attack_3");
    public static final SoundEvent SPEAR_ATTACK_4 = registerSoundEvent("spear_attack_4");
    public static final SoundEvent SPEAR_ATTACK_5 = registerSoundEvent("spear_attack_5");
    public static final SoundEvent SUMMON_CIRCLE = registerSoundEvent("summon_circle");
    public static final SoundEvent WHOOSH_1 = registerSoundEvent("whoosh_1");
    public static final SoundEvent EXPLODE_PUNCH = registerSoundEvent("explode_punch");

    public static final SoundEvent VOID_WORM_AMBIENT_1 = registerSoundEvent("void_worm_ambient_1");
    public static final SoundEvent MONSTER_ROAR = registerSoundEvent("monster_roar");
    public static final SoundEvent VOID_WORM_AMBIENT_2 = registerSoundEvent("void_worm_ambient_2");
    public static final SoundEvent VOID_WORM_AMBIENT_3 = registerSoundEvent("void_worm_ambient_3");
    public static final SoundEvent VOID_WORM_DEATH_ROAR = registerSoundEvent("void_worm_death_roar");
    public static final SoundEvent CRYSTAL_SPAWNED_1 = registerSoundEvent("crystal_spawned_1");
    public static final SoundEvent CRYSTAL_SPAWNED_2 = registerSoundEvent("crystal_spawned_2");
    public static final SoundEvent EARTH_RUMBLE = registerSoundEvent("earth_rumble");
    public static final SoundEvent VOID_WORM_CHARGE_ATTACK = registerSoundEvent("void_worm_charge_attack");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(WelcomeToMyWorld.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        LOGGER.info("Registring sounds for " + WelcomeToMyWorld.MOD_ID);
    }

}
