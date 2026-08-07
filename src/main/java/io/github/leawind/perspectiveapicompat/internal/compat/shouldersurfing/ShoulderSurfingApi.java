package io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Reflective bridge for SSR's public perspective API across its v4 and v5 package layouts.
///
/// SSR's API class must never be initialized early: its static initializer resolves the
/// implementation through ServiceLoader, which fails while SSR itself is still initializing and
/// permanently poisons the class. Availability probing therefore only loads classes without
/// initializing them, and the bridge is resolved lazily on first actual use, by which time SSR
/// has finished initializing.
final class ShoulderSurfingApi {
  private static final Logger LOGGER = LoggerFactory.getLogger(ShoulderSurfingApi.class);

  private static final String API_CLASS =
      "com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing";
  private static final String LEGACY_INSTANCE_CLASS =
      "com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing";
  private static final String MODERN_PERSPECTIVE_CLASS =
      "com.github.exopandora.shouldersurfing.api.client.Perspective";
  private static final String LEGACY_PERSPECTIVE_CLASS =
      "com.github.exopandora.shouldersurfing.api.model.Perspective";

  private Access access;
  private boolean probeFailed;

  /// Whether SSR is installed; must not initialize SSR's API class.
  boolean isAvailable() {
    if (access != null) return true;
    if (probeFailed) return false;
    boolean available = classPresent(API_CLASS) || classPresent(LEGACY_INSTANCE_CLASS);
    probeFailed = !available;
    return available;
  }

  void enable() {
    changePerspective("SHOULDER_SURFING", "enable");
  }

  void disable() {
    changePerspective("THIRD_PERSON_BACK", "disable");
  }

  private void changePerspective(String name, String action) {
    Access resolved = access();
    if (resolved == null) {
      LOGGER.warn("Cannot {} Shoulder Surfing: its public API is unavailable", action);
      return;
    }

    try {
      resolved.changePerspective(name);
    } catch (ReflectiveOperationException | LinkageError exception) {
      LOGGER.warn("Failed to {} Shoulder Surfing through its public API", action, exception);
    }
  }

  private synchronized Access access() {
    if (access != null) return access;

    try {
      access = Access.create();
      return access;
    } catch (ReflectiveOperationException | LinkageError exception) {
      return null;
    }
  }

  private static boolean classPresent(String name) {
    try {
      Class.forName(name, false, ShoulderSurfingApi.class.getClassLoader());
      return true;
    } catch (ClassNotFoundException | LinkageError exception) {
      return false;
    }
  }

  private static final class Access {
    private final Object instance;
    private final Method changePerspective;
    private final Class<?> perspectiveClass;

    private Access(Object instance, Method changePerspective, Class<?> perspectiveClass) {
      this.instance = instance;
      this.changePerspective = changePerspective;
      this.perspectiveClass = perspectiveClass;
    }

    static Access create() throws ReflectiveOperationException {
      ClassLoader loader = ShoulderSurfingApi.class.getClassLoader();
      // Load without initializing; initialization happens on the first static invocation below,
      // which only occurs while the perspective is actually used in game.
      Class<?> apiClass = Class.forName(API_CLASS, false, loader);
      Object instance = findInstance(apiClass, loader);
      Class<?> perspectiveClass = findPerspectiveClass(loader);
      Method changePerspective = apiClass.getMethod("changePerspective", perspectiveClass);
      return new Access(instance, changePerspective, perspectiveClass);
    }

    private static Object findInstance(Class<?> apiClass, ClassLoader loader)
        throws ReflectiveOperationException {
      try {
        return apiClass.getMethod("getInstance").invoke(null);
      } catch (NoSuchMethodException ignored) {
        Class<?> legacyInstanceClass = Class.forName(LEGACY_INSTANCE_CLASS, false, loader);
        return legacyInstanceClass.getMethod("getInstance").invoke(null);
      }
    }

    private static Class<?> findPerspectiveClass(ClassLoader loader) throws ClassNotFoundException {
      try {
        return Class.forName(MODERN_PERSPECTIVE_CLASS, false, loader);
      } catch (ClassNotFoundException ignored) {
        return Class.forName(LEGACY_PERSPECTIVE_CLASS, false, loader);
      }
    }

    void changePerspective(String name) throws ReflectiveOperationException {
      Object perspective = perspectiveClass.getField(name).get(null);
      try {
        changePerspective.invoke(instance, perspective);
      } catch (InvocationTargetException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof ReflectiveOperationException reflectiveException) {
          throw reflectiveException;
        }
        throw exception;
      }
    }
  }
}
