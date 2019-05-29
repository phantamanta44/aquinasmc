package xyz.phanta.aquinasmc.constant;

import io.github.phantamanta44.libnine.util.render.TextureRegion;
import io.github.phantamanta44.libnine.util.render.TextureResource;
import xyz.phanta.aquinasmc.Aquinas;

public class ResConst {

    private static final TextureResource OVERLAY_SELECTION = Aquinas.INSTANCE
            .newTextureResource("textures/gui/selection.png", 32, 64);
    public static final TextureRegion OVERLAY_SELECTION_UL = OVERLAY_SELECTION.getRegion(0, 0, 16, 16);
    public static final TextureRegion OVERLAY_SELECTION_UR = OVERLAY_SELECTION.getRegion(16, 0, 16, 16);
    public static final TextureRegion OVERLAY_SELECTION_LL = OVERLAY_SELECTION.getRegion(0, 16, 16, 16);
    public static final TextureRegion OVERLAY_SELECTION_LR = OVERLAY_SELECTION.getRegion(16, 16, 16, 16);
    public static final TextureRegion OVERLAY_SELECTION_BAD_UL = OVERLAY_SELECTION.getRegion(0, 32, 16, 16);
    public static final TextureRegion OVERLAY_SELECTION_BAD_UR = OVERLAY_SELECTION.getRegion(16, 32, 16, 16);
    public static final TextureRegion OVERLAY_SELECTION_BAD_LL = OVERLAY_SELECTION.getRegion(0, 48, 16, 16);
    public static final TextureRegion OVERLAY_SELECTION_BAD_LR = OVERLAY_SELECTION.getRegion(16, 48, 16, 16);

}
