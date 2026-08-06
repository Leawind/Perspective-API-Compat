package io.github.leawind.perspectiveapicompat.internal.compat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Installs compatibility modules after Perspective API is ready.
///
/// Concrete integrations belong in isolated packages beneath this package. They must check whether
/// their target mod is loaded through the platform API before any class that references the optional
/// mod is initialized.
public final class CompatibilityBootstrap {
  private static final Logger LOGGER = LoggerFactory.getLogger(CompatibilityBootstrap.class);
  private static boolean initialized;

  private CompatibilityBootstrap() {}

  public static synchronized void initialize() {
    if (initialized) return;
    initialized = true;
    LOGGER.debug("Perspective API compatibility bootstrap is ready");
  }
}
