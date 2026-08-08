package io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing.mixin;

import io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing.ShoulderSurfingCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Stops inactive SSR from vetoing Forge's crosshair overlay before other policies can run.
@Pseudo
@Mixin(
    targets = "com.github.exopandora.shouldersurfing.forge.event.ClientEventHandler",
    remap = false)
abstract class ShoulderSurfingForgeCrosshairEventHandlerMixin {
  @Inject(
      method = "preRenderGuiOverlayEvent",
      at = @At("HEAD"),
      cancellable = true,
      require = 0,
      remap = false)
  private static void perspectiveApiCompat$skipInactiveCrosshairVeto(CallbackInfo ci) {
    if (ShoulderSurfingCompatibility.shouldSuppressCrosshair()) {
      ci.cancel();
    }
  }
}
