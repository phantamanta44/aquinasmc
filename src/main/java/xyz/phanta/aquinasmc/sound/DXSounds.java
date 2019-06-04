package xyz.phanta.aquinasmc.sound;

import net.minecraft.util.SoundEvent;
import xyz.phanta.aquinasmc.Aquinas;

public class DXSounds {

//    private static final List<SoundEvent> toRegister = new ArrayList<>();

    public static final String KEY_WEAPON = Aquinas.MOD_ID + ".weapon.";

    private static final String KEY_WEAPON_MISC = KEY_WEAPON + "misc.";
    public static final SoundEvent WEAPON_EMPTY_GUN = create(KEY_WEAPON_MISC + "empty_gun");

    public static SoundEvent create(String name) {
        return new SoundEvent(Aquinas.INSTANCE.newResourceLocation(name));
    }

}
