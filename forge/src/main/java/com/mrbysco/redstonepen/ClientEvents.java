package com.mrbysco.redstonepen;

import com.mrbysco.redstonepen.blocks.ControlBox;
import com.mrbysco.redstonepen.blocks.RedstoneTrack;
import com.mrbysco.redstonepen.libmc.Networking;
import com.mrbysco.redstonepen.libmc.Overlay;
import com.mrbysco.redstonepen.libmc.Registries;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
	@SubscribeEvent
	@SuppressWarnings({"unchecked"})
	public static void onClientSetup(final FMLClientSetupEvent event) {
		Networking.OverlayTextMessage.setHandler(Overlay.TextOverlayGui::show);
		Overlay.on_config(0.75, 0x00ffaa00, 0x55333333, 0x55333333, 0x55444444);
		BlockEntityRenderers.register((BlockEntityType<RedstoneTrack.TrackBlockEntity>) Registries.getBlockEntityTypeOfBlock("track"), com.mrbysco.redstonepen.detail.ModRenderers.TrackTer::new);
		// Player client tick if RCA existing.
		if (com.mrbysco.redstonepen.detail.RcaSync.ClientRca.init()) {
			NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (final PlayerTickEvent.Post ev) -> com.mrbysco.redstonepen.detail.RcaSync.ClientRca.tick());
		}
	}

	@SubscribeEvent
	@SuppressWarnings({"unchecked"})
	public static void onRegisterMenuScreens(final RegisterMenuScreensEvent event) {
		event.register((MenuType<ControlBox.ControlBoxUiContainer>) Registries.getMenuTypeOfBlock("control_box"), ControlBox.ControlBoxGui::new);
	}

	@SubscribeEvent
	public static void onRegisterModels(final ModelEvent.RegisterAdditional event) {
		com.mrbysco.redstonepen.detail.ModRenderers.TrackTer.registerModels().forEach(event::register);
	}
}
