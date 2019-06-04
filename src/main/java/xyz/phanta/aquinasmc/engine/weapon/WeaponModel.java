package xyz.phanta.aquinasmc.engine.weapon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import xyz.phanta.aquinasmc.client.model.DXModel;
import xyz.phanta.aquinasmc.constant.NbtConst;
import xyz.phanta.aquinasmc.engine.weapon.ammo.AmmoType;
import xyz.phanta.aquinasmc.sound.DXSounds;
import xyz.phanta.aquinasmc.util.InsertionOrderedTable;
import xyz.phanta.aquinasmc.util.SafeNbt;

import javax.annotation.Nullable;
import java.util.List;

public class WeaponModel {

    private final String name;
    private final int dimX, dimY;
    private final int reloadTime;
    private final SoundEvent soundReloadStart, soundReloadEnd, soundEquip;
    private final AmmoType defType;
    private final InsertionOrderedTable<String, AmmoType> ammoTypes;

    public WeaponModel(String name, int itemSizeX, int itemSizeY, int reloadTime,
                       AmmoType.Provider defType, AmmoType.Provider... ammoTypes) {
        this.name = name;
        this.dimX = itemSizeX;
        this.dimY = itemSizeY;
        this.reloadTime = reloadTime;
        String weaponKey = DXSounds.KEY_WEAPON + name;
        String reloadKey = weaponKey + ".reload.";
        this.soundReloadStart = DXSounds.create(reloadKey + "start");
        this.soundReloadEnd = DXSounds.create(reloadKey + "end");
        this.soundEquip = DXSounds.create(weaponKey + ".equip");

        this.defType = defType.createType(this);
        this.ammoTypes = new InsertionOrderedTable<>();
        this.ammoTypes.put(this.defType.getId(), this.defType);
        for (AmmoType.Provider typeProvider : ammoTypes) {
            AmmoType type = typeProvider.createType(this);
            this.ammoTypes.put(type.getId(), type);
        }
    }

    public String getName() {
        return name;
    }

    public int getDimX() {
        return dimX;
    }

    public int getDimY() {
        return dimY;
    }

    public List<AmmoType> getAmmoTypes() {
        return ammoTypes.getValues();
    }

    @Nullable
    public AmmoType getNextAmmoType(EntityPlayer player, ItemStack weapon) {
        int types = ammoTypes.size();
        if (types == 1) {
            return null;
        }
        AmmoType type = getAmmoType(weapon);
        while (types > 1) {
            type = ammoTypes.getNextValue(type.getId());
            if (type == null) {
                type = defType;
            }
            for (int i = 0; i < 36; i++) {
                if (type.isValidAmmo(player.inventory.getStackInSlot(i))) {
                    return type;
                }
            }
            --types;
        }
        return null;
    }

    public AmmoType getAmmoType(ItemStack weapon) {
        String typeId = SafeNbt.getString(weapon, NbtConst.AMMO_TYPE, "");
        return typeId.isEmpty() ? defType : ammoTypes.get(typeId);
    }

    public void setAmmoType(ItemStack weapon, AmmoType type) {
        SafeNbt.setString(weapon, NbtConst.AMMO_TYPE, type.getId());
    }

    public int getReloadTime(EntityPlayer player, ItemStack weapon) {
        return reloadTime;
    }

    public void onReloadStart(EntityPlayer player, ItemStack weapon) {
        player.world.playSound(player, player.posX, player.posY, player.posZ, soundReloadStart, SoundCategory.PLAYERS, 1F, 1F);
        DXModel.setSequence(player, weapon, "reload_start");
        DXModel.setSkinState(player, weapon, 2, 0);
    }

    public void onReloadEnd(EntityPlayer player, ItemStack weapon) {
        player.world.playSound(player, player.posX, player.posY, player.posZ, soundReloadEnd, SoundCategory.PLAYERS, 1F, 1F);
        DXModel.setSequence(player, weapon, "reload_end");
    }

    public void onEquipped(EntityPlayer player, ItemStack weapon) {
        player.world.playSound(player, player.posX, player.posY, player.posZ, soundEquip, SoundCategory.PLAYERS, 1F, 1F);
        DXModel.setSequence(player, weapon, "equip");
        DXModel.setSkinState(player, weapon, 2, 0);
    }

    public void fire(EntityPlayer player, ItemStack weapon) {
        getAmmoType(weapon).fire(player, weapon);
        DXModel.setSequence(player, weapon, "shoot");
        DXModel.setSkinState(player, weapon, 2, player.world.rand.nextInt(2) + 1);
    }

    // TODO weapon mods
    // TODO recoil
    // TODO accuracy
    // TODO proper projectiles
    // TODO shell casings

}
