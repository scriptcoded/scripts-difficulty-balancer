package io.nihlen.scriptsdifficultybalancer.state;

import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class PlayerState {
    public boolean keepInventory = false;
    public boolean keepXp = false;
    public boolean preventBreakingNamedTools = false;

    public NbtCompound toNbtCompound() {
        NbtCompound playerStateNbt = new NbtCompound();

        playerStateNbt.putBoolean("keepInventory", this.keepInventory);
        playerStateNbt.putBoolean("keepXp", this.keepXp);
        playerStateNbt.putBoolean("preventBreakingNamedTools", this.preventBreakingNamedTools);

        return playerStateNbt;
    }

    public static PlayerState fromNbtCompound(NbtCompound nbtCompound) {
        PlayerState playerState = new PlayerState();

        playerState.keepInventory = nbtCompound.getBoolean("keepInventory");
        playerState.keepXp = nbtCompound.getBoolean("keepXp");
        playerState.preventBreakingNamedTools = nbtCompound.getBoolean("preventBreakingNamedTools");

        return playerState;
    }
}
