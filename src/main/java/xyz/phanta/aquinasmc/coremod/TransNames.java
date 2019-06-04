package xyz.phanta.aquinasmc.coremod;

class TransNames {

    // trivial types

    static final String C_ArmPose = "net/minecraft/client/model/ModelBiped$ArmPose";
    static final String C_CPacketPlayerDigging = "net/minecraft/network/play/client/CPacketPlayerDigging";
    static final String C_ClickType = "net/minecraft/inventory/ClickType";
    static final String C_Container = "net/minecraft/inventory/Container";
    static final String C_EntityPlayer = "net/minecraft/entity/player/EntityPlayer";
    static final String C_EntityPlayerMP = "net/minecraft/entity/player/EntityPlayerMP";
    static final String C_Event = "net/minecraftforge/fml/common/eventhandler/Event";
    static final String C_Item = "net/minecraft/item/Item";

    // mc types

    static final String C_ItemStack = "net/minecraft/item/ItemStack";
    static final String MN_ItemStack_getItem = "getItem";
    static final String MD_ItemStack_getItem = "()L" + C_Item + ";";

    static final String C_NetHandlerPlayServer = "net/minecraft/network/NetHandlerPlayServer";
    static final String FN_NetHandlerPlayServer_player = "player";
    static final String FD_NetHandlerPlayServer_player = "L" + C_EntityPlayerMP + ";";

    // forge types

    static final String C_EventBus = "net/minecraftforge/fml/common/eventhandler/EventBus";
    static final String MN_EventBus_post = "post";
    static final String MD_EventBus_post = "(L" + C_Event + ";)Z";

    static final String C_MinecraftForge = "net/minecraftforge/common/MinecraftForge";
    static final String FN_MinecraftForge_EVENT_BUS = "EVENT_BUS";
    static final String FD_MinecraftForge_EVENT_BUS = "L" + C_EventBus + ";";

    // dx types

    static final String C_ArmRotator = "xyz/phanta/aquinasmc/item/base/ArmRotator";
    static final String MN_ArmRotator_getArmPose = "getArmPose";
    static final String MD_ArmRotator_getArmPose = String.format("(L%s;L%s;)L%s;", C_EntityPlayer, C_ItemStack, C_ArmPose);

    static final String C_ModelLightingHandler = "xyz/phanta/aquinasmc/client/model/ModelLightingHandler";
    static final String MN_ModelLightingHandler_itemRenderHook = "itemRenderHook";
    static final String MD_ModelLightingHandler_itemRenderHook = "(L" + C_ItemStack + ";)V";

    static final String C_PlayerDiggingEvent = "xyz/phanta/aquinasmc/event/PlayerDiggingEvent";
    static final String MD_PlayerDiggingEvent_new = String.format("(L%s;L%s;)V", C_EntityPlayer, C_CPacketPlayerDigging);

    static final String C_SlotClickEvent = "xyz/phanta/aquinasmc/event/SlotClickEvent";
    static final String MD_SlotClickEvent_new = String.format("(L%s;L%s;L%s;II)V", C_EntityPlayer, C_Container, C_ClickType);
    static final String MN_SlotClickEvent_getResultStack = "getResultStack";
    static final String MD_SlotClickEvent_getResultStack = "()L" + C_ItemStack + ";";

}
