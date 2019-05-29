package xyz.phanta.aquinasmc.client;

import io.github.phantamanta44.libnine.InitMe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import xyz.phanta.aquinasmc.Aquinas;
import xyz.phanta.aquinasmc.capability.DXCapabilities;
import xyz.phanta.aquinasmc.constant.LangConst;
import xyz.phanta.aquinasmc.network.PacketClientWeaponReloadRequest;

import java.util.Objects;

public class DXKeybinds {

    public static final KeyBinding RELOAD = new KeyBinding(
            LangConst.KB_RELOAD_DESC, KeyConflictContext.IN_GAME, Keyboard.KEY_R, "key.categories.gameplay");

    @InitMe(sides = { Side.CLIENT })
    public static void init() {
        ClientRegistry.registerKeyBinding(RELOAD);
        MinecraftForge.EVENT_BUS.register(new DXKeybinds());
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (RELOAD.isPressed()) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.hasCapability(DXCapabilities.AMMO_USER, null)) {
                Aquinas.INSTANCE.getNetworkHandler().sendToServer(new PacketClientWeaponReloadRequest(player));
                Objects.requireNonNull(stack.getCapability(DXCapabilities.AMMO_USER, null)).reload(player);
            }
        }
    }

}
