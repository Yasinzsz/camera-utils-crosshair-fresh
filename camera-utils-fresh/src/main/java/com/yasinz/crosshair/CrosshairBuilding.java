package com.yasinz.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class CrosshairBuilding {

    private CrosshairBuilding() {
    }

    public static BuildTarget getTarget(Minecraft mc) {

        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) {
            return BuildTarget.NONE;
        }

        HitResult hit = mc.hitResult;

        if (!(hit instanceof BlockHitResult blockHit)) {
            return BuildTarget.NONE;
        }

        BlockPos clickedBlock =
                blockHit.getBlockPos();

        Direction face =
                blockHit.getDirection();

        BlockPos placementPos =
                clickedBlock.relative(face);

        Vec3 hitLocation =
                blockHit.getLocation();

        ItemStack held =
                player.getMainHandItem();

        boolean holdingBlock =
                held.getItem() instanceof BlockItem;

        double distance =
                getDistance(
                        player,
                        placementPos
                );

        if (!holdingBlock) {

            return new BuildTarget(
                    clickedBlock,
                    placementPos,
                    face,
                    hitLocation,
                    false,
                    false,
                    false,
                    distance
            );
        }

        BlockItem blockItem =
                (BlockItem) held.getItem();

        BlockState placementState =
                blockItem
                        .getBlock()
                        .defaultBlockState();

        BlockState existingState =
                mc.level.getBlockState(
                        placementPos
                );

        boolean replaceable =
                existingState.canBeReplaced();

        boolean insideWorld =
                placementPos.getY()
                        >= mc.level.getMinY()
                        &&
                        placementPos.getY()
                                < mc.level.getMaxY();

        boolean survives =
                placementState.canSurvive(
                        mc.level,
                        placementPos
                );

        boolean collisionFree =
                !mc.level
                        .getBlockCollisions(
                                player,
                                placementState
                                        .getShape(
                                                mc.level,
                                                placementPos
                                        )
                                        .bounds()
                                        .move(
                                                placementPos.getX(),
                                                placementPos.getY(),
                                                placementPos.getZ()
                                        )
                        )
                        .iterator()
                        .hasNext();

        boolean valid =
                replaceable
                        && insideWorld
                        && survives
                        && collisionFree;

        /*
         * Minecraft's interaction range is slightly different
         * depending on the game mode.
         *
         * Keep this separate so the UI can distinguish:
         *
         * valid placement
         * from
         * valid but too far away.
         */
        double reach =
                player.isCreative()
                        ? 5.0
                        : 4.5;

        boolean inReach =
                distance <= reach;

        return new BuildTarget(
                clickedBlock,
                placementPos,
                face,
                hitLocation,
                true,
                valid,
                inReach,
                distance
        );
    }

    private static double getDistance(
            LocalPlayer player,
            BlockPos pos
    ) {

        return player
                .getEyePosition(1.0F)
                .distanceTo(
                        Vec3.atCenterOf(pos)
                );
    }

    public record BuildTarget(
            BlockPos clickedBlock,
            BlockPos placementPos,
            Direction face,
            Vec3 hitLocation,
            boolean holdingBlock,
            boolean valid,
            boolean inReach,
            double distance
    ) {

        public static final BuildTarget NONE =
                new BuildTarget(
                        BlockPos.ZERO,
                        BlockPos.ZERO,
                        Direction.UP,
                        Vec3.ZERO,
                        false,
                        false,
                        false,
                        Double.MAX_VALUE
                );
    }
}