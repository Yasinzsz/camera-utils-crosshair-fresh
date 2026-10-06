package com.yasinz.crosshair;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import de.maxhenkel.camerautils.config.ClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import net.minecraft.core.BlockPos;

import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;


public final class CrosshairFeatureRenderer {

    private static Entity lastTarget;
    private static int acquiredTicks;
    private static int lostTicks;
    private static int hitTicks;

    private static boolean lastOnGround = true;

    private static long lastWorldFrame = -1L;


    /*
     * ============================================================
     * VANILLA MINECRAFT ATTACK INDICATOR SPRITES
     * ============================================================
     */

    private static final Identifier
            CROSSHAIR_ATTACK_INDICATOR_FULL =
            Identifier.withDefaultNamespace(
                    "hud/crosshair_attack_indicator_full"
            );

    private static final Identifier
            CROSSHAIR_ATTACK_INDICATOR_BACKGROUND =
            Identifier.withDefaultNamespace(
                    "hud/crosshair_attack_indicator_background"
            );

    private static final Identifier
            CROSSHAIR_ATTACK_INDICATOR_PROGRESS =
            Identifier.withDefaultNamespace(
                    "hud/crosshair_attack_indicator_progress"
            );


    private CrosshairFeatureRenderer() {
    }


    // ============================================================
    // HUD
    // ============================================================

    public static void renderHud(
            GuiGraphicsExtractor graphics,
            int centerX,
            int centerY,
            boolean thirdPerson
    ) {

        Minecraft mc =
                Minecraft.getInstance();

        LocalPlayer player =
                mc.player;

        CrosshairConfig config =
                CameraUtilsCrosshair.CONFIG;


        if (player == null || config == null) {
            return;
        }


        // ========================================================
        // TARGET
        // ========================================================

        Entity target =
                config.targetDetection
                        ? currentTarget(mc)
                        : null;

        boolean acquired =
                target != null;


        // ========================================================
        // TARGET ACQUISITION / LOSS
        // ========================================================

        if (target != lastTarget) {

            if (target != null) {

                acquiredTicks = 10;
                lostTicks = 0;

            } else if (lastTarget != null) {

                lostTicks = 8;
            }

            lastTarget = target;
        }


        if (acquiredTicks > 0) {
            acquiredTicks--;
        }

        if (lostTicks > 0) {
            lostTicks--;
        }


        // ========================================================
        // HIT FEEDBACK
        // ========================================================

        if (target instanceof LivingEntity living
                && living.hurtTime > 0) {

            hitTicks =
                    Math.max(
                            hitTicks,
                            4
                    );
        }


        if (hitTicks > 0) {
            hitTicks--;
        }


        // ========================================================
        // LANDING STABILIZATION
        // ========================================================

        if (!lastOnGround
                && player.onGround()
                && config.landingStabilization) {

            hitTicks =
                    Math.max(
                            hitTicks,
                            2
                    );
        }


        lastOnGround =
                player.onGround();


        // ========================================================
        // TARGET ACQUISITION ANIMATION
        // ========================================================

        if (config.targetAcquisitionAnimation
                && acquiredTicks > 0) {

            int size =
                    8
                            +
                            (10 - acquiredTicks) * 2;

            int alpha =
                    Math.min(
                            220,
                            acquiredTicks * 22
                    );

            drawCorners(
                    graphics,
                    centerX,
                    centerY,
                    size,
                    0x00FFFFFF | (alpha << 24)
            );
        }


        // ========================================================
        // TARGET LOST ANIMATION
        // ========================================================

        if (config.targetLostAnimation
                && lostTicks > 0) {

            int size =
                    8
                            +
                            lostTicks * 2;

            int alpha =
                    Math.min(
                            180,
                            lostTicks * 18
                    );

            drawCorners(
                    graphics,
                    centerX,
                    centerY,
                    size,
                    0x00FF5555 | (alpha << 24)
            );
        }


        // ========================================================
        // HIT FEEDBACK
        // ========================================================

        if (config.hitFeedback
                && hitTicks > 0) {

            int alpha =
                    hitTicks * 45;


            graphics.fill(
                    centerX - 12,
                    centerY - 1,
                    centerX - 7,
                    centerY + 1,
                    0x00FF5555 | (alpha << 24)
            );


            graphics.fill(
                    centerX + 7,
                    centerY - 1,
                    centerX + 12,
                    centerY + 1,
                    0x00FF5555 | (alpha << 24)
            );


            graphics.fill(
                    centerX - 1,
                    centerY - 12,
                    centerX + 1,
                    centerY - 7,
                    0x00FF5555 | (alpha << 24)
            );


            graphics.fill(
                    centerX - 1,
                    centerY + 7,
                    centerX + 1,
                    centerY + 12,
                    0x00FF5555 | (alpha << 24)
            );
        }


        // ========================================================
        // TARGET INDICATOR
        // ========================================================

        if (target != null) {

            double distance =
                    player.distanceTo(target);

            boolean inReach =
                    distance <= 3.0D;


            // ----------------------------------------------------
            // ENTITY CORNERS
            // ----------------------------------------------------

            if (config.entityIndicator) {

                int color =
                        inReach
                                ? 0xFF55FF77
                                : 0xFFFFA33D;

                drawCorners(
                        graphics,
                        centerX,
                        centerY,
                        11,
                        color
                );
            }


            // ----------------------------------------------------
            // OUT OF REACH
            // ----------------------------------------------------

            if (config.outOfReachIndicator
                    && !inReach) {

                graphics.text(
                        mc.font,
                        "OUT",
                        centerX + 13,
                        centerY - 4,
                        0xFFFFA33D,
                        true
                );
            }


            // ----------------------------------------------------
            // DISTANCE
            // ----------------------------------------------------

            if (config.targetDistance) {

                String distanceText =
                        String.format(
                                "%.1fm",
                                distance
                        );

                graphics.text(
                        mc.font,
                        distanceText,
                        centerX + 13,
                        centerY + 5,
                        inReach
                                ? 0xFFB8FFCA
                                : 0xFFFFA33D,
                        true
                );
            }
        }


        // ========================================================
        // DEBUG
        // ========================================================

        if (config.debugInfo) {

            String mode =
                    effectiveMode(
                            mc,
                            config
                    ).name();

            String targetName =
                    target == null
                            ? "-"
                            : target
                            .getName()
                            .getString();


            String debug =
                    "CU "
                            + mode
                            + " | target="
                            + targetName
                            + " | TP="
                            + thirdPerson
                            + " | cooldown="
                            + Math.round(
                            player
                                    .getAttackStrengthScale(
                                            0.0F
                                    )
                                    * 100.0F
                    )
                            + "%";


            graphics.text(
                    mc.font,
                    debug,
                    6,
                    6,
                    0xFFE0E0E0,
                    true
            );
        }
    }


