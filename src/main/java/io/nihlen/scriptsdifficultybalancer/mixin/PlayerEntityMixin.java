package io.nihlen.scriptsdifficultybalancer.mixin;

import com.mojang.authlib.GameProfile;
import io.nihlen.scriptsdifficultybalancer.ItemStackExt;
import io.nihlen.scriptsdifficultybalancer.state.ServerState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
abstract public class PlayerEntityMixin extends LivingEntity {
	public PlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(EntityType.PLAYER, world);
	}

	@Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
	private boolean dropInventoryOverride(GameRules gameRules, GameRules.Key<GameRules.BooleanRule> rule) {
		var serverState = ServerState.getServerState(this);
		if (serverState == null) return false;
		var playerState = serverState.getPlayerState(this);

		if (playerState.keepInventory) {
			return true;
		}

		return this.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
	}

	@Redirect(method = "getXpToDrop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
	private boolean getXpToDropOverride(GameRules gameRules, GameRules.Key<GameRules.BooleanRule> rule) {
		var serverState = ServerState.getServerState(this);
		if (serverState == null) return false;
		var playerState = serverState.getPlayerState(this);

		if (playerState.keepXp) {
			return true;
		}

		return this.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
	}

	@Inject(method = "isBlockBreakingRestricted", at = @At("RETURN"), cancellable = true)
	public void isBlockBreakingRestrictedOverride(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
		var mainHandStack = (ItemStackExt)(Object)this.getMainHandStack();
		var offHandStack = (ItemStackExt)(Object)this.getOffHandStack();

		boolean prevented = mainHandStack.scripts_difficulty_balancer$isBreakPrevented() || offHandStack.scripts_difficulty_balancer$isBreakPrevented();

		if (prevented) {
			cir.setReturnValue(true);
		}
	}
}
