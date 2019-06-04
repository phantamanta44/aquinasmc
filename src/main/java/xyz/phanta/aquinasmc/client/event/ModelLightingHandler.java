package xyz.phanta.aquinasmc.client.event;

import io.github.phantamanta44.libnine.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.phanta.aquinasmc.client.DXClientConfig;
import xyz.phanta.aquinasmc.client.model.DXModel;
import xyz.phanta.aquinasmc.client.model.DXModelLoader;

public class ModelLightingHandler {

    private final DXModelLoader modelLoader;

    public ModelLightingHandler(DXModelLoader modelLoader) {
        this.modelLoader = modelLoader;
    }

    @SubscribeEvent
    public void onRenderHand(RenderSpecificHandEvent event) {
        if (DXClientConfig.modelAnimDynRender) {
            ItemStack stack = event.getItemStack();
            if (stack.hasCapability(DXModel.ANIM_CAP, null)) {
                int x = (int)Math.floor(OpenGlHelper.lastBrightnessX / 16F);
                int y = (int)Math.floor(OpenGlHelper.lastBrightnessY / 16F);
                modelLoader.setLighting(Minecraft.getMinecraft().entityRenderer.lightmapColors[y * 16 + x]);
                RenderUtils.enableFullBrightness();
                RenderHelper.disableStandardItemLighting();
            } else {
                RenderUtils.restoreLightmap();
                RenderHelper.enableStandardItemLighting();
            }
        }
    }

}
