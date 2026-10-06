package com.yasinz.crosshair.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.yasinz.crosshair.CameraUtilsCrosshair;
import com.yasinz.crosshair.CrosshairBuilding;
import com.yasinz.crosshair.CrosshairConfig;
import com.yasinz.crosshair.CrosshairFeatureRenderer;

import de.maxhenkel.camerautils.config.ClientConfig;
import mod.crend.dynamiccrosshair.render.CrosshairRenderer;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public class HudMixin {

    private static double positionX = Double.NaN;
    private static double positionY = Double.NaN;

    private static double velocityX = 0.0;
    private static double velocityY = 0.0;

    private static double previousTargetX = Double.NaN;
    private static double previousTargetY = Double.NaN;

    private static double stableTargetX = Double.NaN;
    private static double stableTargetY = Double.NaN;

    private static double trackedX = Double.NaN;
    private static double trackedY = Double.NaN;
    private static double trackedZ = Double.NaN;

    private static double targetVelocityX = 0.0;
    private static double targetVelocityY = 0.0;
    private static double targetVelocityZ = 0.0;

    private static Entity trackedEntity = null;

    private static boolean wasOnGround = true;

    @WrapOperation(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Hud;extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
            )
    )
    private void cameraUtilsCrosshair$renderThirdPerson(
            Hud hud,
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker,
            Operation<Void> original
    ) {

        CrosshairConfig config =
                CameraUtilsCrosshair.CONFIG;

        /*
         * If the mod crosshair is disabled,
         * keep completely vanilla behavior.
         */
        if (config == null ||
                !config.showCrosshairInThirdPerson) {

            original.call(
                    hud,
                    graphics,
                    deltaTracker
            );

            return;
        }

        /*
         * First person:
         *
         * Let Minecraft render its normal crosshair,
         * including its native attack indicator.
         */
        if (ClientConfig.thirdPersonCam < 0) {

            reset();

            original.call(
                    hud,
                    graphics,
                    deltaTracker
            );

            return;
        }

        Minecraft mc =
                Minecraft.getInstance();

        LocalPlayer player =
                mc.player;

        if (player == null) {
            return;
        }

        Camera camera =
                mc.gameRenderer.mainCamera();

        if (camera == null) {
            return;
        }

        CrosshairConfig.CrosshairMode mode =
                config.mode;

        HitResult hitResult =
                mc.hitResult;

        boolean lookingAtEntity =
                hitResult != null &&
                        hitResult.getType()
                                == HitResult.Type.ENTITY;

        boolean lookingAtBlock =
                hitResult != null &&
                        hitResult.getType()
                                == HitResult.Type.BLOCK;

        CrosshairBuilding.BuildTarget buildTarget =
                CrosshairBuilding.getTarget(mc);

        boolean isFirstPerson =
                ClientConfig.thirdPersonCam < 0;

        boolean buildingIndicator =
                config.buildingMode
                        && config.placementIndicator
                        && buildTarget.holdingBlock()
                        && lookingAtBlock
                        && (
                        !isFirstPerson
                                || config.placementIndicatorFirstPerson
                );

        if (mode ==
                CrosshairConfig.CrosshairMode.SMART) {

            if (lookingAtEntity &&
                    config.targetDetection) {

                mode =
                        CrosshairConfig.CrosshairMode.PVP;

            } else if (lookingAtBlock &&
                    config.buildingMode) {

                mode =
                        CrosshairConfig.CrosshairMode.BUILDING;

            } else {

                mode =
                        CrosshairConfig.CrosshairMode.CUSTOM;
            }
        }

        Vec3 aimPoint;

        Entity currentEntity = null;

        if (lookingAtEntity &&
                hitResult instanceof EntityHitResult entityHit) {

            currentEntity =
                    entityHit.getEntity();
        }

        boolean trackingAllowed =
                config.targetDetection &&
                        (
                                mode ==
                                        CrosshairConfig.CrosshairMode.PVP
                                        ||
                                        mode ==
                                                CrosshairConfig.CrosshairMode.SMART
                                        ||
                                        mode ==
                                                CrosshairConfig.CrosshairMode.CUSTOM
                        );

        if (currentEntity == player) {
            currentEntity = null;
        }

        if (currentEntity != null &&
                trackingAllowed) {

            if (trackedEntity != currentEntity ||
                    Double.isNaN(trackedX)) {

                trackedEntity =
                        currentEntity;

                Vec3 center =
                        getEntityAimPoint(
                                currentEntity
                        );

                trackedX =
                        center.x;

                trackedY =
                        center.y;

                trackedZ =
                        center.z;

                targetVelocityX = 0.0;
                targetVelocityY = 0.0;
                targetVelocityZ = 0.0;
            }

            Vec3 entityCenter =
                    getEntityAimPoint(
                            currentEntity
                    );

            double movementX =
                    entityCenter.x -
                            trackedX;

            double movementY =
                    entityCenter.y -
                            trackedY;

            double movementZ =
                    entityCenter.z -
                            trackedZ;

            targetVelocityX =
                    targetVelocityX * 0.82
                            +
                            movementX * 0.18;

            targetVelocityY =
                    targetVelocityY * 0.82
                            +
                            movementY * 0.18;

            targetVelocityZ =
                    targetVelocityZ * 0.82
                            +
                            movementZ * 0.18;

            double tracking;

            switch (mode) {

                case PVP ->
                        tracking = 0.12;

                case CUSTOM ->
                        tracking =
                                0.08
                                        +
                                        (
                                                config.aimAnticipation
                                                        / 100.0
                                        ) * 0.12;

                default ->
                        tracking = 0.10;
            }

            double prediction;

            switch (mode) {

                case PVP ->
                        prediction = 0.18;

                case CUSTOM ->
                        prediction =
                                (
                                        config.aimAnticipation
                                                / 100.0
                                ) * 0.20;

                default ->
                        prediction = 0.08;
            }

            Vec3 predicted =
                    new Vec3(
                            entityCenter.x
                                    +
                                    targetVelocityX
                                            * prediction,

                            entityCenter.y
                                    +
                                    targetVelocityY
                                            * prediction,

                            entityCenter.z
                                    +
                                    targetVelocityZ
                                            * prediction
                    );

            trackedX +=
                    (
                            predicted.x -
                                    trackedX
                    ) * tracking;

            trackedY +=
                    (
                            predicted.y -
                                    trackedY
                    ) * tracking;

            trackedZ +=
                    (
                            predicted.z -
                                    trackedZ
                    ) * tracking;

            aimPoint =
                    new Vec3(
                            trackedX,
                            trackedY,
                            trackedZ
                    );

        } else {

            trackedEntity = null;

            targetVelocityX *= 0.90;
            targetVelocityY *= 0.90;
            targetVelocityZ *= 0.90;

            if (hitResult != null) {

                aimPoint =
                        hitResult.getLocation();

            } else {

                aimPoint =
                        player.getEyePosition(1.0F)
                                .add(
                                        player
                                                .getViewVector(1.0F)
                                                .scale(128.0)
                                );
            }
        }

        if (buildingIndicator) {

            Vec3 placementCenter =
                    Vec3.atCenterOf(
                            buildTarget.placementPos()
                    );

            double placementInfluence =
                    0.12;

            aimPoint =
                    aimPoint.lerp(
                            placementCenter,
                            placementInfluence
                    );
        }

        Vec3 relative =
                aimPoint.subtract(
                        camera.position()
                );

        Quaternionf rotation =
                new Quaternionf(
                        camera.rotation()
                );

        Vector3f right =
                new Vector3f(1, 0, 0)
                        .rotate(rotation);

        Vector3f up =
                new Vector3f(0, 1, 0)
                        .rotate(rotation);

        Vector3f forward =
                new Vector3f(0, 0, -1)
                        .rotate(rotation);

        double cameraX =
                relative.x * right.x()
                        +
                        relative.y * right.y()
                        +
                        relative.z * right.z();

        double cameraY =
                relative.x * up.x()
                        +
                        relative.y * up.y()
                        +
                        relative.z * up.z();

        double cameraZ =
                relative.x * forward.x()
                        +
                        relative.y * forward.y()
                        +
                        relative.z * forward.z();

        if (cameraZ <= 0.001) {
            return;
        }

        int width =
                graphics.guiWidth();

        int height =
                graphics.guiHeight();

        double fov =
                Math.toRadians(
                        mc.options.fov().get()
                );

        double aspect =
                (double) width /
                        (double) height;

        double tanHalfFov =
                Math.tan(fov * 0.5);

        double ndcX =
                cameraX /
                        (
                                cameraZ
                                        * tanHalfFov
                                        * aspect
                        );

        double ndcY =
                cameraY /
                        (
                                cameraZ
                                        * tanHalfFov
                        );

        double rawTargetX =
                (ndcX + 1.0)
                        * 0.5
                        * width;

        double rawTargetY =
                (1.0 - ndcY)
                        * 0.5
                        * height;

        double smoothAmount;
        double stabilization;
        double anticipation;

        switch (mode) {

            case PVP -> {

                smoothAmount = 0.24;
                stabilization = 0.15;
                anticipation = 0.65;
            }

            case BUILDING -> {

                smoothAmount = 0.14;
                stabilization = 0.75;
                anticipation = 0.10;
            }

            case ARCHER -> {

                smoothAmount = 0.18;
                stabilization = 0.45;
                anticipation = 0.45;
            }

            case MINIMAL -> {

                smoothAmount = 0.10;
                stabilization = 0.90;
                anticipation = 0.0;
            }

            case CUSTOM, SMART -> {

                smoothAmount =
                        config.smoothness / 100.0;

                stabilization =
                        config.movementStabilization
                                / 100.0;

                anticipation =
                        config.aimAnticipation
                                / 100.0;
            }

            default -> {

                smoothAmount = 0.18;
                stabilization = 0.50;
                anticipation = 0.20;
            }
        }

        smoothAmount =
                Math.max(
                        0.03,
                        Math.min(
                                0.6,
                                smoothAmount
                        )
                );

        double deadZone;

        if (mode ==
                CrosshairConfig.CrosshairMode.MINIMAL) {

            deadZone = 0.75;

        } else {

            deadZone =
                    config.deadZone / 100.0;
        }

        if (Double.isNaN(stableTargetX)) {

            stableTargetX =
                    rawTargetX;

            stableTargetY =
                    rawTargetY;

        } else {

            double dx =
                    rawTargetX -
                            stableTargetX;

            double dy =
                    rawTargetY -
                            stableTargetY;

            double deadZoneMultiplier =
                    1.0 -
                            Math.min(
                                    0.85,
                                    stabilization * 0.35
                            );

            if (Math.abs(dx) > deadZone) {

                stableTargetX +=
                        dx *
                                smoothAmount *
                                deadZoneMultiplier;
            }

            if (Math.abs(dy) > deadZone) {

                stableTargetY +=
                        dy *
                                smoothAmount *
                                deadZoneMultiplier;
            }
        }

        double targetX =
                stableTargetX;

        double targetY =
                stableTargetY;

        double screenVelocityX = 0.0;
        double screenVelocityY = 0.0;

        if (!Double.isNaN(previousTargetX)) {

            screenVelocityX =
                    targetX -
                            previousTargetX;

            screenVelocityY =
                    targetY -
                            previousTargetY;
        }

        previousTargetX =
                targetX;

        previousTargetY =
                targetY;

        if (config.directionalAnticipation &&
                anticipation > 0) {

            targetX +=
                    screenVelocityX
                            * anticipation
                            * 0.35;

            targetY +=
                    screenVelocityY
                            * anticipation
                            * 0.35;
        }

        boolean onGround =
                player.onGround();

        if (!onGround &&
                config.landingStabilization &&
                !Double.isNaN(positionY)) {

            targetY =
                    targetY *
                            (
                                    1.0 -
                                            stabilization * 0.35
                            )
                            +
                            positionY *
                                    (
                                            stabilization * 0.35
                                    );
        }

        wasOnGround =
                onGround;

        if (Double.isNaN(positionX)) {

            positionX =
                    targetX;

            positionY =
                    targetY;

            velocityX = 0.0;
            velocityY = 0.0;

        } else {

            double strength =
                    Math.max(
                            0.0,
                            Math.min(
                                    1.0,
                                    config.springStrength / 100.0
                            )
                    );

            double dampingControl =
                    Math.max(
                            0.0,
                            Math.min(
                                    1.0,
                                    config.springDamping / 100.0
                            )
                    );

            double stiffness =
                    config.crosshairSpring
                            ? 55.0 + strength * 105.0
                            : 130.0 + strength * 90.0;

            double damping =
                    config.crosshairSpring
                            ? 12.0 + dampingControl * 42.0
                            : 24.0 + dampingControl * 42.0;

            if (mode ==
                    CrosshairConfig.CrosshairMode.PVP) {

                stiffness *= 1.10;
                damping *= 1.20;
            }

            if (mode ==
                    CrosshairConfig.CrosshairMode.BUILDING) {

                stiffness *= 0.85;
                damping *= 1.30;
            }

            double dx =
                    targetX -
                            positionX;

            double dy =
                    targetY -
                            positionY;

            velocityX +=
                    dx *
                            stiffness *
                            0.016;

            velocityY +=
                    dy *
                            stiffness *
                            0.016;

            velocityX *=
                    Math.exp(
                            -damping *
                                    0.016
                    );

            velocityY *=
                    Math.exp(
                            -damping *
                                    0.016
                    );

            double maxVelocity =
                    mode ==
                            CrosshairConfig.CrosshairMode.PVP
                            ? 18.0
                            : 14.0;

            velocityX =
                    Math.max(
                            -maxVelocity,
                            Math.min(
                                    maxVelocity,
                                    velocityX
                            )
                    );

            velocityY =
                    Math.max(
                            -maxVelocity,
                            Math.min(
                                    maxVelocity,
                                    velocityY
                            )
                    );

            positionX +=
                    velocityX;

            positionY +=
                    velocityY;

            if (Math.abs(targetX - positionX) < 0.05 &&
                    Math.abs(velocityX) < 0.05) {

                positionX =
                        targetX;

                velocityX = 0.0;
            }

            if (Math.abs(targetY - positionY) < 0.05 &&
                    Math.abs(velocityY) < 0.05) {

                positionY =
                        targetY;

                velocityY = 0.0;
            }
        }

        positionX =
                Math.max(
                        -100,
                        Math.min(
                                width + 100,
                                positionX
                        )
                );

        positionY =
                Math.max(
                        -100,
                        Math.min(
                                height + 100,
                                positionY
                        )
                );

        int x =
                (int) Math.round(
                        positionX - 7.5
                );

        int y =
                (int) Math.round(
                        positionY - 7.5
                );

        /*
         * Dynamic Crosshair
         */
        CrosshairRenderer.render(
                graphics,
                x,
                y
        );

        /*
         * Our other crosshair features.
         */
        CrosshairFeatureRenderer.renderHud(
                graphics,
                (int) Math.round(positionX),
                (int) Math.round(positionY),
                true
        );

        /*
         * Vanilla-style attack indicator.
         *
         * It is centered on the screen rather than attached
         * to the moving third-person crosshair, matching
         * Minecraft's native positioning.
         */
        CrosshairFeatureRenderer.renderVanillaAttackIndicator(
                graphics,
                mc
        );
    }

    private static Vec3 getEntityAimPoint(
            Entity entity
    ) {

        return entity
                .getBoundingBox()
                .getCenter();
    }

    private static void reset() {

        positionX = Double.NaN;
        positionY = Double.NaN;

        velocityX = 0.0;
        velocityY = 0.0;

        previousTargetX = Double.NaN;
        previousTargetY = Double.NaN;

        stableTargetX = Double.NaN;
        stableTargetY = Double.NaN;

        trackedX = Double.NaN;
        trackedY = Double.NaN;
        trackedZ = Double.NaN;

        targetVelocityX = 0.0;
        targetVelocityY = 0.0;
        targetVelocityZ = 0.0;

        trackedEntity = null;

        wasOnGround = true;
    }
}