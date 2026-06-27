package com.mrbysco.redstonepen.mixin;

import com.mrbysco.redstonepen.libmc.Overlay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(net.minecraft.client.gui.Gui.class)
abstract public class GuiRenderingMixin {
	@Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
	private void render(net.minecraft.client.gui.GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker, CallbackInfo info) {
		if (Overlay.TextOverlayGui.deadline() < System.currentTimeMillis()) return;
		if (Overlay.TextOverlayGui.text() == Overlay.TextOverlayGui.EMPTY_TEXT) return;
		Overlay.TextOverlayGui.INSTANCE.onRenderGui(graphics);
	}
}
