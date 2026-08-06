package io.github.leawind.perspectiveapicompat.platform.api;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Loads platform-specific services without exposing loader APIs to compatibility code.
public final class Services {
  private static final Logger LOGGER = LoggerFactory.getLogger(Services.class);

  public static final PlatformHelper PLATFORM_HELPER = loadFirst(PlatformHelper.class);

  private Services() {}

  private static <T> T loadFirst(Class<T> type) {
    var service =
        ServiceLoader.load(type)
            .findFirst()
            .orElseThrow(
                () -> new ServiceConfigurationError("Failed to load service for " + type.getName()));
    LOGGER.debug("Loaded {} for service {}", service, type);
    return service;
  }
}
