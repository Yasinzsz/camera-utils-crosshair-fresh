package com.yasinz.crosshair.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yasinz.crosshair.CrosshairBuildingRenderer;
import com.yasinz.crosshair.CrosshairFeatureRenderer;
import com.yasinz.crosshair.CameraUtilsCrosshair;
import com.yasinz.crosshair.CrosshairConfig;
import com.yasinz.crosshair.PlacementGhost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Final
    private ModelManager modelManager;

    private BlockModelResolver cameraUtilsCrosshair$blockModelResolver;

    private final BlockModelRenderState cameraUtilsCrosshair$ghostState =
            new BlockModelRenderState();

    @Inject(
            method = "submitBlockOutline",
            at = @At("HEAD")
    )
    private void cameraUtilsCrosshair$renderGhost(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            LevelRenderState levelRenderState,
            CallbackInfo ci
    ) {
        if (!PlacementGhost.shouldRender()) {
            return;
        }

        if (cameraUtilsCrosshair$blockModelResolver == null) {
            cameraUtilsCrosshair$blockModelResolver =
                    new BlockModelResolver(modelManager);
        }

        var state =
                PlacementGhost.getBlockState();

        if (state == null) {
            return;
        }

        var placementPos =
                PlacementGhost.getPosition();

        var camera =
                levelRenderState.cameraRenderState.pos;

        poseStack.pushPose();

        double phase =
                (System.nanoTime() / 1_000_000_000.0D) * 3.0D;
        double bob =
                Math.sin(phase) * 0.035D;

        poseStack.translate(
                placementPos.getX() - camera.x + 0.5D,
                placementPos.getY() - camera.y + 0.5D + bob,
                placementPos.getZ() - camera.z + 0.5D
        );

        float scale =
                0.985F + (float) (Math.sin(phase) * 0.008D);

        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        cameraUtilsCrosshair$ghostState.clear();

        cameraUtilsCrosshair$blockModelResolver.update(
                cameraUtilsCrosshair$ghostState,
                state,
                net.minecraft.client.renderer.block.model.BlockDisplayContext.create()
        );

        cameraUtilsCrosshair$ghostState.blockLightCoords =
                0x00F000F0;

        submitNodeCollector.submitBlockModel(
                poseStack,
                RenderTypes.translucentMovingBlock(),
                cameraUtilsCrosshair$ghostState.setupModel(
                        new org.joml.Matrix4f(),
                        false
                ),
                cameraUtilsCrosshair$ghostState.tintLayers().toIntArray(),
                0,
                0,
                0
        );

        poseStack.popPose();
    }

    @Inject(
            method = "collectPerFrameRenderThreadGizmos",
            at = @At("RETURN")
    )
    private void cameraUtilsCrosshair$buildingPreview(
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<
                    net.minecraft.gizmos.Gizmos.TemporaryCollection
                    > cir
    ) {
        Minecraft mc =
                Minecraft.getInstance();

        if (mc.hitResult != null &&
                mc.hitResult.getType() ==
                        net.minecraft.world.phys.HitResult.Type.BLOCK) {
            CrosshairBuildingRenderer.render();
        }

        CrosshairFeatureRenderer.renderWorld();
    }
}