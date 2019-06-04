package xyz.phanta.aquinasmc.item.base;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ArmRotator {

    ModelBiped.ArmPose getArmPose(EntityPlayer player, ItemStack stack);

}
