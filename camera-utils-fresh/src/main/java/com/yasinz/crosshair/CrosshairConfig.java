package com.yasinz.crosshair;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "camera-utils-crosshair")
public class CrosshairConfig implements ConfigData {

    // ============================================================
    // GENERAL
    // ============================================================

    @ConfigEntry.Gui.Tooltip
    public boolean showCrosshairInThirdPerson = true;

    @ConfigEntry.Gui.EnumHandler(
            option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON
    )
    public CrosshairMode mode = CrosshairMode.SMART;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int smoothness = 70;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int movementStabilization = 75;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int aimAnticipation = 25;


    // ============================================================
    // PVP
    // ============================================================

    @ConfigEntry.Category("pvp")
    @ConfigEntry.Gui.Tooltip
    public boolean targetDetection = true;

    @ConfigEntry.Category("pvp")
    @ConfigEntry.Gui.Tooltip
    public boolean hitFeedback = true;

    @ConfigEntry.Category("pvp")
    @ConfigEntry.Gui.Tooltip
    public boolean attackCooldown = true;

    @ConfigEntry.Category("pvp")
    @ConfigEntry.Gui.Tooltip
    public boolean targetDistance = false;

    @ConfigEntry.Category("pvp")
    @ConfigEntry.Gui.Tooltip
    public boolean entityIndicator = true;

    @ConfigEntry.Category("pvp")
    @ConfigEntry.Gui.Tooltip
    public boolean outOfReachIndicator = true;


    // ============================================================
    // AIM ASSIST
    // ============================================================

    @ConfigEntry.Category("aim_assist")
    @ConfigEntry.Gui.Tooltip
    public boolean aimAssist = true;

    /*
     * OFF = Aim Assist works only in third person.
     * ON  = Aim Assist works in both first and third person.
     */
    @ConfigEntry.Category("aim_assist")
    @ConfigEntry.Gui.Tooltip
    public boolean aimAssistFirstPerson = false;

    @ConfigEntry.Category("aim_assist")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int aimAssistStrength = 30;

    @ConfigEntry.Category("aim_assist")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 2, max = 20)
    public int aimAssistRange = 8;

    @ConfigEntry.Category("aim_assist")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 45)
    public int aimAssistAngle = 18;

    @ConfigEntry.Category("aim_assist")
    @ConfigEntry.Gui.Tooltip
    public boolean aimAssistPlayers = true;

    @ConfigEntry.Category("aim_assist")
    @ConfigEntry.Gui.Tooltip
    public boolean aimAssistMobs = true;


    // ============================================================
    // BUILDING
    // ============================================================

    @ConfigEntry.Category("building")
    @ConfigEntry.Gui.Tooltip
    public boolean buildingMode = true;

    @ConfigEntry.Category("building")
    @ConfigEntry.Gui.Tooltip
    public boolean blockFaceIndicator = true;

    @ConfigEntry.Category("building")
    @ConfigEntry.Gui.Tooltip
    public boolean blockFaceIndicatorFirstPerson = false;

    @ConfigEntry.Category("building")
    @ConfigEntry.Gui.Tooltip
    public boolean placementIndicator = true;

    @ConfigEntry.Category("building")
    @ConfigEntry.Gui.Tooltip
    public boolean placementIndicatorFirstPerson = false;

    @ConfigEntry.Category("building")
    @ConfigEntry.Gui.Tooltip
    public boolean placementValidity = true;

    @ConfigEntry.Category("building")
    @ConfigEntry.Gui.Tooltip
    public boolean reachIndicator = true;

    @ConfigEntry.Category("building")
    @ConfigEntry.Gui.Tooltip
    public boolean placementAnimation = true;

    @ConfigEntry.Category("building")
    @ConfigEntry.Gui.Tooltip
    public boolean placementGhost = true;

    @ConfigEntry.Category("building")
    @ConfigEntry.Gui.Tooltip
    public boolean placementGhostFirstPerson = false;


    // ============================================================
    // PROJECTILES
    // ============================================================

    @ConfigEntry.Category("projectiles")
    @ConfigEntry.Gui.Tooltip
    public boolean projectilePrediction = true;

    @ConfigEntry.Category("projectiles")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 100)
    public int predictionLength = 50;

    @ConfigEntry.Category("projectiles")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public int predictionDots = 5;


    // ============================================================
    // EFFECTS
    // ============================================================

    @ConfigEntry.Category("effects")
    @ConfigEntry.Gui.Tooltip
    public boolean targetAcquisitionAnimation = true;

    @ConfigEntry.Category("effects")
    @ConfigEntry.Gui.Tooltip
    public boolean targetLostAnimation = true;

    @ConfigEntry.Category("effects")
    @ConfigEntry.Gui.Tooltip
    public boolean landingStabilization = true;

    @ConfigEntry.Category("effects")
    @ConfigEntry.Gui.Tooltip
    public boolean crosshairSpring = true;

    @ConfigEntry.Category("effects")
    @ConfigEntry.Gui.Tooltip
    public boolean directionalAnticipation = true;


    // ============================================================
    // ADVANCED
    // ============================================================

    @ConfigEntry.Category("advanced")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int springStrength = 80;

    @ConfigEntry.Category("advanced")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int springDamping = 75;

    @ConfigEntry.Category("advanced")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int deadZone = 20;

    @ConfigEntry.Category("advanced")
    @ConfigEntry.Gui.Tooltip
    public boolean debugInfo = false;


    // ============================================================
    // CROSSHAIR MODES
    // ============================================================

    public enum CrosshairMode {
        SMART,
        PVP,
        BUILDING,
        ARCHER,
        MINIMAL,
        CUSTOM
    }
}