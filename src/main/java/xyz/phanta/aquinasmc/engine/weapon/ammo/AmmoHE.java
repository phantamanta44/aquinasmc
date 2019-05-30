package xyz.phanta.aquinasmc.engine.weapon.ammo;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import xyz.phanta.aquinasmc.engine.weapon.WeaponModel;
import xyz.phanta.aquinasmc.item.ItemMisc;

public class AmmoHE extends AmmoGeneric {

    public AmmoHE(WeaponModel weapon) {
        super(weapon, "he", 30, ItemMisc.Type.AMMO_HE);
    }

    @Override
    protected int getShotDelay() {
        return 15;
    }

    @Override
    protected void spawnBullets(EntityPlayer player, ItemStack weapon) {
        Vec3d dir = player.getLookVec();
        Vec3d pos = player.getPositionVector().add(dir);
        double vX = player.world.rand.nextGaussian() * 0.05D + dir.x;
        double vY = player.world.rand.nextGaussian() * 0.05D + dir.y;
        double vZ = player.world.rand.nextGaussian() * 0.05D + dir.z;
        player.world.spawnEntity(new EntityLargeFireball(player.world, pos.x, pos.y + player.getEyeHeight(), pos.z, vX, vY, vZ));
    }

}
