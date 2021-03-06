package com.elytradev.correlated.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import com.elytradev.correlated.C28n;
import com.elytradev.correlated.ColorType;

public class ItemModule extends Item {
	public static final String[] types = {
		"speech",
		"antigrav"
	};
	public ItemModule() {
		setMaxStackSize(1);
	}

	public int getTypeColor(ItemStack stack) {
		return ColorType.MODULE.getColor(stack.getMetadata());
	}
	
	public String getType(ItemStack stack) {
		if (stack.getMetadata() < 0 || stack.getMetadata() >= types.length) return null;
		return types[stack.getMetadata()];
	}

	@Override
	public boolean getHasSubtypes() {
		return true;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return C28n.format("item.correlated.module."+getType(stack)+".name");
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (isInCreativeTab(tab)) {
			for (int i = 0; i < types.length; i++) {
				subItems.add(new ItemStack(this, 1, i));
			}
		}
	}

}
