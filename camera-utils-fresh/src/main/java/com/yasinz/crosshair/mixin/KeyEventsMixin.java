package com.yasinz.crosshair.mixin;

import com.yasinz.crosshair.CameraUtilsState;
import de.maxhenkel.camerautils.KeyEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyEvents.class)
public class KeyEventsMixin {

    @Inject(
            method = "onShoulderCam",
            at = @At("TAIL")
    )
    private static void cameraUtilsCrosshair$shoulderCamChanged(
            int camera,
            CallbackInfo ci
    ) {
        CameraUtilsState.crosshairActive = true;

        System.out.println(
                "CAMERA UTILS F6 | shoulderCam=" + camera
        );
    }
}