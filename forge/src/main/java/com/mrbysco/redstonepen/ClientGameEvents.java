package com.mrbysco.redstonepen;

import com.mrbysco.redstonepen.libmc.Overlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientGameEvents {
	@SubscribeEvent
	public static void onRenderGui(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
		Overlay.TextOverlayGui.INSTANCE.onRenderGui(event.getGuiGraphics());
	}

	@SubscribeEvent
	public static void onRenderWorldOverlay(net.neoforged.neoforge.client.event.RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS) {
			Overlay.TextOverlayGui.INSTANCE.onRenderWorldOverlay(event.getPoseStack(), event.getRenderTick());
		}
	}
}