    // ============================================================
    // VANILLA ATTACK INDICATOR
    // ============================================================

    /**
     * Renders Minecraft's native crosshair attack indicator.
     *
     * This is copied to match vanilla's 26.2 behavior:
     *
     * - Full attack indicator when fully charged and targeting
     *   a living entity with a sufficiently slow weapon.
     *
     * - 16x4 charging bar while the attack is charging.
     *
     * - Uses Minecraft's actual HUD sprites.
     *
     * - No custom colors.
     * - No custom fade.
     * - No fake rectangle.
     */
    public static void renderVanillaAttackIndicator(
            GuiGraphicsExtractor graphics,
            Minecraft mc
    ) {

        CrosshairConfig config =
                CameraUtilsCrosshair.CONFIG;


        if (config == null
                || !config.attackCooldown) {

            return;
        }


        LocalPlayer player =
                mc.player;


        if (player == null) {
            return;
        }


        /*
         * Minecraft's own Attack Indicator option.
         *
         * If the player selected OFF or HOTBAR,
         * don't force a crosshair indicator.
         */
        if (mc.options.attackIndicator().get()
                != net.minecraft.client.AttackIndicatorStatus.CROSSHAIR) {

            return;
        }


        float cooldown =
                player.getAttackStrengthScale(0.0F);


        /*
         * Exactly like vanilla:
         *
         * Full indicator is only shown when:
         *
         * 1. attack is fully charged
         * 2. crosshair is targeting a LivingEntity
         * 3. weapon attack delay is > 5
         * 4. target is alive
         */
        boolean fullIndicator =
                false;


        if (mc.hitResult instanceof EntityHitResult entityHit) {

            Entity entity =
                    entityHit.getEntity();


            if (entity instanceof LivingEntity living
                    && cooldown >= 1.0F) {

                fullIndicator =
                        player
                                .getCurrentItemAttackStrengthDelay()
                                > 5.0F;

                fullIndicator &=
                        living.isAlive();
            }
        }


        // ========================================================
        // VANILLA POSITION
        // ========================================================

        int x =
                graphics.guiWidth() / 2
                        - 8;


        int y =
                graphics.guiHeight() / 2
                        - 7
                        + 16;


        // ========================================================
        // FULL INDICATOR
        // ========================================================

        if (fullIndicator) {

            graphics.blitSprite(
                    RenderPipelines.CROSSHAIR,
                    CROSSHAIR_ATTACK_INDICATOR_FULL,
                    x,
                    y,
                    16,
                    16
            );

            return;
        }


        // ========================================================
        // CHARGING INDICATOR
        // ========================================================

        if (cooldown < 1.0F) {

            int progress =
                    (int) (
                            cooldown * 17.0F
                    );


            /*
             * Background
             */

            graphics.blitSprite(
                    RenderPipelines.CROSSHAIR,
                    CROSSHAIR_ATTACK_INDICATOR_BACKGROUND,
                    x,
                    y,
                    16,
                    4
            );


            /*
             * Progress
             */

            if (progress > 0) {

                graphics.blitSprite(
                        RenderPipelines.CROSSHAIR,
                        CROSSHAIR_ATTACK_INDICATOR_PROGRESS,
                        16,
                        4,
                        0,
                        0,
                        x,
                        y,
                        progress,
                        4
                );
            }
        }
    }


