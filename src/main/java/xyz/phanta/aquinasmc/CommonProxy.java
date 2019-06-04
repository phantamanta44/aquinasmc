package xyz.phanta.aquinasmc;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import xyz.phanta.aquinasmc.event.MultiSlotItemHandler;
import xyz.phanta.aquinasmc.event.ReloadHandler;
import xyz.phanta.aquinasmc.sound.DXSounds;

public class CommonProxy {

    public void onPreInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ReloadHandler());
        MinecraftForge.EVENT_BUS.register(new MultiSlotItemHandler());
        MinecraftForge.EVENT_BUS.register(new DXSounds());
    }

    public void onInit(FMLInitializationEvent event) {
        // NO-OP
    }

    public void onPostInit(FMLPostInitializationEvent event) {
        // NO-OP
    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {
        // NO-OP
    }

}
