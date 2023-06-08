package io.nihlen.scriptsdifficultybalancer.mixin;

import com.mojang.authlib.GameProfile;
import io.nihlen.scriptsdifficultybalancer.ScriptsDifficultyBalancerMod;
import io.nihlen.scriptsdifficultybalancer.state.ServerState;
import net.minecraft.block.entity.SculkShriekerWarningManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
abstract public class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(MinecraftServer server, ServerWorld world, GameProfile profile) {
        super(world, world.getSpawnPos(), world.getSpawnAngle(), profile);
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyFromOverride(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo callbackInfo) {
        var serverState = ServerState.getServerState((ServerPlayerEntity)(Object)this);
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
