package xyz.phanta.aquinasmc.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xyz.phanta.aquinasmc.capability.DXCapabilities;

import javax.annotation.Nullable;
import java.util.Objects;

public class PacketClientWeaponAmmoChangeRequest implements IMessage {

    private int slot;

    public PacketClientWeaponAmmoChangeRequest() {
        this.slot = -1;
    }

    public PacketClientWeaponAmmoChangeRequest(EntityPlayer player) {
        this.slot = player.inventory.currentItem;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slot = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(slot);
    }

    public static class Handler implements IMessageHandler<PacketClientWeaponAmmoChangeRequest, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(PacketClientWeaponAmmoChangeRequest message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            Objects.requireNonNull(player.world.getMinecraftServer()).addScheduledTask(() -> {
                if (message.slot == player.inventory.currentItem) {
                    ItemStack stack = player.getHeldItemMainhand();
                    if (stack.hasCapability(DXCapabilities.AMMO_USER, null)) {
                        Objects.requireNonNull(stack.getCapability(DXCapabilities.AMMO_USER, null)).cycleAmmoType(player);
                    }
                }
            });
            return null;
        }

    }

}
