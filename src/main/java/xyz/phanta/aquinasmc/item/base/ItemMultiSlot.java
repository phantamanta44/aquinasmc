package xyz.phanta.aquinasmc.item.base;

import io.github.phantamanta44.libnine.capability.provider.CapabilityBroker;
import io.github.phantamanta44.libnine.client.model.ParameterizedItemModel;
import io.github.phantamanta44.libnine.item.L9ItemSubs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import xyz.phanta.aquinasmc.capability.DXCapabilities;
import xyz.phanta.aquinasmc.capability.ProxyItem;
import xyz.phanta.aquinasmc.client.model.DXModel;
import xyz.phanta.aquinasmc.constant.NbtConst;
import xyz.phanta.aquinasmc.util.SafeNbt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ItemMultiSlot extends L9ItemSubs implements ParameterizedItemModel.IParamaterized {

    private final int dimX, dimY;
    protected final int proxyMeta;

    public ItemMultiSlot(String name, int dimX, int dimY) {
        super(name, dimX * dimY + 1);
        this.dimX = dimX;
        this.dimY = dimY;
        this.proxyMeta = getVariantCount() - 1;
        setMaxStackSize(1);
    }

    @Nonnull
    @Override
    public CapabilityBroker initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        CapabilityBroker caps = new CapabilityBroker();
        if (stack.getMetadata() == proxyMeta) {
            caps.with(DXCapabilities.PROXY_ITEM, new InventoryProxy(this, stack));
        }
        return caps;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));
        }
    }

    public int getDimX() {
        return dimX;
    }

    public int getDimY() {
        return dimY;
    }

    private int getMetaAtCoord(int x, int y) {
        return y * dimX + x;
    }

    private int getXCoordForMeta(int meta) {
        return meta % dimX;
    }

    private int getYCoordForMeta(int meta) {
        return (meta - getXCoordForMeta(meta)) / dimX;
    }

    public int getBaseSlot(int slot, int meta) {
        int slotX = slot % 9;
        int baseX = slotX - getXCoordForMeta(meta);
        int baseY = (slot - slotX) / 9 - getYCoordForMeta(meta);
        return baseY * 9 + baseX;
    }

    public int getMaxX() {
        return 9 - dimX;
    }

    public int getMaxY() {
        return 3 - dimY;
    }

    public boolean fitsInSlot(InventoryPlayer inv, int x, int y) {
        return x + dimX <= 9 && y + dimY <= 3 && fitsInSlot(inv, 9 * y + x + 9);
    }

    public boolean fitsInSlot(InventoryPlayer inv, int slot) {
        if (slot < 9) {
            return false;
        }
        int slotX = slot % 9;
        if (slotX + dimX > 9 || (slot - slotX) / 9 + dimY > 4) {
            return false;
        }
        for (int offY = 0; offY < dimY; offY++) {
            for (int offX = 0; offX < dimX; offX++) {
                if (!inv.getStackInSlot(offY * 9 + offX + slot).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void placeInSlot(InventoryPlayer inv, ItemStack stack, int x, int y) {
        for (int offY = 0; offY < dimY; offY++) {
            for (int offX = 0; offX < dimX; offX++) {
                inv.setInventorySlotContents((y + offY) * 9 + x + offX + 9,
                        offY == 0 && offX == 0 ? stack : new ItemStack(this, 1, getMetaAtCoord(offX, offY)));
            }
        }
        setItemPlaced(stack, true);
    }

    public void placeInSlot(InventoryPlayer inv, ItemStack stack, int slot) {
        for (int offY = 0; offY < dimY; offY++) {
            for (int offX = 0; offX < dimX; offX++) {
                inv.setInventorySlotContents(slot + offY * 9 + offX,
                        offY == 0 && offX == 0 ? stack : new ItemStack(this, 1, getMetaAtCoord(offX, offY)));
            }
        }
        setItemPlaced(stack, true);
    }

    public ItemStack clearFromSlot(InventoryPlayer inv, int slot) {
        int baseSlot = getBaseSlot(slot, inv.getStackInSlot(slot).getMetadata());
        for (int i = 0; i < 9; i++) {
            ItemStack proxyStack = inv.getStackInSlot(i);
            if (getProxyDestination(proxyStack) == baseSlot) {
                onProxyDestroyed(inv.player, i, proxyStack);
                inv.setInventorySlotContents(i, ItemStack.EMPTY);
            }
        }
        ItemStack baseStack = inv.getStackInSlot(baseSlot);
        for (int offY = 0; offY < dimY; offY++) {
            for (int offX = 0; offX < dimX; offX++) {
                int offSlot = baseSlot + offY * 9 + offX;
                if (inv.getStackInSlot(offSlot).getItem() == this) {
                    inv.setInventorySlotContents(offSlot, ItemStack.EMPTY);
                }
            }
        }
        setItemPlaced(baseStack, false);
        return baseStack;
    }

    public void createProxyStack(InventoryPlayer inv, int baseSlot, int proxySlot) {
        ItemStack proxy = new ItemStack(this, 1, proxyMeta);
        setProxyDestination(proxy, baseSlot);
        setProxyProxySlot(proxy, proxySlot);
        setProxyOwner(proxy, inv.player.getUniqueID());
        inv.setInventorySlotContents(proxySlot, proxy);
    }

    protected ItemStack proxy(InventoryPlayer inv, ItemStack proxy) {
        int proxySlot = getProxyDestination(proxy);
        return proxySlot == -1 ? proxy : inv.getStackInSlot(proxySlot);
    }

    protected void onProxyDestroyed(EntityPlayer player, int slot, ItemStack stack) {
        // NO-OP
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        if (getProxyDestination(item) != -1) {
            int slot = player.inventory.getSlotFor(item);
            onProxyDestroyed(player, slot, item);
            player.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
            return false;
        }
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(String.format("%s(%dx%d)", TextFormatting.DARK_GRAY, dimX, dimY));
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return getUnlocalizedName();
    }

    @Override
    public void getModelMutations(ItemStack stack, ParameterizedItemModel.Mutation m) {
        int meta = stack.getMetadata();
        m.mutate("role", meta == 0
                ? (getItemPlaced(stack) ? "multi" : "single")
                : (meta == proxyMeta ? "single" : "ghost"));
    }

    private static int getProxyDestination(ItemStack stack) {
        return SafeNbt.getInt(stack, NbtConst.PROXY_DEST_SLOT, -1);
    }

    private static void setProxyDestination(ItemStack stack, int slot) {
        SafeNbt.setInt(stack, NbtConst.PROXY_DEST_SLOT, slot);
    }

    private static int getProxyProxySlot(ItemStack stack) {
        return Objects.requireNonNull(stack.getTagCompound()).getInteger(NbtConst.PROXY_PROXY_SLOT);
    }

    private static void setProxyProxySlot(ItemStack stack, int slot) {
        SafeNbt.setInt(stack, NbtConst.PROXY_PROXY_SLOT, slot);
    }

    private static UUID getProxyOwner(ItemStack stack) {
        return UUID.fromString(Objects.requireNonNull(stack.getTagCompound()).getString(NbtConst.PROXY_OWNER_ID));
    }

    private static void setProxyOwner(ItemStack stack, UUID id) {
        SafeNbt.setString(stack, NbtConst.PROXY_OWNER_ID, id.toString());
    }

    private static boolean getItemPlaced(ItemStack stack) {
        return SafeNbt.getBool(stack, NbtConst.MULTI_SLOT_PLACED, false);
    }

    private static void setItemPlaced(ItemStack stack, boolean placed) {
        SafeNbt.setBool(stack, NbtConst.MULTI_SLOT_PLACED, placed);
    }

    private static class InventoryProxy implements ProxyItem {

        private final ItemMultiSlot item;
        private final ItemStack stack;

        InventoryProxy(ItemMultiSlot item, ItemStack stack) {
            this.item = item;
            this.stack = stack;
        }

        @Override
        public int getBaseSlot() {
            return getProxyDestination(stack);
        }

        @Override
        public int getProxySlot() {
            return getProxyProxySlot(stack);
        }

        @Override
        public void onProxyDestroyed(EntityPlayer player) {
            item.onProxyDestroyed(player, getProxySlot(), stack);
        }

    }

    public static class ProxyModelItem implements DXModel.DXModelItem {

        private static final DXModel.DXModelItem NOOP_MODEL_ITEM = new DXModel.DXModelItem.Impl();
        private final ItemStack proxyStack;

        public ProxyModelItem(ItemStack proxyStack) {
            this.proxyStack = proxyStack;
        }

        @Override
        public boolean requiresPlayer() {
            return true;
        }

        private DXModel.DXModelItem getDelegate(@Nullable EntityPlayer player) {
            if (player == null) {
                throw new UnsupportedOperationException();
            }
            DXModel.DXModelItem modelItem = Objects.requireNonNull(proxyStack.getCapability(DXCapabilities.PROXY_ITEM, null))
                    .getBaseStack(player).getCapability(DXModel.ANIM_CAP, null);
            return modelItem != null ? modelItem : NOOP_MODEL_ITEM;
        }

        @Override
        public UUID getIdentifier(@Nullable EntityPlayer player) {
            return getDelegate(player).getIdentifier(player);
        }

        @Nullable
        @Override
        public String getSequence(@Nullable EntityPlayer player) {
            return getDelegate(player).getSequence(player);
        }

        @Override
        public void setSequence(@Nullable EntityPlayer player, String sequence) {
            getDelegate(player).setSequence(player, sequence);
        }

        @Override
        public int getSkinState(@Nullable EntityPlayer player, int skin) {
            return getDelegate(player).getSkinState(player, skin);
        }

        @Override
        public void setSkinState(@Nullable EntityPlayer player, int skin, int state) {
            getDelegate(player).setSkinState(player, skin, state);
        }

        @Override
        public byte getActionIndex(@Nullable EntityPlayer player) {
            return getDelegate(player).getActionIndex(player);
        }

    }

}
