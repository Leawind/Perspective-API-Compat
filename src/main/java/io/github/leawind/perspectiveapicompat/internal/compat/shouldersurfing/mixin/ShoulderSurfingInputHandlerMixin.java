package io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.leawind.perspectiveapi.api.PerspectiveAPI;
import io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing.ShoulderSurfingCompatibility;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Keeps Perspective API as the sole owner of perspective selection while it is enabled.
@Pseudo
@Mixin(targets = "com.github.exopandora.shouldersurfing.client.InputHandler")
abstract class ShoulderSurfingInputHandlerMixin {
  @Unique
  private static final String[] perspectiveApiCompat$DIRECT_PERSPECTIVE_KEYS = {
    "TOGGLE_FIRST_PERSON",
    "TOGGLE_THIRD_PERSON_FRONT",
    "TOGGLE_THIRD_PERSON_BACK",
    "ENTER_FIRST_PERSON",
    "ENTER_THIRD_PERSON_FRONT",
    "ENTER_THIRD_PERSON_BACK",
    "ENTER_SHOULDER_SURFING"
  };

  @Unique private static KeyMapping[] perspectiveApiCompat$directPerspectiveKeys;

  @Inject(method = "tick", at = @At("HEAD"), remap = false)
  private void perspectiveApiCompat$consumeDirectPerspectiveKeys(CallbackInfo ci) {
    if (!perspectiveApiCompat$ownsPerspectiveSelection()) return;

    for (KeyMapping key : perspectiveApiCompat$directPerspectiveKeys(getClass())) {
      perspectiveApiCompat$consumeAll(key);
    }
  }

  @WrapOperation(
      method = "tick",
      remap = true,
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z",
              ordinal = 0,
              remap = true),
      slice =
          @Slice(
              from =
                  @At(
                      value = "FIELD",
                      target =
                          "Lnet/minecraft/client/Options;keyTogglePerspective:Lnet/minecraft/client/KeyMapping;",
                      remap = true)))
  private boolean perspectiveApiCompat$letPerspectiveApiConsumeToggle(
      KeyMapping key, Operation<Boolean> original) {
    return perspectiveApiCompat$ownsPerspectiveSelection() ? false : original.call(key);
  }

  @Unique
  private static boolean perspectiveApiCompat$ownsPerspectiveSelection() {
    return PerspectiveAPI.isEnabled() && ShoulderSurfingCompatibility.isAvailable();
  }

  @Unique
  private static void perspectiveApiCompat$consumeAll(KeyMapping key) {
    while (key.consumeClick()) {}
  }

  @Unique
  private static KeyMapping[] perspectiveApiCompat$directPerspectiveKeys(Class<?> inputHandlerClass) {
    KeyMapping[] cached = perspectiveApiCompat$directPerspectiveKeys;
    if (cached != null) return cached;

    List<KeyMapping> keys = new ArrayList<>();
    for (String fieldName : perspectiveApiCompat$DIRECT_PERSPECTIVE_KEYS) {
      try {
        Field field = inputHandlerClass.getField(fieldName);
        Object value = field.get(null);
        if (value instanceof KeyMapping key) {
          keys.add(key);
        }
      } catch (ReflectiveOperationException | LinkageError ignored) {
        // The older SSR versions do not expose all direct-perspective key bindings.
      }
    }

    cached = keys.toArray(KeyMapping[]::new);
    perspectiveApiCompat$directPerspectiveKeys = cached;
    return cached;
  }
}
