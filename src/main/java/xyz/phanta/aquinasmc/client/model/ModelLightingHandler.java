package xyz.phanta.aquinasmc.client.model;

import io.github.phantamanta44.libnine.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import xyz.phanta.aquinasmc.client.DXClientConfig;

public class ModelLightingHandler {

    @SuppressWarnings("NullableProblems")
    private static DXModelLoader modelLoader;
    private static boolean lightMapChanged = false;

    public static void init(DXModelLoader loader) {
        modelLoader = loader;
    }

    public static void itemRenderHook(ItemStack stack) {
        if (DXClientConfig.modelAnimDynRender && stack.hasCapability(DXModel.ANIM_CAP, null)) {
            int x = (int)Math.floor(OpenGlHelper.lastBrightnessX / 16F);
            int y = (int)Math.floor(OpenGlHelper.lastBrightnessY / 16F);
            modelLoader.setLighting(Minecraft.getMinecraft().entityRenderer.lightmapColors[y * 16 + x]);
            RenderUtils.enableFullBrightness();
            RenderHelper.disableStandardItemLighting();
            lightMapChanged = true;
        } else if (lightMapChanged) {
            RenderUtils.restoreLightmap();
            RenderHelper.enableStandardItemLighting();
            lightMapChanged = false;
        }
    }

}