    // ============================================================
    // WORLD RENDERING
    // ============================================================

    public static void renderWorld() {

        Minecraft mc =
                Minecraft.getInstance();

        LocalPlayer player =
                mc.player;

        CrosshairConfig config =
                CameraUtilsCrosshair.CONFIG;


        if (player == null
                || mc.level == null
                || config == null) {

            return;
        }


        /*
         * Avoid duplicate submissions if the renderer
         * asks for the gizmo collection more than once.
         */

        long frame =
                System.nanoTime()
                        / 5_000_000L;


        if (frame == lastWorldFrame) {
            return;
        }


        lastWorldFrame =
                frame;


        Entity target =
                config.targetDetection
                        ? currentTarget(mc)
                        : null;


        // ========================================================
        // ENTITY WORLD INDICATOR
        // ========================================================

        if (target != null
                && config.entityIndicator) {

            boolean inReach =
                    player.distanceTo(target)
                            <= 3.0D;


            int color =
                    inReach
                            ? 0xFF55FF77
                            : 0xFFFFA33D;


            int fill =
                    inReach
                            ? 0x2055FF77
                            : 0x20FFA33D;


            Gizmos.cuboid(
                    target
                            .getBoundingBox()
                            .inflate(0.015D),

                    GizmoStyle.strokeAndFill(
                            color,
                            1.5F,
                            fill
                    )
            ).setAlwaysOnTop();
        }


        // ========================================================
        // PROJECTILE PREDICTION
        // ========================================================

        if (config.projectilePrediction
                && isProjectileItem(
                player.getMainHandItem()
        )) {

            renderProjectilePrediction(
                    mc,
                    player,
                    config
            );
        }
    }


    // ============================================================
    // PROJECTILE PREDICTION
    // ============================================================

    private static void renderProjectilePrediction(
            Minecraft mc,
            LocalPlayer player,
            CrosshairConfig config
    ) {

        Vec3 pos =
                player.getEyePosition(
                        1.0F
                );


        Vec3 direction =
                player.getViewVector(
                        1.0F
                ).normalize();


        double speed =
                projectileSpeed(
                        player.getMainHandItem()
                );


        double gravity =
                projectileGravity(
                        player.getMainHandItem()
                );


        Vec3 velocity =
                direction.scale(
                        speed
                );


        int dots =
                Math.max(
                        1,
                        Math.min(
                                10,
                                config.predictionDots
                        )
                );


        int steps =
                Math.max(
                        10,
                        Math.min(
                                100,
                                config.predictionLength
                        )
                );


        int spacing =
                Math.max(
                        1,
                        steps / dots
                );


        for (int i = 1; i <= steps; i++) {

            pos =
                    pos.add(
                            velocity
                    );


            velocity =
                    velocity.add(
                            0.0D,
                            -gravity,
                            0.0D
                    );


            if (i % spacing != 0
                    && i != steps) {

                continue;
            }


            AABB point =
                    new AABB(
                            pos.x - 0.045D,
                            pos.y - 0.045D,
                            pos.z - 0.045D,

                            pos.x + 0.045D,
                            pos.y + 0.045D,
                            pos.z + 0.045D
                    );


            int alpha =
                    Math.max(
                            35,
                            190
                                    -
                                    (
                                            i
                                                    * 140
                                                    / steps
                                    )
                    );


            Gizmos.cuboid(
                    point,

                    GizmoStyle.strokeAndFill(
                            0x00FFFFFF
                                    |
                                    (alpha << 24),

                            1.0F,

                            0x00000000
                                    |
                                    (
                                            Math.max(
                                                    18,
                                                    alpha / 4
                                            )
                                                    << 24
                                    )
                    )
            ).setAlwaysOnTop();
        }
    }


