package com.yasinz.crosshair;

import de.maxhenkel.camerautils.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class CrosshairBuildingRenderer {

    private CrosshairBuildingRenderer() {
    }

    public static void render() {

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) {
            return;
        }

        CrosshairConfig config =
                CameraUtilsCrosshair.CONFIG;

        if (config == null
                || !config.buildingMode) {
            return;
        }

        /*
         * =========================================================
         * FIRST / THIRD PERSON
         * =========================================================
         */

        boolean firstPerson =
                ClientConfig.thirdPersonCam < 0;

        /*
         * =========================================================
         * BLOCK HIT ONLY
         * =========================================================
         */

        HitResult hit =
                mc.hitResult;

        if (hit == null ||
                hit.getType() != HitResult.Type.BLOCK ||
                !(hit instanceof BlockHitResult blockHit)) {
            return;
        }

        /*
         * =========================================================
         * FACE INDICATOR
         * =========================================================
         */

        if (config.blockFaceIndicator &&
                (!firstPerson ||
                        config.blockFaceIndicatorFirstPerson)) {

            renderBlockFace(blockHit);
        }

        /*
         * =========================================================
         * PLACEMENT INDICATOR
         * =========================================================
         */

        if (!config.placementIndicator) {
            return;
        }

        if (firstPerson &&
                !config.placementIndicatorFirstPerson) {
            return;
        }

        /*
         * =========================================================
         * HOLDING BLOCK
         * =========================================================
         */

        ItemStack held =
                player.getMainHandItem();

        if (!(held.getItem() instanceof BlockItem)) {
            return;
        }

        CrosshairBuilding.BuildTarget target =
                CrosshairBuilding.getTarget(mc);

        if (!target.holdingBlock()) {
            return;
        }

        BlockPos pos =
                target.placementPos();

        /*
         * =========================================================
         * PLACEMENT BOX
         * =========================================================
         */

        double inset = 0.015D;

        if (config.placementAnimation) {
            double pulse =
                    (Math.sin(
                            System.nanoTime() / 120_000_000.0D
                    ) + 1.0D) * 0.004D;

            inset += pulse;
        }

        double minX = pos.getX() + inset;
        double minY = pos.getY() + inset;
        double minZ = pos.getZ() + inset;

        double maxX = pos.getX() + 1.0D - inset;
        double maxY = pos.getY() + 1.0D - inset;
        double maxZ = pos.getZ() + 1.0D - inset;

        /*
         * =========================================================
         * VERY TRANSPARENT WHITE FILL
         * =========================================================
         *
         * 0x14FFFFFF = extremely transparent white.
         */
        AABB box =
                new AABB(
                        minX,
                        minY,
                        minZ,
                        maxX,
                        maxY,
                        maxZ
                );

        GizmoStyle fillStyle =
                GizmoStyle.strokeAndFill(
                        0x00FFFFFF,
                        1.0F,
                        0x14FFFFFF
                );

        Gizmos.cuboid(
                box,
                fillStyle
        ).setAlwaysOnTop();

        /*
         * =========================================================
         * DASHED WHITE OUTLINE
         * =========================================================
         */

        renderDashedBox(
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ
        );
    }

    /*
     * =============================================================
     * DASHED BOX
     * =============================================================
     */

    private static void renderDashedBox(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {

        /*
         * Thin white segments.
         *
         * segmentLength = length of each dash
         * gapLength     = space between dashes
         */
        double segmentLength = 0.12D;
        double gapLength = 0.08D;

        /*
         * Bottom
         */
        dashedEdge(
                minX, minY, minZ,
                maxX, minY, minZ,
                segmentLength,
                gapLength
        );

        dashedEdge(
                minX, minY, maxZ,
                maxX, minY, maxZ,
                segmentLength,
                gapLength
        );

        dashedEdge(
                minX, minY, minZ,
                minX, minY, maxZ,
                segmentLength,
                gapLength
        );

        dashedEdge(
                maxX, minY, minZ,
                maxX, minY, maxZ,
                segmentLength,
                gapLength
        );

        /*
         * Top
         */
        dashedEdge(
                minX, maxY, minZ,
                maxX, maxY, minZ,
                segmentLength,
                gapLength
        );

        dashedEdge(
                minX, maxY, maxZ,
                maxX, maxY, maxZ,
                segmentLength,
                gapLength
        );

        dashedEdge(
                minX, maxY, minZ,
                minX, maxY, maxZ,
                segmentLength,
                gapLength
        );

        dashedEdge(
                maxX, maxY, minZ,
                maxX, maxY, maxZ,
                segmentLength,
                gapLength
        );

        /*
         * Vertical
         */
        dashedEdge(
                minX, minY, minZ,
                minX, maxY, minZ,
                segmentLength,
                gapLength
        );

        dashedEdge(
                maxX, minY, minZ,
                maxX, maxY, minZ,
                segmentLength,
                gapLength
        );

        dashedEdge(
                minX, minY, maxZ,
                minX, maxY, maxZ,
                segmentLength,
                gapLength
        );

        dashedEdge(
                maxX, minY, maxZ,
                maxX, maxY, maxZ,
                segmentLength,
                gapLength
        );
    }

    /*
     * =============================================================
     * DASHED EDGE
     * =============================================================
     */

    private static void dashedEdge(
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2,
            double segmentLength,
            double gapLength
    ) {

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        double length =
                Math.sqrt(
                        dx * dx +
                                dy * dy +
                                dz * dz
                );

        if (length <= 0.0001D) {
            return;
        }

        double dirX = dx / length;
        double dirY = dy / length;
        double dirZ = dz / length;

        double position = 0.0D;

        while (position < length) {

            double dashStart = position;

            double dashEnd =
                    Math.min(
                            position + segmentLength,
                            length
                    );

            double sx =
                    x1 + dirX * dashStart;

            double sy =
                    y1 + dirY * dashStart;

            double sz =
                    z1 + dirZ * dashStart;

            double ex =
                    x1 + dirX * dashEnd;

            double ey =
                    y1 + dirY * dashEnd;

            double ez =
                    z1 + dirZ * dashEnd;

            /*
             * Slight thickness so the dashes are clearly visible.
             */
            double thickness = 0.018D;

            AABB dash =
                    new AABB(
                            Math.min(sx, ex) - thickness,
                            Math.min(sy, ey) - thickness,
                            Math.min(sz, ez) - thickness,

                            Math.max(sx, ex) + thickness,
                            Math.max(sy, ey) + thickness,
                            Math.max(sz, ez) + thickness
                    );

            GizmoStyle dashStyle =
                    GizmoStyle.strokeAndFill(
                            0xFFFFFFFF,
                            1.0F,
                            0x00000000
                    );

            Gizmos.cuboid(
                    dash,
                    dashStyle
            ).setAlwaysOnTop();

            position +=
                    segmentLength + gapLength;
        }
    }

    /*
     * =========================================================
     * BLOCK FACE
     * =========================================================
     */

    private static void renderBlockFace(
            BlockHitResult hit
    ) {

        BlockPos pos =
                hit.getBlockPos();

        Direction face =
                hit.getDirection();

        double offset = 0.002D;

        double minX = pos.getX();
        double minY = pos.getY();
        double minZ = pos.getZ();

        double maxX = pos.getX() + 1.0D;
        double maxY = pos.getY() + 1.0D;
        double maxZ = pos.getZ() + 1.0D;

        AABB faceBox;

        switch (face) {

            case DOWN -> {

                faceBox =
                        new AABB(
                                minX + 0.02D,
                                minY - offset,
                                minZ + 0.02D,
                                maxX - 0.02D,
                                minY + 0.02D,
                                maxZ - 0.02D
                        );
            }

            case UP -> {

                faceBox =
                        new AABB(
                                minX + 0.02D,
                                maxY - 0.02D,
                                minZ + 0.02D,
                                maxX - 0.02D,
                                maxY + offset,
                                maxZ - 0.02D
                        );
            }

            case NORTH -> {

                faceBox =
                        new AABB(
                                minX + 0.02D,
                                minY + 0.02D,
                                minZ - offset,
                                maxX - 0.02D,
                                maxY - 0.02D,
                                minZ + 0.02D
                        );
            }

            case SOUTH -> {

                faceBox =
                        new AABB(
                                minX + 0.02D,
                                minY + 0.02D,
                                maxZ - 0.02D,
                                maxX - 0.02D,
                                maxY - 0.02D,
                                maxZ + offset
                        );
            }

            case WEST -> {

                faceBox =
                        new AABB(
                                minX - offset,
                                minY + 0.02D,
                                minZ + 0.02D,
                                minX + 0.02D,
                                maxY - 0.02D,
                                maxZ - 0.02D
                        );
            }

            case EAST -> {

                faceBox =
                        new AABB(
                                maxX - 0.02D,
                                minY + 0.02D,
                                minZ + 0.02D,
                                maxX + offset,
                                maxY - 0.02D,
                                maxZ - 0.02D
                        );
            }

            default -> {
                return;
            }
        }

        /*
         * Keep the block-face indicator unchanged.
         */
        GizmoStyle faceStyle =
                GizmoStyle.strokeAndFill(
                        0xFF35D9FF,
                        2.5F,
                        0x3035D9FF
                );

        Gizmos.cuboid(
                faceBox,
                faceStyle
        ).setAlwaysOnTop();
    }
}