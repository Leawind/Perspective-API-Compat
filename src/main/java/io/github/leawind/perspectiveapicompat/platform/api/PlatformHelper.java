package io.github.leawind.perspectiveapicompat.platform.api;

/// Abstraction over loader-specific utility APIs.
public interface PlatformHelper {
  /// Returns whether a mod with the supplied ID is present in the current loader.
  boolean isModLoaded(String modId);
}
