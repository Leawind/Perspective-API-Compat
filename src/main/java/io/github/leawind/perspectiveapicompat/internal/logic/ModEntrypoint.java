package io.github.leawind.perspectiveapicompat.internal.logic;

import io.github.leawind.perspectiveapi.api.PerspectiveAPI;
import io.github.leawind.perspectiveapicompat.PerspectiveApiCompat;
import io.github.leawind.perspectiveapicompat.internal.compat.CompatibilityBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Shared composition root invoked by each loader-specific entrypoint.
///
/// Perspective API owns the camera runtime. Compatibility modules are consequently installed only
/// after its public initialization contract reports readiness.
public final class ModEntrypoint {
  private static final Logger LOGGER = LoggerFactory.getLogger(ModEntrypoint.class);
  private static boolean initialized;

  private ModEntrypoint() {}

  public static synchronized void initialize() {
    if (initialized) return;
    initialized = true;

    PerspectiveAPI.runWhenReady(PerspectiveApiCompat.MOD_ID, CompatibilityBootstrap::initialize);
    LOGGER.info("{} initialized", PerspectiveApiCompat.MOD_NAME);
  }
}
