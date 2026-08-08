package io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing;

import io.github.leawind.perspectiveapi.api.PerspectiveAPI;
import io.github.leawind.perspectiveapi.api.PerspectiveBehavior.BaseType;
import io.github.leawind.perspectiveapi.api.PerspectiveInfo;
import io.github.leawind.perspectiveapicompat.PerspectiveApiCompat;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Registers the Shoulder Surfing perspective without linking the base mod to Shoulder Surfing.
public final class ShoulderSurfingCompatibility {
  public static final String MOD_ID = "shouldersurfing";
  public static final String PERSPECTIVE_ID = "perspective_api_compat.shoulder_surfing";

  private static final Logger LOGGER = LoggerFactory.getLogger(ShoulderSurfingCompatibility.class);
  private static final ShoulderSurfingApi API = new ShoulderSurfingApi();
  private static final ThreadLocal<Boolean> CROSSHAIR_VETO_BYPASS =
      ThreadLocal.withInitial(() -> false);
  private static volatile boolean initialized;

  private ShoulderSurfingCompatibility() {}

  public static synchronized void initialize() {
    if (initialized) return;

    var registry = PerspectiveAPI.getRegistry();
    if (registry.contains(PERSPECTIVE_ID)) {
      LOGGER.warn("Perspective ID '{}' is already registered; skipping Shoulder Surfing", PERSPECTIVE_ID);
      return;
    }

    var info =
        PerspectiveInfo.builder(PERSPECTIVE_ID, Component.literal("Shoulder Surfing"))
            .description(Component.literal("Shoulder Surfing Reloaded camera"))
            .baseType(BaseType.THIRD_PERSON_BACK)
            .priority(1)
            .trait("third_person")
            .trait(PerspectiveApiCompat.MOD_ID + ":shoulder_surfing")
            .build();
    registry.register(info, new ShoulderSurfingPerspectiveBehavior(API));
    initialized = true;
    LOGGER.info("Registered Perspective API compatibility for Shoulder Surfing");
  }

  /// Returns whether the registered integration can control this SSR installation.
  public static boolean isAvailable() {
    return initialized && API.isAvailable();
  }

  /// Whether SSR's crosshair policy must yield to the active non-SSR Perspective.
  public static boolean shouldSuppressCrosshair() {
    return PerspectiveAPI.isEnabled()
        && isAvailable()
        && !PerspectiveAPI.isCurrent(PERSPECTIVE_ID);
  }

  /// Arms one permissive visibility read for Fabric's early SSR crosshair veto.
  public static void beginCrosshairVetoBypass() {
    if (shouldSuppressCrosshair()) {
      CROSSHAIR_VETO_BYPASS.set(true);
    } else {
      CROSSHAIR_VETO_BYPASS.remove();
    }
  }

  /// Consumes the one visibility read reserved for Fabric's early SSR crosshair veto.
  public static boolean consumeCrosshairVetoBypass() {
    if (!CROSSHAIR_VETO_BYPASS.get()) return false;
    CROSSHAIR_VETO_BYPASS.remove();
    return true;
  }

  /// Clears an unused Fabric crosshair-veto allowance after vanilla finishes the pass.
  public static void endCrosshairVetoBypass() {
    CROSSHAIR_VETO_BYPASS.remove();
  }
}
