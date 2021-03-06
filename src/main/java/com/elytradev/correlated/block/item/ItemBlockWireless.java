package com.elytradev.correlated.block.item;

import java.util.List;

import com.elytradev.correlated.CorrelatedWorldData;
import com.elytradev.correlated.EnergyHelper;
import com.elytradev.correlated.block.BlockWireless;
import com.elytradev.correlated.block.BlockWireless.Variant;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.tile.TileEntityMicrowaveBeam;
import com.elytradev.correlated.wifi.Beam;

import net.minecraft.block.Block;
import com.elytradev.correlated.C28n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemBlockWireless extends ItemBlock {

	public ItemBlockWireless(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.getMetadata() == 0) {
			C28n.formatList(tooltip, "tile.correlated.microwave_beam");
			tooltip.add(EnergyHelper.formatPotentialUsage(CConfig.beamPUsage));
		} else if (stack.getMetadata() == 1) {
			C28n.formatList(tooltip, "tile.correlated.optical");
			tooltip.add(EnergyHelper.formatPotentialUsage(CConfig.opticalPUsage));
		} else if (stack.getMetadata() == 2) {
			C28n.formatList(tooltip, "tile.correlated.beacon_lens");
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (stack.getMetadata() == 0) {
			return "tile.correlated.microwave_beam";
		} else if (stack.getMetadata() == 1) {
			return "tile.correlated.optical";
		} else if (stack.getMetadata() == 2) {
			return "tile.correlated.beacon_lens";
		}
		return super.getUnlocalizedName(stack);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getMetadata() == 0) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityMicrowaveBeam) {
				if (!stack.hasTagCompound()) {
					stack.setTagCompound(new NBTTagCompound());
				}
				stack.getTagCompound().setLong("OtherSide", pos.toLong());
				if (world.isRemote) {
					player.sendMessage(new TextComponentTranslation("msg.correlated.position_saved"));
				}
				return EnumActionResult.SUCCESS;
			}
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("OtherSide")) {
				BlockPos other = BlockPos.fromLong(stack.getTagCompound().getLong("OtherSide"));
				Beam b = CorrelatedWorldData.getFor(world).getWirelessManager().getBeam(other);
				if (b != null) {
					player.sendMessage(new TextComponentTranslation("msg.correlated.beam_exists"));
					return EnumActionResult.FAIL;
				}
			}
		}
		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public boolean getHasSubtypes() {
		return true;
	}
	
	@Override
	public int getMetadata(int damage) {
		return CBlocks.WIRELESS.getMetaFromState(CBlocks.WIRELESS.getDefaultState().withProperty(BlockWireless.VARIANT, Variant.VALUES[damage%Variant.VALUES.length]));
	}
	
}
