package xyz.phanta.aquinasmc.engine.weapon.ammo;

import io.github.phantamanta44.libnine.util.helper.ItemUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import xyz.phanta.aquinasmc.engine.weapon.WeaponModel;

import java.util.function.IntFunction;

public abstract class AmmoType {

    private final WeaponModel weapon;
    private final String id;
    private final int maxAmmo;
    private final IntFunction<ItemStack> ammoFactory;

    public AmmoType(WeaponModel weapon, String id, int maxAmmo, IntFunction<ItemStack> ammoFactory) {
        this.weapon = weapon;
        this.id = id;
        this.maxAmmo = maxAmmo;
        this.ammoFactory = ammoFactory;
    }

    public WeaponModel getWeapon() {
        return weapon;
    }

    public String getId() {
        return id;
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public ItemStack getAmmoStack(int count) {
        return ammoFactory.apply(count);
    }

    public boolean isValidAmmo(ItemStack stack) {
        return ItemUtils.matchesWithWildcard(getAmmoStack(1), stack);
    }

    public abstract void fire(EntityPlayer player, ItemStack weapon);

    @FunctionalInterface
    public interface Provider {

        AmmoType createType(WeaponModel model);

    }

}
