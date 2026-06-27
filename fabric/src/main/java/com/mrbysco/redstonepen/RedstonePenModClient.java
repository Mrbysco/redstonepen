package com.mrbysco.redstonepen;

import com.mrbysco.redstonepen.blocks.ControlBox;
import com.mrbysco.redstonepen.blocks.RedstoneTrack;
import com.mrbysco.redstonepen.detail.ModRenderers;
import com.mrbysco.redstonepen.libmc.Overlay;
import com.mrbysco.redstonepen.libmc.Registries;
import com.mrbysco.redstonepen.network.FabricNetworkingClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RedstonePenModClient implements ClientModInitializer {
	public RedstonePenModClient() {
		ModelLoadingPlugin.register(pluginContext -> ModRenderers.TrackTer.registerModels().forEach(pluginContext::addModels));
	}

	@Override
	public void onInitializeClient() {
		FabricNetworkingClient.clientInit(Constants.MOD_ID);
		Overlay.register();
		registerMenuGuis();
		registerBlockEntityRenderers();
		processContentClientSide();
		Overlay.on_config(
				0.75,
				0x00ffaa00,
				0x55333333,
				0x55333333,
				0x55444444
		);

		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, ignored) -> {
			Overlay.TextOverlayGui.INSTANCE.onRenderWorldOverlay(context.matrixStack(), context.tickCounter().getRealtimeDeltaTicks());
			return true;
		});
		if (com.mrbysco.redstonepen.detail.RcaSync.ClientRca.init()) {
			ClientTickEvents.END_CLIENT_TICK.register(RedstonePenModClient::onPlayerTickEvent);
		}
	}

	// ----------------------------------------------------------------------------------------------------------------

	private static void onPlayerTickEvent(final net.minecraft.client.Minecraft mc) {
		if ((mc.level == null) || (mc.level.getGameTime() & 0x1) != 0) return;
		com.mrbysco.redstonepen.detail.RcaSync.ClientRca.tick();
	}

	@SuppressWarnings("unchecked")
	private static void registerBlockEntityRenderers() {
		net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
				(BlockEntityType<RedstoneTrack.TrackBlockEntity>) Registries.getBlockEntityTypeOfBlock("track"),
				com.mrbysco.redstonepen.detail.ModRenderers.TrackTer::new
		);
	}

	@SuppressWarnings("unchecked")
	private static void registerMenuGuis() {
		MenuScreens.register((MenuType<ControlBox.ControlBoxUiContainer>) Registries.getMenuTypeOfBlock("control_box"), ControlBox.ControlBoxGui::new);
	}

	private static void processContentClientSide() {
		BlockRenderLayerMap.INSTANCE.putBlock(ModContent.references.TRACK_BLOCK, RenderType.cutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModContent.references.BASIC_GAUGE_BLOCK, RenderType.translucent());
	}
}
