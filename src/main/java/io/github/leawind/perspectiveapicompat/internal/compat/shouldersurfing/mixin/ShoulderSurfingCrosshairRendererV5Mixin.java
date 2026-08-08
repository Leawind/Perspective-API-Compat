package io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing.mixin;

import io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing.ShoulderSurfingCompatibility;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Limits SSR v5 crosshair state to the Perspective that explicitly activates SSR.
@Pseudo
@Mixin(
    targets = "com.github.exopandora.shouldersurfing.client.renderer.CrosshairRenderer",
    remap = false)
abstract class ShoulderSurfingCrosshairRendererV5Mixin {
  @Inject(method = "isCrosshairVisible", at = @At("HEAD"), cancellable = true, remap = false)
  private void perspectiveApiCompat$restoreVanillaVisibility(
      CallbackInfoReturnable<Boolean> cir) {
    if (ShoulderSurfingCompatibility.shouldSuppressCrosshair()) {
      cir.setReturnValue(Minecraft.getInstance().options.getCameraType().isFirstPerson());
    }
  }

  @Inject(
      method = {
        "isCrosshairDynamic",
        "isObstructionCrosshairVisible",
        "isObstructionIndicatorVisible"
      },
      at = @At("HEAD"),
      cancellable = true,
      remap = false)
  private void perspectiveApiCompat$disableShoulderSurfingDecorations(
      CallbackInfoReturnable<Boolean> cir) {
    if (ShoulderSurfingCompatibility.shouldSuppressCrosshair()) {
      cir.setReturnValue(false);
    }
  }
}
