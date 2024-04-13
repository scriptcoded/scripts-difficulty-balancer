package io.nihlen.scriptsdifficultybalancer.mixin;

import com.mojang.authlib.GameProfile;
import io.nihlen.scriptsdifficultybalancer.state.ServerState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
abstract public class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(MinecraftServer server, ServerWorld world, GameProfile profile) {
        super(world, world.getSpawnPos(), world.getSpawnAngle(), profile);
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyFromOverride(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo callbackInfo) {
        var serverState = ServerState.getServerState((ServerPlayerEntity)(Object)this);
        if (serverState == null) return;
        var playerState = serverState.getPlayerState((ServerPlayerEntity)(Object)this);

        if (playerState.keepInventory) {
            this.getInventory().clone(oldPlayer.getInventory());
        }

        if (playerState.keepXp) {
            this.experienceLevel = oldPlayer.experienceLevel;
            this.totalExperience = oldPlayer.totalExperience;
            this.experienceProgress = oldPlayer.experienceProgress;
            this.setScore(oldPlayer.getScore());
        }
    }
}
