package com.yasinz.crosshair;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AimAssist {

    private static Entity currentTarget;

    private AimAssist() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(AimAssist::tick);
    }

    private static void tick(Minecraft mc) {
        CrosshairConfig config = CameraUtilsCrosshair.CONFIG;

        if (config == null || !config.aimAssist) {
            currentTarget = null;
            return;
        }

        LocalPlayer player = mc.player;

        if (player == null || mc.level == null || player.isSpectator()) {
            currentTarget = null;
            return;
        }

        // First Person OFF = third person only.
        // First Person ON = both first and third person.
        if (!config.aimAssistFirstPerson
                && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
            currentTarget = null;
            return;
        }

        Entity target = findBestTarget(mc, player, config);

        if (target == null) {
            currentTarget = null;
            return;
        }

        currentTarget = target;
        applyAimAssist(player, target, config);
    }

    private static Entity findBestTarget(
            Minecraft mc,
            LocalPlayer player,
            CrosshairConfig config
    ) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 view = player.getViewVector(1.0F).normalize();

        double range = config.aimAssistRange;
        double maxAngle = config.aimAssistAngle;

        Entity best = null;
        double bestScore = Double.MAX_VALUE;

        AABB box = player.getBoundingBox().inflate(range);

        for (Entity entity : mc.level.getEntities(
                player,
                box,
                entity -> isValidTarget(entity, player, config)
        )) {

            Vec3 targetPos = getTargetPosition(entity);
            Vec3 toTarget = targetPos.subtract(eye);

            double distance = toTarget.length();

            if (distance <= 0.001D || distance > range) {
                continue;
            }

            Vec3 direction = toTarget.normalize();

            double dot = view.dot(direction);
            dot = Math.max(-1.0D, Math.min(1.0D, dot));

            double angle = Math.toDegrees(Math.acos(dot));

            if (angle > maxAngle) {
                continue;
            }

            if (!hasLineOfSight(player, targetPos)) {
                continue;
            }

            double score = angle * 5.0D + distance * 0.12D;

            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }

        return best;
    }

    private static boolean isValidTarget(
            Entity entity,
            LocalPlayer player,
            CrosshairConfig config
    ) {
        if (entity == player) {
            return false;
        }

        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        if (!living.isAlive() || living.isSpectator()) {
            return false;
        }

        if (entity instanceof Player) {
            return config.aimAssistPlayers;
        }

        return config.aimAssistMobs;
    }

    private static Vec3 getTargetPosition(Entity entity) {
        AABB box = entity.getBoundingBox();

        return new Vec3(
                (box.minX + box.maxX) * 0.5D,
                box.minY + box.getYsize() * 0.72D,
                (box.minZ + box.maxZ) * 0.5D
        );
    }

    private static boolean hasLineOfSight(
            LocalPlayer player,
            Vec3 targetPosition
    ) {
        Vec3 start = player.getEyePosition(1.0F);

        var result = player.level().clip(
                new net.minecraft.world.level.ClipContext(
                        start,
                        targetPosition,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,
                        player
                )
        );

        return result.getType()
                == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    private static void applyAimAssist(
            LocalPlayer player,
            Entity target,
            CrosshairConfig config
    ) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 targetPos = getTargetPosition(target);
        Vec3 delta = targetPos.subtract(eye);

        double horizontalDistance =
                Math.sqrt(delta.x * delta.x + delta.z * delta.z);

        if (horizontalDistance < 0.001D) {
            return;
        }

        float desiredYaw = (float) Math.toDegrees(
                Math.atan2(-delta.x, delta.z)
        );

        float desiredPitch = (float) -Math.toDegrees(
                Math.atan2(delta.y, horizontalDistance)
        );

        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();

        float yawDifference =
                wrapDegrees(desiredYaw - currentYaw);

        float pitchDifference =
                wrapDegrees(desiredPitch - currentPitch);

        float strength = Math.max(
                0.01F,
                Math.min(
                        1.0F,
                        config.aimAssistStrength / 100.0F
                )
        );

        double distance = player.distanceTo(target);

        float distanceFactor = (float) Math.max(
                0.35D,
                1.0D -
                        (distance / config.aimAssistRange) * 0.45D
        );

        double totalAngle = Math.sqrt(
                yawDifference * yawDifference
                        + pitchDifference * pitchDifference
        );

        float angleFactor = (float) Math.max(
                0.15D,
                1.0D -
                        totalAngle / config.aimAssistAngle
        );

        float finalStrength =
                strength * distanceFactor * angleFactor;

        float yawStep =
                yawDifference * finalStrength * 0.16F;

        float pitchStep =
                pitchDifference * finalStrength * 0.16F;

        yawStep = clamp(yawStep, -2.5F, 2.5F);
        pitchStep = clamp(pitchStep, -2.0F, 2.0F);

        player.setYRot(currentYaw + yawStep);

        player.setXRot(
                clamp(
                        currentPitch + pitchStep,
                        -90.0F,
                        90.0F
                )
        );

        player.setYHeadRot(player.getYRot());
    }

    private static float wrapDegrees(float angle) {
        angle %= 360.0F;

        if (angle >= 180.0F) {
            angle -= 360.0F;
        }

        if (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    private static float clamp(
            float value,
            float min,
            float max
    ) {
        return Math.max(min, Math.min(max, value));
    }

    public static Entity getCurrentTarget() {
        return currentTarget;
    }
}