package xyz.phanta.aquinasmc.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import xyz.phanta.aquinasmc.Aquinas;
import xyz.phanta.aquinasmc.capability.DXCapabilities;
import xyz.phanta.aquinasmc.constant.LangConst;
import xyz.phanta.aquinasmc.network.PacketClientWeaponAmmoChangeRequest;
import xyz.phanta.aquinasmc.network.PacketClientWeaponReloadRequest;

import java.util.Objects;

public class DXKeybinds {

    public static final KeyBinding RELOAD = new KeyBinding(
            LangConst.KB_RELOAD_DESC, KeyConflictContext.IN_GAME, Keyboard.KEY_R, LangConst.KB_CATEGORY);
    public static final KeyBinding CHANGE_AMMO = new KeyBinding(
            LangConst.KB_CHANGE_AMMO_DESC, KeyConflictContext.IN_GAME, Keyboard.KEY_APOSTROPHE, LangConst.KB_CATEGORY);

    public static void init() {
        ClientRegistry.registerKeyBinding(RELOAD);
        ClientRegistry.registerKeyBinding(CHANGE_AMMO);
        MinecraftForge.EVENT_BUS.register(new DXKeybinds());
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (RELOAD.isPressed()) {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.hasCapability(DXCapabilities.AMMO_USER, null)) {
                Aquinas.INSTANCE.getNetworkHandler().sendToServer(new PacketClientWeaponReloadRequest(player));
                Objects.requireNonNull(stack.getCapability(DXCapabilities.AMMO_USER, null)).reload(player);
            }
        }
        if (CHANGE_AMMO.isPressed()) {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.hasCapability(DXCapabilities.AMMO_USER, null)) {
                Aquinas.INSTANCE.getNetworkHandler().sendToServer(new PacketClientWeaponAmmoChangeRequest(player));
                Objects.requireNonNull(stack.getCapability(DXCapabilities.AMMO_USER, null)).cycleAmmoType(player);
            }
        }
    }

}
