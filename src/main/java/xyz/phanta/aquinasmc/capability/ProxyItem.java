package xyz.phanta.aquinasmc.capability;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;

public interface ProxyItem {

    InventoryPlayer getInventory();

    int getBaseSlot();

    int getProxySlot();

    void onProxyDestroyed();

    class DefaultImpl implements ProxyItem {

        @Override
        public InventoryPlayer getInventory() {
            return Minecraft.getMinecraft().player.inventory;
        }

        @Override
        public int getBaseSlot() {
            return 0;
        }

        @Override
        public int getProxySlot() {
            return 0;
        }

        @Override
        public void onProxyDestroyed() {
            // NO-OP
        }

    }

}
