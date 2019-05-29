package xyz.phanta.aquinasmc.capability;

import io.github.phantamanta44.libnine.util.IDisplayableMatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface AmmoStock {

    int getCurrentMag();

    int getMagSize();

    IDisplayableMatcher<ItemStack> getAmmoType(ItemStack stack);

    void reload(EntityPlayer player);

    void cancelReload(EntityPlayer player);

    class DefaultImpl implements AmmoStock {

        @Override
        public int getCurrentMag() {
            return 0;
        }

        @Override
        public int getMagSize() {
            return 0;
        }

        @Override
        public IDisplayableMatcher<ItemStack> getAmmoType(ItemStack stack) {
            return IDisplayableMatcher.of(() -> ItemStack.EMPTY, s -> true);
        }

        @Override
        public void reload(EntityPlayer player) {
            // NO-OP
        }

        @Override
        public void cancelReload(EntityPlayer player) {
            // NO-OP
        }

    }

}
