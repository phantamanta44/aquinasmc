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

    @Config.Comment({
            "Should models be animated dynamically?",
            "Dynamic animation provides smoother interpolation and proper muzzle flash lighting.",
            "When disabled, AOT animation is used, which provides better framerate at the cost of heightened memory use and initialization time"
    })
    @Config.RequiresMcRestart
    public static boolean modelAnimDynRender = true;

    @Config.Comment({
            "Frame density for AOT model animation interpolation.",
            "Higher values yield smoother animations but higher memory consumption and initialization time.",
            "Set to 0 to disable interpolation entirely.",
            "Requires a resource pack reload if modified!"
    })
    public static int modelAnimAotLerpDensity = 3;

}
