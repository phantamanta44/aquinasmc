package xyz.phanta.aquinasmc.item;

import io.github.phantamanta44.libnine.client.model.ParameterizedItemModel;
import io.github.phantamanta44.libnine.item.L9ItemSubs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import xyz.phanta.aquinasmc.constant.LangConst;

public class ItemMisc extends L9ItemSubs implements ParameterizedItemModel.IParamaterized {

    ItemMisc() {
        super(LangConst.ITEM_MISC, Type.VALUES.length);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (int i = 0; i < getVariantCount(); i++) {
                if (i != Type.DX_LOGO.ordinal()) {
                    items.add(new ItemStack(this, 1, i));
                }
            }
        }
    }

    @Override
    public void getModelMutations(ItemStack stack, ParameterizedItemModel.Mutation m) {
        m.mutate("type", Type.resolve(stack).name());
    }

    public enum Type {

        AMMO_10MM, AMMO_HE, AMMO_762, AMMO_3006, AMMO_DARTS_FLARE, AMMO_DARTS_NORMAL, AMMO_DARTS_POISON, AMMO_NAPALM,
        AMMO_PEPPER, AMMO_PLASMA, AMMO_PROD, AMMO_ROCKETS, AMMO_SABOT, AMMO_SHELLS, AMMO_WPROCKETS, AUG_UPGRADE,
        VIAL_AMBROSIA, DX_LOGO;

        public ItemStack newStack(int count) {
            return new ItemStack(DXItems.MISC, count, ordinal());
        }

        public static Type[] VALUES = values();

        public static Type resolve(ItemStack stack) {
            return VALUES[stack.getMetadata()];
        }

    }

}
