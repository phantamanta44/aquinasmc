package xyz.phanta.aquinasmc.client;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import xyz.phanta.aquinasmc.CommonProxy;

public class ClientProxy extends CommonProxy {

    @Override
    public void onPreInit(FMLPreInitializationEvent event) {
        super.onPreInit(event);
        DXKeybinds.init();
    }

}
