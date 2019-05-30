package xyz.phanta.aquinasmc.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.phanta.aquinasmc.Aquinas;

import java.util.ArrayList;
import java.util.List;

public class DXSounds {

//    private static final List<SoundEvent> toRegister = new ArrayList<>();

    public static final String KEY_WEAPON = Aquinas.MOD_ID + ".weapon.";

    private static final String KEY_WEAPON_MISC = KEY_WEAPON + "misc.";
    public static final SoundEvent WEAPON_EMPTY_GUN = create(KEY_WEAPON_MISC + "empty_gun");

    public static SoundEvent create(String name) {
        ResourceLocation loc = Aquinas.INSTANCE.newResourceLocation(name);
        SoundEvent sound = new SoundEvent(loc);
//        sound.setRegistryName(loc);
//        toRegister.add(sound);
        return sound;
    }

//    @SubscribeEvent
//    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
//        event.getRegistry().registerAll(toRegister.toArray(new SoundEvent[0]));
//    }

}
