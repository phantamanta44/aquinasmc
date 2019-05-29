package xyz.phanta.aquinasmc.event;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xyz.phanta.aquinasmc.capability.DXCapabilities;
import xyz.phanta.aquinasmc.constant.NbtConst;
import xyz.phanta.aquinasmc.item.base.ItemWeapon;

import java.util.Objects;

public class ReloadHandler {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            NBTTagCompound tag = event.player.getEntityData();
            int selected = event.player.inventory.currentItem;
            if (tag.hasKey(NbtConst.PREV_HOTBAR)) {
                int prev = tag.getInteger(NbtConst.PREV_HOTBAR);
                if (prev != selected) {
                    ItemStack stack = event.player.inventory.getStackInSlot(prev);
                    if (stack.hasCapability(DXCapabilities.AMMO_USER, null)) {
                        Objects.requireNonNull(stack.getCapability(DXCapabilities.AMMO_USER, null)).cancelReload(event.player);
                    }
                    tag.setInteger(NbtConst.PREV_HOTBAR, selected);
                    return;
                }
            } else {
                tag.setInteger(NbtConst.PREV_HOTBAR, selected);
            }

            ItemStack stack = event.player.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemWeapon) {
                ((ItemWeapon)stack.getItem()).tickWeapon(event.player, stack);
            }
        }
    }

    /*@SubscribeEvent
    public void onSlotClick(SlotClickEvent event) {
        if (event.slotId >= 0) {
            Slot slot = event.container.getSlot(event.slotId);
            if (slot.inventory instanceof InventoryPlayer) {
                InventoryPlayer inv = (InventoryPlayer)slot.inventory;
                if (slot.getSlotIndex() == inv.currentItem) {
                    ItemStack stack = inv.getStackInSlot(inv.currentItem);
                    if (stack.hasCapability(DXCapabilities.AMMO_USER, null)) {
                        Objects.requireNonNull(stack.getCapability(DXCapabilities.AMMO_USER, null)).cancelReload(inv.player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntity();
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemWeapon) {
                Vec3d playerPos = player.getPositionVector();
                ((ItemWeapon)stack.getItem()).getReloadAmmoStacks(stack)
                        .forEach(s -> WorldUtils.dropItem(player.world, playerPos, s));
            }
        }
    }*/

}
