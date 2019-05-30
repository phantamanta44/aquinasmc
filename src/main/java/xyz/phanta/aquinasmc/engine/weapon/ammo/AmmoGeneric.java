package xyz.phanta.aquinasmc.engine.weapon.ammo;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import xyz.phanta.aquinasmc.Aquinas;
import xyz.phanta.aquinasmc.engine.weapon.WeaponModel;
import xyz.phanta.aquinasmc.item.ItemMisc;
import xyz.phanta.aquinasmc.sound.DXSounds;

public abstract class AmmoGeneric extends AmmoType {

    private final SoundEvent soundFire;

    public AmmoGeneric(WeaponModel weapon, String id, int maxAmmo, ItemMisc.Type ammoType) {
        super(weapon, id, maxAmmo, ammoType::newStack);
        this.soundFire = DXSounds.create(Aquinas.MOD_ID + ".weapon." + weapon.getName() + ".fire." + id);
    }

    @Override
    public void fire(EntityPlayer player, ItemStack weapon) {
        if (!player.world.isRemote) {
            spawnBullets(player, weapon);
        }
        player.world.playSound(player, player.posX, player.posY, player.posZ, soundFire, SoundCategory.PLAYERS, 1F, 1F);
        player.getCooldownTracker().setCooldown(weapon.getItem(), getShotDelay());
    }

    protected abstract int getShotDelay();

    protected abstract void spawnBullets(EntityPlayer player, ItemStack weapon);

}
