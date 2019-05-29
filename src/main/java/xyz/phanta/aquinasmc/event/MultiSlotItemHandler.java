package xyz.phanta.aquinasmc.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import xyz.phanta.aquinasmc.capability.DXCapabilities;
import xyz.phanta.aquinasmc.capability.ProxyItem;
import xyz.phanta.aquinasmc.item.base.ItemMultiSlot;

import java.util.Objects;

public class MultiSlotItemHandler {

    @SubscribeEvent
    public void onPickupItem(EntityItemPickupEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItem().getItem();
        if (stack.getItem() instanceof ItemMultiSlot) {
            event.setCanceled(true);
            ItemMultiSlot item = (ItemMultiSlot)stack.getItem();
            int maxX = item.getMaxX();
            int maxY = item.getMaxY();
            for (int y = 0; y <= maxY; y++) {
                for (int x = 0; x <= maxX; x++) {
                    if (item.fitsInSlot(player.inventory, x, y)) {
                        item.placeInSlot(player.inventory, stack, x, y);
                        player.onItemPickup(event.getItem(), 1);
                        event.getItem().setDead();
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntity();
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack.hasCapability(DXCapabilities.PROXY_ITEM, null)) {
                    Objects.requireNonNull(stack.getCapability(DXCapabilities.PROXY_ITEM, null)).onProxyDestroyed();
                    player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @SubscribeEvent
    public void onSlotClick(SlotClickEvent event) {
        if (event.slotId >= 0) {
            Slot slot = event.container.getSlot(event.slotId);
            if (slot.inventory instanceof InventoryPlayer) {
                InventoryPlayer inv = (InventoryPlayer)slot.inventory;
                int slotIndex = slot.getSlotIndex();
                switch (event.clickType) {
                    case PICKUP:
                    case QUICK_CRAFT: {
                        ItemStack held = inv.getItemStack();
                        if (slotIndex < 9) {
                            if (held.isEmpty()) {
                                ItemStack stack = inv.getStackInSlot(slotIndex);
                                if (stack.hasCapability(DXCapabilities.PROXY_ITEM, null)) {
                                    Objects.requireNonNull(stack.getCapability(DXCapabilities.PROXY_ITEM, null))
                                            .onProxyDestroyed();
                                    inv.setInventorySlotContents(slotIndex, ItemStack.EMPTY);
                                    event.setCanceled(true);
                                }
                            } else if (held.getItem() instanceof ItemMultiSlot) {
                                event.setCanceled(true);
                            }
                        } else {
                            if (held.getItem() instanceof ItemMultiSlot) {
                                ItemMultiSlot item = (ItemMultiSlot)held.getItem();
                                if (item.fitsInSlot(inv, slotIndex)) {
                                    item.placeInSlot(inv, held, slotIndex);
                                    inv.setItemStack(ItemStack.EMPTY);
                                }
                                event.setCanceled(true);
                            } else {
                                ItemStack stack = inv.getStackInSlot(slotIndex);
                                if (stack.getItem() instanceof ItemMultiSlot) {
                                    ItemMultiSlot item = (ItemMultiSlot)stack.getItem();
                                    inv.setItemStack(item.clearFromSlot(inv, slotIndex));
                                    inv.setInventorySlotContents(slotIndex, held);
                                    event.setCanceled(true);
                                }
                            }
                        }
                        break;
                    }
                    case QUICK_MOVE: {
                        ItemStack stack = inv.getStackInSlot(slotIndex);
                        if (stack.getItem() instanceof ItemMultiSlot) {
                            ItemMultiSlot item = (ItemMultiSlot)stack.getItem();
                            if (slotIndex < 9) {
                                if (stack.hasCapability(DXCapabilities.PROXY_ITEM, null)) {
                                    Objects.requireNonNull(stack.getCapability(DXCapabilities.PROXY_ITEM, null))
                                            .onProxyDestroyed();
                                    inv.setInventorySlotContents(slotIndex, ItemStack.EMPTY);
                                }
                            } else if (!(event.container instanceof ContainerPlayer)) {
                                int baseSlot = item.getBaseSlot(slotIndex, stack.getMetadata());
                                ItemStack baseStack = item.clearFromSlot(inv, slotIndex);
                                slot.putStack(baseStack);
                                ItemStack result;
                                do {
                                    result = event.container.transferStackInSlot(inv.player, event.slotId);
                                } while (!result.isEmpty() && ItemStack.areItemsEqual(slot.getStack(), result));
                                if (result.isEmpty()) {
                                    item.placeInSlot(inv, baseStack, baseSlot);
                                } else {
                                    event.setResultStack(result);
                                }
                            }
                            event.setCanceled(true);
                        }
                        break;
                    }
                    case SWAP: {
                        ItemStack stack = inv.getStackInSlot(slotIndex);
                        if (stack.getItem() instanceof ItemMultiSlot) {
                            ItemMultiSlot item = (ItemMultiSlot)stack.getItem();
                            item.createProxyStack(inv, item.getBaseSlot(slotIndex, stack.getMetadata()), event.data);
                            event.setCanceled(true);
                        }
                        break;
                    }
                    case CLONE: {
                        if (inv.player.capabilities.isCreativeMode && inv.getItemStack().isEmpty()) {
                            ItemStack stack = inv.getStackInSlot(slotIndex);
                            if (stack.getItem() instanceof ItemMultiSlot) {
                                if (stack.hasCapability(DXCapabilities.PROXY_ITEM, null)) {
                                    ProxyItem proxy = Objects.requireNonNull(
                                            stack.getCapability(DXCapabilities.PROXY_ITEM, null));
                                    ItemStack baseStack = proxy.getInventory().getStackInSlot(proxy.getBaseSlot());
                                    inv.setItemStack(
                                            ItemHandlerHelper.copyStackWithSize(baseStack, baseStack.getMaxStackSize()));
                                    event.setCanceled(true);
                                } else {
                                    int meta = stack.getMetadata();
                                    if (meta != 0) {
                                        ItemStack baseStack = inv.getStackInSlot(
                                                ((ItemMultiSlot)stack.getItem()).getBaseSlot(slotIndex, meta));
                                        inv.setItemStack(
                                                ItemHandlerHelper.copyStackWithSize(baseStack, baseStack.getMaxStackSize()));
                                        event.setCanceled(true);
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case PICKUP_ALL: {
                        ItemStack held = inv.getItemStack();
                        if (held.getItem() instanceof ItemMultiSlot) {
                            event.setCanceled(true);
                        }
                        break;
                    }
                }
            } else if (event.clickType == ClickType.QUICK_MOVE) {
                ItemStack stack = slot.getStack();
                if (stack.getItem() instanceof ItemMultiSlot) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onDrawGui(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof GuiContainer) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            Slot slot = ((GuiContainer)event.getGui()).getSlotUnderMouse();
            if (slot != null && slot.inventory == player.inventory) {
                int slotIndex = slot.getSlotIndex();
                if (slotIndex >= 9) {
                    ItemStack stack = player.inventory.getItemStack();
                    if (stack.getItem() instanceof ItemMultiSlot) {
                        ItemMultiSlot item = (ItemMultiSlot)stack.getItem();
                        int baseX = event.getMouseX() - 8;
                        int baseY = event.getMouseY() - 8;
                        Gui.drawRect(baseX, baseY, baseX + 18 * item.getDimX() - 2, baseY + 18 * item.getDimY() - 2,
                                item.fitsInSlot(player.inventory, slotIndex) ? 0x77FFFFFF : 0x4AFF0000);
                        // TODO better multi slot indicator
                    }
                }
            }
        }
    }

}
