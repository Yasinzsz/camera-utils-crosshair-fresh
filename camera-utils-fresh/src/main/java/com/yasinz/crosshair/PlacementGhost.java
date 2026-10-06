package com.yasinz.crosshair;

import de.maxhenkel.camerautils.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public final class PlacementGhost {

    private PlacementGhost() {
    }

    public static boolean shouldRender() {

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null) {
            return false;
        }

        CrosshairConfig config =
                CameraUtilsCrosshair.CONFIG;

        if (config == null ||
                !config.buildingMode ||
                !config.placementGhost) {
            return false;
        }

        boolean firstPerson =
                ClientConfig.thirdPersonCam < 0;

        if (firstPerson &&
                !config.placementGhostFirstPerson) {
            return false;
        }

        if (!(player.getMainHandItem().getItem()
                instanceof BlockItem)) {
            return false;
        }

        if (mc.hitResult == null ||
                mc.hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        CrosshairBuilding.BuildTarget target =
                CrosshairBuilding.getTarget(mc);

        // Keep the ghost visible for invalid/out-of-reach placements too.
        // The placement indicator underneath it communicates the exact state.
        return target.holdingBlock();
    }

    public static BlockState getBlockState() {

        Minecraft mc = Minecraft.getInstance();

        ItemStack held =
                mc.player.getMainHandItem();

        if (!(held.getItem() instanceof BlockItem blockItem)) {
            return null;
        }

        return blockItem
                .getBlock()
                .defaultBlockState();
    }

    public static BlockPos getPosition() {

        Minecraft mc = Minecraft.getInstance();

        return CrosshairBuilding
                .getTarget(mc)
                .placementPos();
    }
}