package xyz.phanta.aquinasmc.client;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xyz.phanta.aquinasmc.Aquinas;

@SideOnly(Side.CLIENT)
@Config(modid = Aquinas.MOD_ID, name = "aquinas_client")
public class DXClientConfig {

    @Config.Comment("Should guns be reloaded after firing the last round in a magazine?")
    public static boolean reloadOnEmpty = true;

}