    // ============================================================
    // PROJECTILE ITEM DETECTION
    // ============================================================

    private static boolean isProjectileItem(
            ItemStack stack
    ) {

        String name =
                stack.getItem()
                        .getClass()
                        .getSimpleName()
                        .toLowerCase();


        return name.contains("bow")
                || name.contains("crossbow")
                || name.contains("trident")
                || name.contains("snowball")
                || name.contains("egg")
                || name.contains("enderpearl")
                || name.contains("experiencebottle")
                || name.contains("potion");
    }


    // ============================================================
    // PROJECTILE SPEED
    // ============================================================

    private static double projectileSpeed(
            ItemStack stack
    ) {

        String name =
                stack.getItem()
                        .getClass()
                        .getSimpleName()
                        .toLowerCase();


        if (name.contains("snowball")
                || name.contains("egg")
                || name.contains("enderpearl")
                || name.contains("experiencebottle")
                || name.contains("potion")) {

            return 0.72D;
        }


        if (name.contains("trident")) {
            return 2.4D;
        }


        if (name.contains("crossbow")) {
            return 3.15D;
        }


        return 2.8D;
    }


    // ============================================================
    // PROJECTILE GRAVITY
    // ============================================================

    private static double projectileGravity(
            ItemStack stack
    ) {

        String name =
                stack.getItem()
                        .getClass()
                        .getSimpleName()
                        .toLowerCase();


        if (name.contains("trident")) {
            return 0.05D;
        }


        if (name.contains("snowball")
                || name.contains("egg")
                || name.contains("enderpearl")
                || name.contains("experiencebottle")
                || name.contains("potion")) {

            return 0.03D;
        }


        return 0.05D;
    }


    // ============================================================
    // CURRENT TARGET
    // ============================================================

    private static Entity currentTarget(
            Minecraft mc
    ) {

        if (mc.hitResult
                instanceof EntityHitResult hit) {

            Entity entity =
                    hit.getEntity();


            if (entity != mc.player
                    && entity.isAlive()) {

                return entity;
            }
        }


        return null;
    }


    // ============================================================
    // EFFECTIVE MODE
    // ============================================================

    private static CrosshairConfig.CrosshairMode effectiveMode(
            Minecraft mc,
            CrosshairConfig config
    ) {

        if (config.mode
                != CrosshairConfig.CrosshairMode.SMART) {

            return config.mode;
        }


        if (currentTarget(mc) != null
                && config.targetDetection) {

            return CrosshairConfig.CrosshairMode.PVP;
        }


        if (CrosshairBuilding
                .getTarget(mc)
                .holdingBlock()
                && config.buildingMode) {

            return CrosshairConfig.CrosshairMode.BUILDING;
        }


        return CrosshairConfig.CrosshairMode.CUSTOM;
    }


    // ============================================================
    // CORNER RENDERER
    // ============================================================

    private static void drawCorners(
            GuiGraphicsExtractor graphics,
            int cx,
            int cy,
            int size,
            int color
    ) {

        int arm = 3;
        int t = 1;


        // --------------------------------------------------------
        // TOP LEFT
        // --------------------------------------------------------

        graphics.fill(
                cx - size,
                cy - size,
                cx - size + arm,
                cy - size + t,
                color
        );


        graphics.fill(
                cx - size,
                cy - size,
                cx - size + t,
                cy - size + arm,
                color
        );


        // --------------------------------------------------------
        // TOP RIGHT
        // --------------------------------------------------------

        graphics.fill(
                cx + size - arm,
                cy - size,
                cx + size,
                cy - size + t,
                color
        );


        graphics.fill(
                cx + size - t,
                cy - size,
                cx + size,
                cy - size + arm,
                color
        );


        // --------------------------------------------------------
        // BOTTOM LEFT
        // --------------------------------------------------------

        graphics.fill(
                cx - size,
                cy + size - t,
                cx - size + arm,
                cy + size,
                color
        );


        graphics.fill(
                cx - size,
                cy + size - arm,
                cx - size + t,
                cy + size,
                color
        );


        // --------------------------------------------------------
        // BOTTOM RIGHT
        // --------------------------------------------------------

        graphics.fill(
                cx + size - arm,
                cy + size - t,
                cx + size,
                cy + size,
                color
        );


        graphics.fill(
                cx + size - t,
                cy + size - arm,
                cx + size,
                cy + size,
                color
        );
    }
}