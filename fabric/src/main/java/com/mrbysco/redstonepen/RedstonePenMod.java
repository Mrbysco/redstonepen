package com.mrbysco.redstonepen;

import com.mrbysco.redstonepen.libmc.Auxiliaries;
import com.mrbysco.redstonepen.libmc.Registries;
import com.mrbysco.redstonepen.network.FabricNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class RedstonePenMod implements ModInitializer {
	public RedstonePenMod() {
		Auxiliaries.init();
		Auxiliaries.logGitVersion();
	}

	@Override
	public void onInitialize() {
		Registries.init();
		FabricNetworking.init();
		ModContent.init();
		ModContent.initReferences();
		com.mrbysco.redstonepen.detail.RcaSync.CommonRca.init();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "creative_tab"), CREATIVE_TAB);
	}
	private static final CreativeModeTab CREATIVE_TAB = FabricItemGroup.builder()
			.title(Component.translatable("itemGroup.tab" + Constants.MOD_ID))
			.icon(()->new ItemStack(Registries.getItem("quill")))
			.displayItems((ctx,reg)-> Registries.getRegisteredItems().forEach(it->{
				if(!(it instanceof BlockItem bit) || (bit.getBlock() != ModContent.references.TRACK_BLOCK)) reg.accept(it);
			}))
			.build();
}
