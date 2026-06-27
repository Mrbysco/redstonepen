/*
 * @file Networking.java
 * @author Stefan Wilhelm (wile)
 * @copyright (C) 2020 Stefan Wilhelm
 * @license MIT (see https://opensource.org/licenses/MIT)
 *
 * Main client/server message handling.
 */
package com.mrbysco.redstonepen.libmc;

import com.mrbysco.redstonepen.Constants;
import com.mrbysco.redstonepen.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class Networking {

	//--------------------------------------------------------------------------------------------------------------------
	// Unified Packet Handling
	//--------------------------------------------------------------------------------------------------------------------

	public record UnifiedPayload(UnifiedData data) implements CustomPacketPayload {
		public static final StreamCodec<FriendlyByteBuf, UnifiedPayload> STREAM_CODEC = CustomPacketPayload.codec(UnifiedPayload::write, UnifiedPayload::new);
		public static final Type<UnifiedPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "unpnbt"));

		public static Type<UnifiedPayload> getTYPE() {
			return TYPE;
		}

		private UnifiedPayload(FriendlyByteBuf buf) {
			this(new UnifiedData(buf.readUtf(), buf.readNbt()));
		}

		private void write(FriendlyByteBuf buf) {
			data.write(buf);
		}

		public Type<UnifiedPayload> type() {
			return TYPE;
		}

		public record UnifiedData(String id, CompoundTag nbt) {
			public UnifiedData(FriendlyByteBuf buf) {
				this(buf.readUtf(), buf.readNbt());
			}

			public void write(FriendlyByteBuf buf) {
				buf.writeUtf(id);
				buf.writeNbt(nbt);
			}

			@Override
			public String toString() {
				return id + ": " + nbt.toString();
			}
		}
	}

	//--------------------------------------------------------------------------------------------------------------------
	// Tile entity notifications
	//--------------------------------------------------------------------------------------------------------------------

	public interface IPacketTileNotifyReceiver {
		default void onServerPacketReceived(CompoundTag nbt) {
		}

		default void onClientPacketReceived(Player player, CompoundTag nbt) {
		}
	}

	public static class PacketTileNotifyClientToServer {
		public static final String PACKET_ID = "tnc2s";
	}

	public static class PacketTileNotifyServerToClient {
		public static final String PACKET_ID = "tns2c";

		public static void sendToPlayer(ServerPlayer player, BlockEntity te, CompoundTag nbt) {
			if ((te == null) || (nbt == null)) return;
			final CompoundTag payload = new CompoundTag();
			payload.putLong("pos", te.getBlockPos().asLong());
			payload.put("nbt", nbt);
			Services.PLATFORM.sendToClient(player, PACKET_ID, payload);
		}

		public static void sendToPlayers(BlockEntity te, CompoundTag nbt) {
			if ((te == null) || (!(te.getLevel() instanceof ServerLevel sworld))) return;
			final CompoundTag payload = new CompoundTag();
			payload.putLong("pos", te.getBlockPos().asLong());
			payload.put("nbt", nbt);
			Services.PLATFORM.sendToClients(sworld, PACKET_ID, payload);
		}
	}

	//--------------------------------------------------------------------------------------------------------------------
	// (GUI) Container synchronization
	//--------------------------------------------------------------------------------------------------------------------

	public interface INetworkSynchronisableContainer {
		void onServerPacketReceived(int windowId, CompoundTag nbt);

		void onClientPacketReceived(int windowId, Player player, CompoundTag nbt);
	}

	public static class PacketContainerSyncClientToServer {
		public static final String PACKET_ID = "csc2s";
	}

	public static class PacketContainerSyncServerToClient {
		public static final String PACKET_ID = "css2c";

		public static void sendToPlayer(ServerPlayer player, int windowId, CompoundTag nbt) {
			if (nbt == null || player == null) return;
			final CompoundTag payload = new CompoundTag();
			payload.putInt("cid", windowId);
			payload.put("nbt", nbt);
			Services.PLATFORM.sendToClient(player, PACKET_ID, payload);
		}

		public static void sendToPlayer(ServerPlayer player, AbstractContainerMenu container, CompoundTag nbt) {
			if (container != null) sendToPlayer(player, container.containerId, nbt);
		}

		public static <C extends AbstractContainerMenu & INetworkSynchronisableContainer>
		void sendToListeners(Level world, C container, CompoundTag nbt) {
			for (Player player : world.players()) {
				if (player.containerMenu.containerId != container.containerId) continue;
				sendToPlayer((ServerPlayer) player, container.containerId, nbt);
			}
		}
	}

	//--------------------------------------------------------------------------------------------------------------------
	// World notifications
	//--------------------------------------------------------------------------------------------------------------------

	public static class PacketNbtNotifyClientToServer {
		public static final String PACKET_ID = "nnc2s";
		public static final Map<String, BiConsumer<Player, CompoundTag>> handlers = new HashMap<>();
	}

	public static class PacketNbtNotifyServerToClient {
		public static final String PACKET_ID = "nns2c";
		public static final Map<String, Consumer<CompoundTag>> handlers = new HashMap<>();

		public static void sendToPlayer(Player player, CompoundTag nbt) {
			if ((nbt == null) || (!(player instanceof ServerPlayer splayer))) return;
			Services.PLATFORM.sendToClient(splayer, PACKET_ID, nbt);
		}

		public static void sendToPlayers(Level world, String handler, CompoundTag nbt) {
			if (world != null) for (Player player : world.players()) sendToPlayer(player, nbt);
		}
	}

	//--------------------------------------------------------------------------------------------------------------------
	// Main window GUI text message
	//--------------------------------------------------------------------------------------------------------------------

	public static class OverlayTextMessage {
		public static final String PACKET_ID = "otms2c";
		public static BiConsumer<Component, Integer> handler_ = null;
		public static final int DISPLAY_TIME_MS = 3000;

		public static void setHandler(BiConsumer<Component, Integer> handler) {
			if (handler_ == null) handler_ = handler;
		}

		public static void sendToPlayer(ServerPlayer player, Component message) {
			sendToPlayer(player, message, DISPLAY_TIME_MS);
		}

		public static void sendToPlayer(ServerPlayer player, Component message, int delay) {
			if (Auxiliaries.isEmpty(message)) return;
			try {
				final CompoundTag payload = new CompoundTag();
				payload.putInt("delay", delay);
				payload.putString("msg", Auxiliaries.serializeTextComponent(message, player.registryAccess()));
				Services.PLATFORM.sendToClient(player, PACKET_ID, payload);
			} catch (Throwable e) {
				Auxiliaries.logger().error("OverlayTextMessage.toBytes() failed: {}", String.valueOf(e));
			}
		}
	}

}
