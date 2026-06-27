package com.mrbysco.redstonepen.platform.services;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.nio.file.Path;
import java.util.Optional;

public interface IPlatformHelper {

	Path getGamePath();

	boolean isModLoaded(String modId);

	Optional<ServerPlayer> getFakePlayer(Level level);

	void sendToClient(ServerPlayer player, String packet_id, CompoundTag payload_nbt);

	void sendToClients(ServerLevel world, String packet_id, CompoundTag payload_nbt);

	void sendToServer(String packet_id, CompoundTag payload_nbt);
}
