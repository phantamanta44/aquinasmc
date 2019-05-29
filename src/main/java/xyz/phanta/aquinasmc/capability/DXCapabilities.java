package xyz.phanta.aquinasmc.capability;

import io.github.phantamanta44.libnine.InitMe;
import io.github.phantamanta44.libnine.capability.StatelessCapabilitySerializer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

@SuppressWarnings("NullableProblems")
public class DXCapabilities {

    @CapabilityInject(AmmoStock.class)
    public static Capability<AmmoStock> AMMO_USER;

    @CapabilityInject(ProxyItem.class)
    public static Capability<ProxyItem> PROXY_ITEM;

    @InitMe
    public static void init() {
        CapabilityManager.INSTANCE.register(AmmoStock.class, new StatelessCapabilitySerializer<>(), AmmoStock.DefaultImpl::new);
        CapabilityManager.INSTANCE.register(ProxyItem.class, new StatelessCapabilitySerializer<>(), ProxyItem.DefaultImpl::new);
    }

}
