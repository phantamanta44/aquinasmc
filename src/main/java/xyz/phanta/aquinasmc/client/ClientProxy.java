package xyz.phanta.aquinasmc.client;

import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import xyz.phanta.aquinasmc.CommonProxy;
import xyz.phanta.aquinasmc.client.event.ModelLightingHandler;
import xyz.phanta.aquinasmc.client.model.DXModelLoader;

public class ClientProxy extends CommonProxy {

    @Override
    public void onPreInit(FMLPreInitializationEvent event) {
        super.onPreInit(event);
        DXKeybinds.init();
        DXModelLoader modelLoader = new DXModelLoader();
        ModelLoaderRegistry.registerLoader(modelLoader);
        MinecraftForge.EVENT_BUS.register(new ModelLightingHandler(modelLoader));
    }

}
