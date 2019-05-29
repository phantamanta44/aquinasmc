package xyz.phanta.aquinasmc.item.base;

import io.github.phantamanta44.libnine.capability.provider.CapabilityBroker;
import io.github.phantamanta44.libnine.util.IDisplayableMatcher;
import io.github.phantamanta44.libnine.util.world.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import xyz.phanta.aquinasmc.Aquinas;
import xyz.phanta.aquinasmc.capability.AmmoStock;
import xyz.phanta.aquinasmc.capability.DXCapabilities;
import xyz.phanta.aquinasmc.constant.NbtConst;
import xyz.phanta.aquinasmc.network.PacketClientWeaponFire;
import xyz.phanta.aquinasmc.util.SafeNbt;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class ItemWeapon extends ItemMultiSlot {

    public ItemWeapon(String name, int dimX, int dimY) {
        super(name, dimX, dimY);
    }

    @Override
    public CapabilityBroker initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return super.initCapabilities(stack, nbt)
                .with(DXCapabilities.AMMO_USER, new WeaponAmmoStock(this, stack));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!player.isHandActive()) {
            ItemStack stack = player.getHeldItemMainhand();
            int ammo = getAmmoCount(proxy(player.inventory, stack));
            if (ammo == 0 && reload(player, stack)) {
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }
        return super.onItemRightClick(world, player, hand);
    }

    public void tryFire(EntityPlayer player, ItemStack stack) {
        stack = proxy(player.inventory, stack);
        if (proxy(player.inventory, player.getHeldItemMainhand()) == stack && getReloadTime(stack) == 0) {
            int ammo = getAmmoCount(stack);
            if (ammo > 0) {
                if (!player.getCooldownTracker().hasCooldown(this)) {
                    setAmmoCount(stack, ammo - 1);
                    if (player.world.isRemote) {
                        Aquinas.INSTANCE.getNetworkHandler().sendToServer(new PacketClientWeaponFire(player));
                    }
                    fire(player, stack);
                }
            }
        }
    }

    public void tickWeapon(EntityPlayer player, ItemStack stack) {
        stack = proxy(player.inventory, stack);
        int reloadTime = getReloadTime(stack);
        if (reloadTime == 0) {
            if (player.world.isRemote && Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown()) {
                tryFire(player, stack);
            }
        } else if (reloadTime == 1) {
            setReloadTime(stack, 0);
            setAmmoCount(stack, getAmmoCount(stack) + getReloadAmmo(stack));
            setReloadAmmo(stack, 0);
            onReloadFinished(player, stack);
        } else {
            setReloadTime(stack, reloadTime - 1);
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        cancelReload(player, item);
        return super.onDroppedByPlayer(item, player);
    }

    @Override
    public void onProxyDestroyed(EntityPlayer player, int slot, ItemStack stack) {
        cancelReload(player, stack);
    }

    private boolean reload(EntityPlayer player, ItemStack stack) {
        stack = proxy(player.inventory, stack);
        if (getReloadTime(stack) == 0) {
            int max = Math.max(getMaxAmmo(stack) - getAmmoCount(stack), 0);
            if (max != 0) {
                int found = 0;
                IDisplayableMatcher<ItemStack> matcher = getAmmoType(stack);
                List<ItemStack> inv = player.inventory.mainInventory;
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack ammo = inv.get(i);
                    if (!ammo.isEmpty() && matcher.test(ammo)) {
                        int possible = Math.min(max - found, ammo.getCount());
                        if (ammo.getCount() == possible) {
                            inv.set(i, ItemStack.EMPTY);
                        } else {
                            ammo.shrink(possible);
                        }
                        found += possible;
                        if (found == max) {
                            break;
                        }
                    }
                }
                if (found > 0) {
                    setReloadAmmo(stack, found);
                    setReloadTime(stack, getReloadDuration(player, stack));
                    onReloadStarted(player, stack);
                } else {
                    onReloadFailed(player, stack);
                }
                return true;
            }
        }
        return false;
    }

    private void cancelReload(EntityPlayer player, ItemStack stack) {
        stack = proxy(player.inventory, stack);
        if (getReloadTime(stack) != 0) {
            IItemHandler inv = Objects.requireNonNull(
                    player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
            List<ItemStack> ammoStacks = getReloadAmmoStacks(stack);
            Vec3d playerPos = player.getPositionVector();
            int i = 0;
            while (i < ammoStacks.size()) {
                ItemStack remaining = ItemHandlerHelper.insertItem(inv, ammoStacks.get(i), false);
                ++i;
                if (!remaining.isEmpty()) {
                    WorldUtils.dropItem(player.world, playerPos, remaining);
                    break;
                }
            }
            for (; i < ammoStacks.size(); i++) {
                WorldUtils.dropItem(player.world, playerPos, ammoStacks.get(i));
            }
            setReloadAmmo(stack, 0);
            setReloadTime(stack, 0);
        }
    }

    public List<ItemStack> getReloadAmmoStacks(ItemStack stack) {
        int ammo = getReloadAmmo(stack);
        ItemStack ammoStack = getAmmoType(stack).getVisual();
        int maxStackSize = ammoStack.getMaxStackSize();
        List<ItemStack> stacks = new ArrayList<>();
        while (ammo != 0) {
            int qty = Math.min(ammo, maxStackSize);
            stacks.add(ItemHandlerHelper.copyStackWithSize(ammoStack, qty));
            ammo -= qty;
        }
        return stacks;
    }

    protected abstract int getMaxAmmo(ItemStack stack);

    protected abstract IDisplayableMatcher<ItemStack> getAmmoType(ItemStack stack);

    protected abstract void fire(EntityPlayer player, ItemStack stack);

    protected abstract int getReloadDuration(EntityPlayer player, ItemStack stack);

    protected abstract void onReloadStarted(EntityPlayer player, ItemStack stack);

    protected abstract void onReloadFinished(EntityPlayer player, ItemStack stack);

    protected void onReloadFailed(EntityPlayer player, ItemStack stack) {
        player.world.playSound(player, player.posX, player.posY, player.posZ,
                SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1F, 2F);
    }

    private static int getAmmoCount(ItemStack stack) {
        return SafeNbt.getInt(stack, NbtConst.AMMO_COUNT, 0);
    }

    private static void setAmmoCount(ItemStack stack, int count) {
        SafeNbt.setInt(stack, NbtConst.AMMO_COUNT, count);
    }

    private static int getReloadTime(ItemStack stack) {
        return SafeNbt.getInt(stack, NbtConst.RELOAD_TIME, 0);
    }

    private static void setReloadTime(ItemStack stack, int time) {
        SafeNbt.setInt(stack, NbtConst.RELOAD_TIME, time);
    }

    private static int getReloadAmmo(ItemStack stack) {
        return SafeNbt.getInt(stack, NbtConst.RELOAD_AMMO, 0);
    }

    private static void setReloadAmmo(ItemStack stack, int count) {
        SafeNbt.setInt(stack, NbtConst.RELOAD_AMMO, count);
    }

    private static class WeaponAmmoStock implements AmmoStock {

        private final ItemWeapon item;
        private final ItemStack stack;

        WeaponAmmoStock(ItemWeapon item, ItemStack stack) {
            this.item = item;
            this.stack = stack;
        }

        @Override
        public int getCurrentMag() {
            return getAmmoCount(stack);
        }

        @Override
        public int getMagSize() {
            return item.getMaxAmmo(stack);
        }

        @Override
        public IDisplayableMatcher<ItemStack> getAmmoType(ItemStack stack) {
            return item.getAmmoType(stack);
        }

        @Override
        public void reload(EntityPlayer player) {
            item.reload(player, stack);
        }

        @Override
        public void cancelReload(EntityPlayer player) {
            item.cancelReload(player, stack);
        }

    }

}
