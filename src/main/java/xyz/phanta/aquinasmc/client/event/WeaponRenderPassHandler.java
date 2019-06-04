package xyz.phanta.aquinasmc.client.event;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class WeaponRenderPassHandler {

    // bad static fields for a bad hacky class
    private static boolean renderingFirstPersonHand = false;

    public static boolean isRenderingFirstPersonHand() {
        return renderingFirstPersonHand;
    }

    @SubscribeEvent
    public void onRenderWorld(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            renderingFirstPersonHand = false;
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        renderingFirstPersonHand = true;
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Pre event) {
        renderingFirstPersonHand = false;
    }

}
