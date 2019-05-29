package xyz.phanta.aquinasmc.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SafeNbt {

    public static int getInt(ItemStack stack, String key, int def) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(key) ? tag.getInteger(key) : def;
    }

    public static void setInt(ItemStack stack, String key, int value) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setInteger(key, value);
    }

    public static String getString(ItemStack stack, String key, String def) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(key) ? tag.getString(key) : def;
    }

    public static void setString(ItemStack stack, String key, String value) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setString(key, value);
    }

}
