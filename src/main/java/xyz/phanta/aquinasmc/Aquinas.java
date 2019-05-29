package xyz.phanta.aquinasmc;

import io.github.phantamanta44.libnine.Virtue;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;
import xyz.phanta.aquinasmc.network.PacketClientWeaponFire;
import xyz.phanta.aquinasmc.network.PacketClientWeaponReloadRequest;

@Mod(modid = Aquinas.MOD_ID, version = Aquinas.VERSION, useMetadata = true)
public class Aquinas extends Virtue {

    public static final String MOD_ID = "aquinas";
    public static final String VERSION = "1.0.0";

    @SuppressWarnings("NullableProblems")
    @Mod.Instance(MOD_ID)
    public static Aquinas INSTANCE;

    @SuppressWarnings("NullableProblems")
    @SidedProxy(
            clientSide = "xyz.phanta.aquinasmc.client.ClientProxy",
            serverSide = "xyz.phanta.aquinasmc.CommonProxy")
    public static CommonProxy PROXY;

    @SuppressWarnings("NullableProblems")
    public static Logger LOGGER;

    public Aquinas() {
        super(MOD_ID);
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        getNetworkHandler().registerMessage(
                PacketClientWeaponReloadRequest.Handler.class, PacketClientWeaponReloadRequest.class, 0, Side.SERVER);
        getNetworkHandler().registerMessage(
                PacketClientWeaponFire.Handler.class, PacketClientWeaponFire.class, 1, Side.SERVER);
        PROXY.onPreInit(event);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        PROXY.onInit(event);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        PROXY.onPostInit(event);
    }

}
