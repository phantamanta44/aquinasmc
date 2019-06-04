package xyz.phanta.aquinasmc.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ProxyItem {

    int getBaseSlot();

    int getProxySlot();

    void onProxyDestroyed(EntityPlayer player);

    default ItemStack getBaseStack(EntityPlayer player) {
        return player.inventory.getStackInSlot(getBaseSlot());
    }

    class DefaultImpl implements ProxyItem {

        @Override
        public int getBaseSlot() {
            return 0;
        }

        @Override
        public int getProxySlot() {
            return 0;
        }

        @Override
        public void onProxyDestroyed(EntityPlayer player) {
            // NO-OP
        }

        @Override
        public ItemStack getBaseStack(EntityPlayer player) {
            return ItemStack.EMPTY;
        }

    }

}
