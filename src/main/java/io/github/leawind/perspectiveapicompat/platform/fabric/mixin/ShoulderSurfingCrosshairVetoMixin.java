/*? if fabric {*/
package io.github.leawind.perspectiveapicompat.platform.fabric.mixin;

import io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing.ShoulderSurfingCompatibility;
/*? if >=26.2 {*/
import net.minecraft.client.gui.Hud;
/*? } else {*/
/*import net.minecraft.client.gui.Gui;
*//*? }*/
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Lets an inactive SSR pass its Fabric-only early veto before downstream policies run.
/*? if >=26.2 {*/
@Mixin(value = Hud.class, priority = 900)
/*? } else {*/
/*@Mixin(value = Gui.class, priority = 900)
*//*? }*/
abstract class ShoulderSurfingCrosshairVetoMixin {
  @Inject(
      /*? if >=26.1 {*/
      method = "extractCrosshair",
      /*? } else {*/
      /*method = "renderCrosshair",
      *//*? }*/
      at = @At("HEAD"),
      require = 0)
  private void perspectiveApiCompat$beginCrosshairPass(CallbackInfo ci) {
    ShoulderSurfingCompatibility.beginCrosshairVetoBypass();
  }

  @Inject(
      /*? if >=26.1 {*/
      method = "extractCrosshair",
      /*? } else {*/
      /*method = "renderCrosshair",
      *//*? }*/
      at = @At("RETURN"),
      require = 0)
  private void perspectiveApiCompat$endCrosshairPass(CallbackInfo ci) {
    ShoulderSurfingCompatibility.endCrosshairVetoBypass();
  }
}
/*? }*/
