package xyz.phanta.aquinasmc.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import xyz.phanta.aquinasmc.capability.DXCapabilities;
import xyz.phanta.aquinasmc.capability.ProxyItem;
import xyz.phanta.aquinasmc.constant.ResConst;
import xyz.phanta.aquinasmc.item.base.ItemMultiSlot;

import java.util.Objects;

public class MultiSlotItemHandler {

    private static final double OVERLAY_Z_INDEX = 299D;

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
    public void onDropItem(ItemTossEvent event) {
        //noinspection ConstantConditions
        if (event.getEntityItem().getThrower() == null) {
            ItemStack stack = event.getEntityItem().getItem();
            if (stack.getItem() instanceof ItemMultiSlot && !stack.getItem().onDroppedByPlayer(stack, event.getPlayer())) {
                event.setCanceled(true);
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
                    Objects.requireNonNull(stack.getCapability(DXCapabilities.PROXY_ITEM, null)).onProxyDestroyed(player);
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
                            if (held.getItem() instanceof ItemMultiSlot) {
                                event.setCanceled(true);
                            } else {
                                ItemStack stack = inv.getStackInSlot(slotIndex);
                                if (stack.hasCapability(DXCapabilities.PROXY_ITEM, null)) {
                                    Objects.requireNonNull(stack.getCapability(DXCapabilities.PROXY_ITEM, null))
                                            .onProxyDestroyed(inv.player);
                                    inv.setInventorySlotContents(slotIndex, held);
                                    inv.setItemStack(ItemStack.EMPTY);
                                    event.setCanceled(true);
                                }
                            }
                        } else if (slotIndex < 36) {
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
                        } else {
                            event.setCanceled(true);
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
                                            .onProxyDestroyed(inv.player);
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
                        ItemStack swap = inv.getStackInSlot(event.data);
                        if (stack.getItem() instanceof ItemMultiSlot) {
                            if (swap.isEmpty() && !stack.hasCapability(DXCapabilities.PROXY_ITEM, null)) {
                                ItemMultiSlot item = (ItemMultiSlot)stack.getItem();
                                item.createProxyStack(inv, item.getBaseSlot(slotIndex, stack.getMetadata()), event.data);
                            }
                            event.setCanceled(true);
                        } else if (swap.getItem() instanceof ItemMultiSlot) {
                            inv.setInventorySlotContents(event.data, ItemStack.EMPTY);
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
                                    ItemStack baseStack = proxy.getBaseStack(inv.player);
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
            } else if (event.clickType == ClickType.QUICK_MOVE || event.clickType == ClickType.SWAP) {
                ItemStack stack = slot.getStack();
                if (stack.getItem() instanceof ItemMultiSlot) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDiggingPacket(PlayerDiggingEvent event) {
        if (event.action == CPacketPlayerDigging.Action.SWAP_HELD_ITEMS) {
            if (event.player.getHeldItemMainhand().getItem() instanceof ItemMultiSlot) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onDrawTooltip(RenderTooltipEvent.Pre event) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiContainer) {
            GuiContainer gui = (GuiContainer)screen;
            Slot slot = gui.getSlotUnderMouse();
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (slot != null && slot.inventory == player.inventory) {
                int slotIndex = slot.getSlotIndex();
                ItemStack stack = player.inventory.getStackInSlot(slotIndex);
                ItemStack held = player.inventory.getItemStack();
                if (slotIndex >= 9) {
                    if (held.isEmpty()) {
                        if (stack.getItem() instanceof ItemMultiSlot) {
                            ItemMultiSlot item = (ItemMultiSlot)stack.getItem();
                            int baseSlotIndex = item.getBaseSlot(slotIndex, stack.getMetadata());
                            Slot baseSlot = gui.inventorySlots.getSlotFromInventory(player.inventory, baseSlotIndex);
                            if (baseSlot != null) {
                                drawSelection(gui, baseSlot, item.getDimX(), item.getDimY(), true);
                            }
                            for (int i = 0; i < 9; i++) {
                                ItemStack proxyStack = player.inventory.getStackInSlot(i);
                                if (proxyStack.hasCapability(DXCapabilities.PROXY_ITEM, null)) {
                                    int proxyBaseSlotIndex = Objects.requireNonNull(
                                            proxyStack.getCapability(DXCapabilities.PROXY_ITEM, null)).getBaseSlot();
                                    if (proxyBaseSlotIndex == baseSlotIndex) {
                                        Slot proxySlot = gui.inventorySlots.getSlotFromInventory(player.inventory, i);
                                        if (proxySlot != null) {
                                            drawSelection(gui, proxySlot, 1, 1, true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (held.isEmpty() && stack.hasCapability(DXCapabilities.PROXY_ITEM, null)) {
                    ProxyItem proxy = Objects.requireNonNull(stack.getCapability(DXCapabilities.PROXY_ITEM, null));
                    Slot baseSlot = gui.inventorySlots.getSlotFromInventory(player.inventory, proxy.getBaseSlot());
                    if (baseSlot != null) {
                        ItemStack base = proxy.getBaseStack(player);
                        if (base.getItem() instanceof ItemMultiSlot) {
                            ItemMultiSlot item = (ItemMultiSlot)base.getItem();
                            drawSelection(gui, baseSlot, item.getDimX(), item.getDimY(), true);
                        } else {
                            drawSelection(gui, baseSlot, 1, 1, true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onDrawGui(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof GuiContainer) {
            GuiContainer gui = (GuiContainer)event.getGui();
            Slot slot = gui.getSlotUnderMouse();
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (slot != null && slot.inventory == player.inventory) {
                int slotIndex = slot.getSlotIndex();
                ItemStack held = player.inventory.getItemStack();
                if (slotIndex >= 9 && slotIndex < 36) {
                    if (held.getItem() instanceof ItemMultiSlot) {
                        ItemMultiSlot item = (ItemMultiSlot)held.getItem();
                        drawSelection(gui, slot, item.getDimX(), item.getDimY(), item.fitsInSlot(player.inventory, slotIndex));
                    }
                }
            }
        }
    }

    private static void drawSelection(GuiContainer gui, Slot slot, int slotDimX, int slotDimY, boolean good) {
        drawSelection(slot.xPos + gui.getGuiLeft(), slot.yPos + gui.getGuiTop(), slotDimX, slotDimY, good);
    }

    private static void drawSelection(double x, double y, int slotDimX, int slotDimY, boolean good) {
        double x2 = x + slotDimX * 18 - 18;
        double y2 = y + slotDimY * 18 - 18;
        double offset = 1.5D + 0.5D * Math.sin(System.currentTimeMillis() / 160D);
        x -= offset;
        y -= offset;
        x2 += offset;
        y2 += offset;
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.color(1F, 1F, 1F, 1F);
        if (good) {
            ResConst.OVERLAY_SELECTION_UL.draw(x, y, OVERLAY_Z_INDEX);
            ResConst.OVERLAY_SELECTION_UR.draw(x2, y, OVERLAY_Z_INDEX);
            ResConst.OVERLAY_SELECTION_LL.draw(x, y2, OVERLAY_Z_INDEX);
            ResConst.OVERLAY_SELECTION_LR.draw(x2, y2, OVERLAY_Z_INDEX);
        } else {
            ResConst.OVERLAY_SELECTION_BAD_UL.draw(x, y, OVERLAY_Z_INDEX);
            ResConst.OVERLAY_SELECTION_BAD_UR.draw(x2, y, OVERLAY_Z_INDEX);
            ResConst.OVERLAY_SELECTION_BAD_LL.draw(x, y2, OVERLAY_Z_INDEX);
            ResConst.OVERLAY_SELECTION_BAD_LR.draw(x2, y2, OVERLAY_Z_INDEX);
        }
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
    }

}
