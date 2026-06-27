package com.mrbysco.redstonepen.platform;

import com.mrbysco.redstonepen.libmc.Networking;
import com.mrbysco.redstonepen.platform.services.IPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;
import java.util.Optional;

public class NeoForgePlatformHelper implements IPlatformHelper {

	@Override
	public Path getGamePath() {
		return null;
	}

	@Override
	public boolean isModLoaded(String modId) {

		return ModList.get().isLoaded(modId);
	}

	@Override
	public Optional<ServerPlayer> getFakePlayer(Level level) {
		if (level.isClientSide()) return Optional.empty();
		var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft((ServerLevel) level);
		return (player == null) ? Optional.empty() : Optional.of(player);
	}

	@Override
	public void sendToClient(ServerPlayer player, String packet_id, CompoundTag payload_nbt) {
		PacketDistributor.sendToPlayer(player, new Networking.UnifiedPayload(new Networking.UnifiedPayload.UnifiedData(packet_id, payload_nbt)));
	}

	@Override
	public void sendToClients(ServerLevel world, String packet_id, CompoundTag payload_nbt) {
		final var payload = new Networking.UnifiedPayload(new Networking.UnifiedPayload.UnifiedData(packet_id, payload_nbt));
		for (ServerPlayer player : world.players()) PacketDistributor.sendToPlayer(player, payload);
	}

	@Override
	public void sendToServer(String packet_id, CompoundTag payload_nbt) {
		PacketDistributor.sendToServer(new Networking.UnifiedPayload(new Networking.UnifiedPayload.UnifiedData(packet_id, payload_nbt)));
	}

}
