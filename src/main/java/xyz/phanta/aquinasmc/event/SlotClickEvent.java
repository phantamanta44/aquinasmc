package xyz.phanta.aquinasmc.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class SlotClickEvent extends PlayerEvent {

    public final Container container;
    public final ClickType clickType;
    public final int slotId;
    public final int data;
    private ItemStack resultStack = ItemStack.EMPTY;

    public SlotClickEvent(EntityPlayer player, Container container, ClickType clickType, int slotId, int data) {
        super(player);
        this.container = container;
        this.clickType = clickType;
        this.slotId = slotId;
        this.data = data;
    }

    public ItemStack getResultStack() {
        return resultStack;
    }

    public void setResultStack(ItemStack stack) {
        this.resultStack = stack;
    }

}
