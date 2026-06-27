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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


public class NeoNetworking {
	public static void init(net.neoforged.neoforge.network.registration.PayloadRegistrar registrar) {
		registrar.playBidirectional(Networking.UnifiedPayload.TYPE, Networking.UnifiedPayload.STREAM_CODEC, (unifed_payload, context) -> {
			if (context.player() instanceof ServerPlayer player) {
				//final ServerPlayer player = (ServerPlayer)context.player();
				final ServerLevel world = player.serverLevel();
				if (player == null) return;
				final CompoundTag payload = unifed_payload.data().nbt();
				player.server.execute(() -> {
					switch (unifed_payload.data().id()) {
						case Networking.PacketTileNotifyClientToServer.PACKET_ID -> {
							final BlockPos pos = BlockPos.of(payload.getLong("pos"));
							final CompoundTag nbt = payload.getCompound("nbt");
							final BlockEntity te = world.getBlockEntity(pos);
							if (!(te instanceof Networking.IPacketTileNotifyReceiver)) return;
							((Networking.IPacketTileNotifyReceiver) te).onClientPacketReceived(player, nbt);
						}
						case Networking.PacketContainerSyncClientToServer.PACKET_ID -> {
							final int container_id = payload.getInt("cid");
							final CompoundTag nbt = payload.getCompound("nbt");
							if (!(player.containerMenu instanceof Networking.INetworkSynchronisableContainer nsc)) return;
							if (player.containerMenu.containerId != container_id) return;
							nsc.onClientPacketReceived(container_id, player, nbt);
						}
						case Networking.PacketNbtNotifyClientToServer.PACKET_ID -> {
							final String hnd = payload.getString("hnd");
							final CompoundTag nbt = payload.getCompound("nbt");
							if (hnd.isEmpty() || (!Networking.PacketNbtNotifyClientToServer.handlers.containsKey(hnd))) return;
							Networking.PacketNbtNotifyClientToServer.handlers.get(hnd).accept(player, nbt);
						}
					}
				});
			} else {
				final LocalPlayer player = (LocalPlayer) context.player();
				final Level level = player.level();
				final CompoundTag payload = unifed_payload.data().nbt();
				context.enqueueWork(() -> {
					switch (unifed_payload.data().id()) {
						case Networking.PacketTileNotifyServerToClient.PACKET_ID -> {
							final BlockPos pos = BlockPos.of(payload.getLong("pos"));
							final CompoundTag nbt = payload.getCompound("nbt");
							final BlockEntity te = level.getBlockEntity(pos);
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
							if (hnd.isEmpty() || (!Networking.PacketNbtNotifyServerToClient.handlers.containsKey(hnd))) return;
							Networking.PacketNbtNotifyServerToClient.handlers.get(hnd).accept(nbt);
						}
						case Networking.OverlayTextMessage.PACKET_ID -> {
							if (Networking.OverlayTextMessage.handler_ == null) return;
							final int delay = payload.getInt("delay");
							if (delay <= 0) return;
							final String deserialized = payload.getString("msg");
							Component m;
							try {
								m = Auxiliaries.unserializeTextComponent(deserialized, level.registryAccess());
							} catch (Throwable e) {
								m = Component.translatable("[incorrect translation]");
							}
							final Component message = m;
							Networking.OverlayTextMessage.handler_.accept(message, delay);
						}
					}
				});
			}
		});
	}

}
