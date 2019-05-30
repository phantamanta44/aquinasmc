package xyz.phanta.aquinasmc.item;

import io.github.phantamanta44.libnine.InitMe;
import xyz.phanta.aquinasmc.Aquinas;
import xyz.phanta.aquinasmc.engine.weapon.DXWeapons;
import xyz.phanta.aquinasmc.item.base.ItemWeapon;

@SuppressWarnings("NullableProblems")
public class DXItems {

    public static ItemMisc MISC;

    public static ItemWeapon ASSAULT_GUN;

    @InitMe(Aquinas.MOD_ID)
    public static void init() {
        ASSAULT_GUN = new ItemWeapon(DXWeapons.ASSAULT_GUN);
        MISC = new ItemMisc();
    }

}
