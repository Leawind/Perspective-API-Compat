package io.github.leawind.perspectiveapicompat.platform.fabric;

/*? if fabric {*/
import com.google.auto.service.AutoService;
import io.github.leawind.perspectiveapicompat.platform.api.PlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

@AutoService(PlatformHelper.class)
public final class PlatformHelperImpl implements PlatformHelper {
  @Override
  public boolean isModLoaded(String modId) {
    return FabricLoader.getInstance().isModLoaded(modId);
  }
}
/*? }*/
