package xyz.phanta.aquinasmc.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nullable;

@Cancelable
public class PlayerDiggingEvent extends PlayerEvent {

    public final EntityPlayer player;
    public final CPacketPlayerDigging.Action action;
    @Nullable
    public final BlockPos pos;
    @Nullable
    public final EnumFacing face;

    public PlayerDiggingEvent(EntityPlayer player, CPacketPlayerDigging packet) {
        super(player);
        this.player = player;
        this.action = packet.getAction();
        this.pos = packet.getPosition();
        this.face = packet.getFacing();
    }

}
