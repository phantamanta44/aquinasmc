package xyz.phanta.aquinasmc.item;

import io.github.phantamanta44.libnine.InitMe;
import xyz.phanta.aquinasmc.Aquinas;

@SuppressWarnings("NullableProblems")
public class DXItems {

    public static ItemAssaultGun ASSAULT_GUN;

    @InitMe(Aquinas.MOD_ID)
    public static void init() {
        ASSAULT_GUN = new ItemAssaultGun();
    }

}
