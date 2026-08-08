package io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing.mixin;

import io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing.ShoulderSurfingCompatibility;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Restores vanilla crosshair visibility when an SSR v4 perspective does not own the camera.
@Pseudo
@Mixin(
    targets = "com.github.exopandora.shouldersurfing.api.model.CrosshairVisibility",
    remap = false)
abstract class ShoulderSurfingCrosshairVisibilityV4Mixin {
  @Inject(method = "doRender", at = @At("HEAD"), cancellable = true, remap = false)
  private void perspectiveApiCompat$restoreVanillaVisibility(
      HitResult hitResult, boolean aiming, CallbackInfoReturnable<Boolean> cir) {
    if (ShoulderSurfingCompatibility.shouldSuppressCrosshair()) {
      cir.setReturnValue(Minecraft.getInstance().options.getCameraType().isFirstPerson());
    }
  }
}
