package com.mrbysco.redstonepen.platform;

import com.mrbysco.redstonepen.libmc.Networking;
import com.mrbysco.redstonepen.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.nio.file.Path;
import java.util.Optional;

public class FabricPlatformHelper implements IPlatformHelper {

	@Override
	public Path getGamePath() {
		return FabricLoader.getInstance().getGameDir();
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public Optional<ServerPlayer> getFakePlayer(Level level) {
		if (level.isClientSide()) return Optional.empty();
		var player = net.fabricmc.fabric.api.entity.FakePlayer.get((ServerLevel) level);
		return (player == null) ? Optional.empty() : Optional.of(player);
	}

	public void sendToClient(ServerPlayer player, String packet_id, CompoundTag payload_nbt) {
		ServerPlayNetworking.send(player, new Networking.UnifiedPayload(new Networking.UnifiedPayload.UnifiedData(packet_id, payload_nbt)));
	}

	public void sendToClients(ServerLevel world, String packet_id, CompoundTag payload_nbt) {
		final var payload = new Networking.UnifiedPayload(new Networking.UnifiedPayload.UnifiedData(packet_id, payload_nbt));
		for (ServerPlayer player : world.players()) ServerPlayNetworking.send(player, payload);
	}

	@Override
	public void sendToServer(String packet_id, CompoundTag payload_nbt) {
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new Networking.UnifiedPayload(new Networking.UnifiedPayload.UnifiedData(packet_id, payload_nbt)));
	}
}
