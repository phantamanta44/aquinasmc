package xyz.phanta.aquinasmc.item;

import io.github.phantamanta44.libnine.util.IDisplayableMatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import xyz.phanta.aquinasmc.constant.LangConst;
import xyz.phanta.aquinasmc.item.base.ItemWeapon;

public class ItemAssaultGun extends ItemWeapon {

    ItemAssaultGun() {
        super(LangConst.ITEM_ASSAULT_GUN, 2, 2);
    }

    @Override
    protected int getMaxAmmo(ItemStack stack) {
        return 30;
    }

    @Override
    protected IDisplayableMatcher<ItemStack> getAmmoType(ItemStack stack) {
        return IDisplayableMatcher.of(() -> new ItemStack(Items.IRON_NUGGET), s -> s.getItem() == Items.IRON_NUGGET);
    }

    @Override
    protected void fire(EntityPlayer player, ItemStack stack) {
        // TODO swap out placeholder gun effects
        if (!player.world.isRemote) {
            EntityArrow arrow = ((ItemArrow)Items.ARROW).createArrow(player.world, new ItemStack(Items.ARROW), player);
            arrow.shoot(player, player.rotationPitch, player.rotationYaw, 0F, 2F, 1F);
            arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
            player.world.spawnEntity(arrow);
        }
        player.world.playSound(player, player.posX, player.posY, player.posZ,
                SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.3F, 2F);
        player.getCooldownTracker().setCooldown(this, 2);
    }

    @Override
    protected int getReloadDuration(EntityPlayer player, ItemStack stack) {
        return 60;
    }

    @Override
    protected void onReloadStarted(EntityPlayer player, ItemStack stack) {
        player.world.playSound(player, player.posX, player.posY, player.posZ,
                SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.PLAYERS, 1F, 1.5F);
    }

    @Override
    protected void onReloadFinished(EntityPlayer player, ItemStack stack) {
        player.world.playSound(player, player.posX, player.posY, player.posZ,
                SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.PLAYERS, 1F, 1.5F);
    }

}
