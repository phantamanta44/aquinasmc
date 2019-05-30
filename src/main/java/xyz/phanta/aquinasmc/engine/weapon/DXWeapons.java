package xyz.phanta.aquinasmc.engine.weapon;

import xyz.phanta.aquinasmc.constant.LangConst;
import xyz.phanta.aquinasmc.engine.weapon.ammo.Ammo762;
import xyz.phanta.aquinasmc.engine.weapon.ammo.AmmoHE;

public class DXWeapons {

    public static final WeaponModel ASSAULT_GUN = new WeaponModel(
            LangConst.ITEM_ASSAULT_GUN, 2, 2, 60, Ammo762::new, AmmoHE::new);

}
