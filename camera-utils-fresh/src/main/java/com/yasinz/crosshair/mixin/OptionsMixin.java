package com.yasinz.crosshair.mixin;

import de.maxhenkel.camerautils.config.ClientConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(value = net.minecraft.client.gui.Hud.class, priority = 1200)
public class OptionsMixin {

    @ModifyExpressionValue(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Options;getCameraType()Lnet/minecraft/client/CameraType;"
            )
    )
    private CameraType cameraUtilsCrosshair$cameraType(CameraType original) {
        System.out.println("OUR MIXIN | cameraType=" + original
                + " | shoulderCam=" + ClientConfig.thirdPersonCam);

        if (ClientConfig.thirdPersonCam >= 0) {
            return CameraType.THIRD_PERSON_BACK;
        }

        return original;
    }
}