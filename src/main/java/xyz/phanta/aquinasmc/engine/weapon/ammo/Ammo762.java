package xyz.phanta.aquinasmc.engine.weapon.ammo;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.item.ItemStack;
import xyz.phanta.aquinasmc.engine.weapon.WeaponModel;
import xyz.phanta.aquinasmc.item.ItemMisc;

public class Ammo762 extends AmmoGeneric {

    public Ammo762(WeaponModel weapon) {
        super(weapon, "762", 30, ItemMisc.Type.AMMO_762);
    }

    @Override
    protected int getShotDelay() {
        return 2;
    }

    @Override
    protected void spawnBullets(EntityPlayer player, ItemStack weapon) {
        EntitySnowball entitysnowball = new EntitySnowball(player.world, player);
        entitysnowball.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 5F, 1.0F);
        player.world.spawnEntity(entitysnowball);
    }

}
