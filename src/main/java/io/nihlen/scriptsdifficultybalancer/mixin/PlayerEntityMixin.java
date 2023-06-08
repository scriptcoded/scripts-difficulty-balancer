package io.nihlen.scriptsdifficultybalancer.mixin;

import com.mojang.authlib.GameProfile;
import io.nihlen.scriptsdifficultybalancer.ItemStackExt;
import io.nihlen.scriptsdifficultybalancer.ScriptsDifficultyBalancerMod;
import io.nihlen.scriptsdifficultybalancer.state.ServerState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
abstract public class PlayerEntityMixin extends LivingEntity {
	public PlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(EntityType.PLAYER, world);
	}

	@Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
	private boolean dropInventoryOverride(GameRules gameRules, GameRules.Key<GameRules.BooleanRule> rule) {
		var serverState = ServerState.getServerState((ServerPlayerEntity)(Object)this);
		var playerState = serverState.getPlayerState((ServerPlayerEntity)(Object)this);

		if (playerState.keepInventory) {
			return true;
		}

		return this.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
	}

	@Redirect(method = "getXpToDrop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
	private boolean getXpToDropOverride(GameRules gameRules, GameRules.Key<GameRules.BooleanRule> rule) {
		var serverState = ServerState.getServerState((ServerPlayerEntity)(Object)this);
		var playerState = serverState.getPlayerState((ServerPlayerEntity)(Object)this);

		if (playerState.keepXp) {
			return true;
		}

		return this.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
	}

	@Inject(method = "isBlockBreakingRestricted", at = @At("RETURN"), cancellable = true)
	public void isBlockBreakingRestrictedOverride(World world, BlockPos pos, GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
		var mainHandStack = (ItemStackExt)(Object)this.getMainHandStack();
		var offHandStack = (ItemStackExt)(Object)this.getOffHandStack();

		boolean prevented = mainHandStack.isBreakPrevented() || offHandStack.isBreakPrevented();

		if (prevented) {
			cir.setReturnValue(true);
		}
	}
}
