package io.nihlen.scriptsdifficultybalancer.state;

import io.nihlen.scriptsdifficultybalancer.ScriptsDifficultyBalancerMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class ServerState extends PersistentState {
    public HashMap<UUID, PlayerState> players = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        // Putting the 'players' hashmap, into the 'nbt' which will be saved.
        NbtCompound playersNbtCompound = new NbtCompound();
        players.forEach((UUID, playerSate) -> {
            var playerStateNbt = playerSate.toNbtCompound();
            playersNbtCompound.put(String.valueOf(UUID), playerStateNbt);
        });
        nbt.put("players", playersNbtCompound);

        return nbt;
    }

    public static ServerState createFromNbt(NbtCompound tag) {
        ServerState serverState = new ServerState();

        // Here we are basically reversing what we did in ''writeNbt'' and putting the data inside the tag back to our hashmap
        NbtCompound playersTag = tag.getCompound("players");
        playersTag.getKeys().forEach(key -> {
            var playerState = PlayerState.fromNbtCompound(playersTag.getCompound(key));
            UUID uuid = UUID.fromString(key);
            serverState.players.put(uuid, playerState);
        });

        return serverState;
    }

    public static ServerState getServerState(LivingEntity player) {
        return getServerState(player.world.getServer());
    }

    public static ServerState getServerState(MinecraftServer server) {
        // First we get the persistentStateManager for the OVERWORLD
        PersistentStateManager persistentStateManager = server
                .getWorld(World.OVERWORLD).getPersistentStateManager();

        // Calling this reads the file from the disk if it exists, or creates a new one and saves it to the disk
        // You need to use a unique string as the key. You should already have a MODID variable defined by you somewhere in your code. Use that.
        ServerState serverState = persistentStateManager.getOrCreate(
                ServerState::createFromNbt,
                ServerState::new,
                ScriptsDifficultyBalancerMod.MODID);

        return serverState;
    }

    public PlayerState getPlayerState(LivingEntity player) {
        // Either get the player by the uuid, or we don't have data for him yet, make a new player state
        PlayerState playerState = this.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerState());

        return playerState;
    }
}
