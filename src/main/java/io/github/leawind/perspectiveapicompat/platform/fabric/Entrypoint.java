/*? if fabric {*/
package io.github.leawind.perspectiveapicompat.platform.fabric;

import io.github.leawind.perspectiveapicompat.internal.logic.ModEntrypoint;
import net.fabricmc.api.ClientModInitializer;

@SuppressWarnings("unused")
public final class Entrypoint implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ModEntrypoint.initialize();
  }
}
/*? }*/
