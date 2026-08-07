package io.github.leawind.perspectiveapicompat.internal.compat.shouldersurfing;

import io.github.leawind.perspectiveapi.api.PerspectiveBehavior;

/// Lets Shoulder Surfing supply the base camera state only while this Perspective is active.
final class ShoulderSurfingPerspectiveBehavior implements PerspectiveBehavior {
  private final ShoulderSurfingApi shoulderSurfing;

  ShoulderSurfingPerspectiveBehavior(ShoulderSurfingApi shoulderSurfing) {
    this.shoulderSurfing = shoulderSurfing;
  }

  @Override
  public boolean allowTransitionIn() {
    return false;
  }

  @Override
  public boolean allowTransitionOut() {
    return false;
  }

  @Override
  public boolean isAvailable() {
    return shoulderSurfing.isAvailable();
  }

  @Override
  public void onActivate() {
    shoulderSurfing.enable();
  }

  @Override
  public void onDeactivate() {
    shoulderSurfing.disable();
  }
}
