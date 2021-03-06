package com.elytradev.correlated;

import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.elytradev.correlated.compat.probe.UnitPotential;
import com.elytradev.correlated.entity.automaton.Opcode;
import com.elytradev.correlated.init.CBlocks;
import com.elytradev.correlated.init.CConfig;
import com.elytradev.correlated.init.CDimensions;
import com.elytradev.correlated.init.CEntities;
import com.elytradev.correlated.init.CItems;
import com.elytradev.correlated.init.CNetwork;
import com.elytradev.correlated.init.CRecipes;
import com.elytradev.correlated.init.CRecords;
import com.elytradev.correlated.init.CSoundEvents;
import com.elytradev.correlated.init.CTiles;
import com.elytradev.correlated.proxy.Proxy;
import buildcraft.api.mj.IMjReceiver;

import com.elytradev.probe.api.IProbeDataProvider;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

@Mod(modid=Correlated.MODID, name=Correlated.NAME, version=Correlated.VERSION,
	updateJSON="http://unascribed.com/update-check/correlated.json", dependencies="after:albedo")
public class Correlated {
	public static final String MODID = "correlated";
	public static final String NAME = "Correlated";
	public static final String VERSION = "@VERSION@";
	
	@Instance
	public static Correlated inst;
	@SidedProxy(clientSide="com.elytradev.correlated.proxy.ClientProxy", serverSide="com.elytradev.correlated.proxy.Proxy")
	public static Proxy proxy;

	
	@CapabilityInject(IProbeDataProvider.class)
	public static Capability<?> PROBE;
	@CapabilityInject(IMjReceiver.class)
	public static Capability<?> MJ_RECEIVER;
	@CapabilityInject(ITeslaConsumer.class)
	public static Capability<?> TESLA_CONSUMER;
	

	public static CreativeTabs CREATIVE_TAB = new CreativeTabs("correlated") {
		ItemStack stack = null;
		@Override
		public ItemStack getTabIconItem() {
			if (stack == null) {
				stack = new ItemStack(CItems.MISC, 1, 9);
			}
			return stack;
		}
	};

	public boolean jeiAvailable = false;
	
	public Consumer<String> jeiQueryUpdater = (s) -> {};
	public Supplier<String> jeiQueryReader = () -> "";
	public BiPredicate<String, ItemStack> colorSearcher = (query, stack) -> false;


	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		CConfig.setConfig(new Configuration(e.getSuggestedConfigurationFile()));
		CConfig.load();
		CConfig.save();
		
		CDimensions.register();
		
		MinecraftForge.EVENT_BUS.register(CSoundEvents.class);
		
		MinecraftForge.EVENT_BUS.register(CBlocks.class);
		MinecraftForge.EVENT_BUS.register(CItems.class);
		CRecords.register();
		
		CTiles.register();
		CEntities.register();

		MinecraftForge.EVENT_BUS.register(CRecipes.class);
		
		CNetwork.register();
		
		Opcode.init();
		
		if (Loader.isModLoaded("probedataprovider")) {
			UnitPotential.register();
		}
		if (Loader.isModLoaded("thermionics")) {
			CLog.info("Thermionics, are you thinking what I'm thinking?");
			try {
				Class.forName("com.elytradev.thermionics.CorrelatedHint");
			} catch (Throwable t) {
				CLog.info("...Uh, Thermionics? Where are you?");
			}
		} else {
			CLog.info("I hear there's this really cool mod called Thermionics, you should install it!");
		}
		
		MinecraftForge.EVENT_BUS.register(this);
		proxy.preInit();
	}

	@EventHandler
	public void onPostInit(FMLPostInitializationEvent e) {
		proxy.postInit();
	}
	
	@SubscribeEvent
	public void onMissingMappingsBlock(RegistryEvent.MissingMappings<Block> e) {
		for (Mapping<Block> mm : e.getAllMappings()) {
			ResourceLocation newloc = null;
			if (mm.key.getResourceDomain().equals("correlatedpotentialistics")) {
				if (mm.key.getResourcePath().equals("vt")) {
					newloc = new ResourceLocation("correlated", "terminal");
				} else if (mm.key.getResourcePath().equals("iface")) {
					newloc = new ResourceLocation("correlated", "interface");
				} else {
					newloc = new ResourceLocation("correlated", mm.key.getResourcePath());
				}
			} else if (mm.key.getResourceDomain().equals("correlated")) {
				if (mm.key.getResourcePath().equals("iface")) {
					newloc = new ResourceLocation("correlated", "interface");
				}
			}
			if (newloc != null) {
				mm.remap(Block.REGISTRY.getObject(newloc));
			}
		}
	}
	
	@SubscribeEvent
	public void onMissingMappingsItem(RegistryEvent.MissingMappings<Item> e) {
		for (Mapping<Item> mm : e.getAllMappings()) {
			ResourceLocation newloc = null;
			if (mm.key.getResourceDomain().equals("correlatedpotentialistics")) {
				if (mm.key.getResourcePath().equals("wireless_terminal")) {
					newloc = new ResourceLocation("correlated", "handheld_terminal");
				} else {
					newloc = new ResourceLocation("correlated", mm.key.getResourcePath());
				}
			} else if (mm.key.getResourceDomain().equals("correlated")) {
				if (mm.key.getResourcePath().equals("wireless_terminal")) {
					newloc = new ResourceLocation("correlated", "handheld_terminal");
				}
			}
			if (newloc != null) {
				mm.remap(Item.REGISTRY.getObject(newloc));
			}
		}
	}

	@SubscribeEvent
	public void onBlockRightClick(RightClickBlock e) {
		if (e.getItemStack().getItem() == CItems.DEBUGGINATOR) {
			e.setUseItem(Result.ALLOW);
			e.setUseBlock(Result.DENY);
		}
	}
	
	@SubscribeEvent
	public void onLootAdd(LootTableLoadEvent e) {
		if (e.getName().toString().startsWith("minecraft:chests/")) {
			e.getTable().getPool("main").addEntry(new LootEntryItem(CItems.MISC, 45, 0, new LootFunction[0], new LootCondition[0], "correlated:processor"));
		}
	}
	
	@SubscribeEvent
	public void onRespawn(PlayerRespawnEvent e) {
		CorrelatedWorldData d = CorrelatedWorldData.getFor(e.player.world);
		UUID id = e.player.getGameProfile().getId();
		if (d.getPlayerRespawnData().containsKey(id)) {
			e.player.readFromNBT(d.getPlayerRespawnData().remove(id));
		}
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load e) {
		if (!e.getWorld().isRemote) {
			e.getWorld().addEventListener(new WorldEventListener());
		}
	}

}


