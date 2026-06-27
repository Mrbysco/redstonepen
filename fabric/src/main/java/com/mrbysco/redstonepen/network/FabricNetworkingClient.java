/*
 * @file Networking.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Main client/server message handling.
 */
package com.mrbysco.redstonepen.network;

import com.mrbysco.redstonepen.libmc.Auxiliaries;
import com.mrbysco.redstonepen.libmc.Networking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


@Environment(EnvType.CLIENT)
public class FabricNetworkingClient {
	public static void clientInit(String modid) {
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(Networking.UnifiedPayload.getTYPE(), (unifed_payload, context) -> {
			final LocalPlayer player = context.player();
			final Level world = player.level();
			final CompoundTag payload = unifed_payload.data().nbt();
			context.client().execute(() -> {
				switch (unifed_payload.data().id()) {
					case Networking.PacketTileNotifyServerToClient.PACKET_ID -> {
						final BlockPos pos = BlockPos.of(payload.getLong("pos"));
						final CompoundTag nbt = payload.getCompound("nbt");
						final BlockEntity te = world.getBlockEntity(pos);
						if (!(te instanceof Networking.IPacketTileNotifyReceiver nte)) return;
						nte.onServerPacketReceived(nbt);
					}
					case Networking.PacketContainerSyncServerToClient.PACKET_ID -> {
						final int container_id = payload.getInt("cid");
						final CompoundTag nbt = payload.getCompound("nbt");
						if (!(player.containerMenu instanceof Networking.INetworkSynchronisableContainer nsc)) return;
						if (player.containerMenu.containerId != container_id) return;
						nsc.onServerPacketReceived(container_id, nbt);
					}
					case Networking.PacketNbtNotifyServerToClient.PACKET_ID -> {
						final String hnd = payload.getString("hnd");
						final CompoundTag nbt = payload.getCompound("nbt");
						if (hnd.isEmpty() || (!Networking.PacketNbtNotifyServerToClient.handlers.containsKey(hnd)))
							return;
						context.client().execute(() -> Networking.PacketNbtNotifyServerToClient.handlers.get(hnd).accept(nbt));
					}
					case Networking.OverlayTextMessage.PACKET_ID -> {
						if (Networking.OverlayTextMessage.handler_ == null) return;
						final int delay = payload.getInt("delay");
						if (delay <= 0) return;
						final String deserialized = payload.getString("msg");
						Component m;
						try {
							m = Auxiliaries.unserializeTextComponent(deserialized, world.registryAccess());
						} catch (Throwable e) {
							m = Component.translatable("[incorrect translation]");
						}
						final Component message = m;
						context.client().execute(() -> Networking.OverlayTextMessage.handler_.accept(message, delay));
					}
				}
			});
		});
	}

}
