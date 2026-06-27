/*
 * @file Networking.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Main client/server message handling.
 */
package com.mrbysco.redstonepen.network;

import com.mrbysco.redstonepen.libmc.Networking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;


public class FabricNetworking {
	public static void init() {
		PayloadTypeRegistry.playC2S().register(Networking.UnifiedPayload.TYPE, Networking.UnifiedPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(Networking.UnifiedPayload.TYPE, Networking.UnifiedPayload.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(Networking.UnifiedPayload.TYPE, (unifed_payload, context) -> {
			final ServerPlayer player = context.player();
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
						if (hnd.isEmpty() || (!Networking.PacketNbtNotifyClientToServer.handlers.containsKey(hnd)))
							return;
						Networking.PacketNbtNotifyClientToServer.handlers.get(hnd).accept(player, nbt);
					}
				}
			});
		});
	}
}
