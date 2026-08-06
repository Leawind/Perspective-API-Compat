package io.github.leawind.perspectiveapicompat.platform.neoforge;

/*? if neoforge {*/
/*import io.github.leawind.perspectiveapicompat.PerspectiveApiCompat;
import io.github.leawind.perspectiveapicompat.internal.logic.ModEntrypoint;
/^? if >=1.21.11 {^/
/^import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = PerspectiveApiCompat.MOD_ID, dist = Dist.CLIENT)
public final class Entrypoint {
  public Entrypoint(ModContainer container) {
    ModEntrypoint.initialize();
  }
}
^//^? } else {^/
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(value = PerspectiveApiCompat.MOD_ID)
public final class Entrypoint {
  public Entrypoint(IEventBus modBus) {
    if (FMLEnvironment.dist != Dist.CLIENT) return;
    ModEntrypoint.initialize();
  }
}
/^? }^/
*//*? }*/
