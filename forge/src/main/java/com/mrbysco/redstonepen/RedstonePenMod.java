package com.mrbysco.redstonepen;

import com.mrbysco.redstonepen.detail.RcaSync;
import com.mrbysco.redstonepen.libmc.Auxiliaries;
import com.mrbysco.redstonepen.libmc.Registries;
import com.mrbysco.redstonepen.network.NeoNetworking;
import com.mrbysco.redstonepen.network.NeoNetworkingClient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class RedstonePenMod {

	public RedstonePenMod(IEventBus eventBus) {
		Auxiliaries.init();
		Auxiliaries.logGitVersion();
		Registries.init();
		ModContent.init();
		eventBus.addListener(LiveCycleEvents::onConstruct);
		eventBus.addListener(LiveCycleEvents::onRegister);
		eventBus.addListener(LiveCycleEvents::onRegisterNetwork);
		CREATIVE_MODE_TABS.register(eventBus);
	}


	// -------------------------------------------------------------------------------------------------------------------
	// Creative Mode Tab
	// -------------------------------------------------------------------------------------------------------------------

	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register(
			"tab_" + Constants.MOD_ID, () -> CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.tabredstonepen"))
					.withTabsBefore(CreativeModeTabs.COMBAT)
					.icon(() -> new ItemStack(Registries.getItem("pen")))
					.displayItems((parameters, output) -> Registries.getRegisteredItems().forEach(
							it -> {
								if (!(it instanceof BlockItem bit) || (bit.getBlock() != ModContent.references.TRACK_BLOCK))
									output.accept(it);
							})
					).build()
	);

	// -------------------------------------------------------------------------------------------------------------------
	// Events
	// -------------------------------------------------------------------------------------------------------------------

	private static class LiveCycleEvents {
		private static void onConstruct(final FMLConstructModEvent event) {
			RcaSync.CommonRca.init();
		}

		private static void onRegister(RegisterEvent event) {
			final String registry_name = event.getRegistry().key().location().toString();
			if (!registry_name.equals("minecraft:block")) return;
			Registries.instantiateAll();
			ModContent.initReferences();
		}

		private static void onRegisterNetwork(final RegisterPayloadHandlersEvent event) {
			PayloadRegistrar registrar = event.registrar("v1");
			NeoNetworking.init(registrar);
			NeoNetworkingClient.clientInit(registrar);
		}

		private static void onLoadComplete(final FMLLoadCompleteEvent event) {
		}
	}
}