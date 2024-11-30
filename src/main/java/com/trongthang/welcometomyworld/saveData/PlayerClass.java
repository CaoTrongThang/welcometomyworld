package com.trongthang.welcometomyworld.saveData;

public class PlayerClass {
    public boolean firstJoin = false;
    public boolean firstTeleportedToSky = false;
    public double playerFirstIntroDeathChance = 0;
    public boolean firstRemoveStartingItems = false;
    public boolean firstGivingStartingItems = false;
    public boolean introDeathByGod = false;
    public boolean introMessageAfterDeath = false;
    public boolean completeSpawningParticles = false;

    public int introTimeLimit = 1200;

    public boolean firstOriginSelectingScreen = false;
    public boolean completeOriginSelectingScreen = false;

    public int deaths;

    // ================= Achievements =================
    public boolean firstTouchGround = false;
    public boolean firstPunchingBlocksDamage = false;
    public boolean firstPunchingBlocksDie = false;
    public boolean firstBlockSpawnMobs = false;

    public boolean firstBedExplosion = false;
    public boolean firstFallingToWaterDie = false;
    // ================= Achievements =================

    public PlayerClass(boolean firstJoin) {
        this.firstJoin = firstJoin;
    }

    public static PlayerClass CreateExistPlayer() {

        PlayerClass player = new PlayerClass(true);

        player.firstJoin = true;
        player.firstTouchGround = true;
        player.firstTeleportedToSky = true;
        player.firstRemoveStartingItems = true;
        player.firstGivingStartingItems = true;
        player.introMessageAfterDeath = true;
        player.introDeathByGod = true;
        player.completeOriginSelectingScreen = true;

        return player;
    }
}
