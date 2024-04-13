package io.nihlen.scriptsdifficultybalancer.mixin;

import io.nihlen.scriptsdifficultybalancer.ItemStackExt;
import io.nihlen.scriptsdifficultybalancer.state.ServerState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExt {

	@Shadow public abstract int getDamage();

	@Shadow public abstract boolean isDamageable();

	@Unique
	private Entity owner;


	public boolean scripts_difficulty_balancer$isBreakPrevented() {
		if (owner == null) return false;
		if (!(owner instanceof PlayerEntity)) return false;

		var serverState = ServerState.getServerState((PlayerEntity)owner);
		if (serverState == null) return false;

		var playerState = serverState.getPlayerState((PlayerEntity)owner);

		if (!playerState.preventBreakingNamedTools) return false;

		var itemStack = (ItemStack)(Object)this;

		if (!itemStack.hasCustomName()) return false;

		var damage = itemStack.getDamage();
		var maxDamage = itemStack.getMaxDamage();

		return damage == maxDamage - 1;
	}

	@Inject(method = "inventoryTick", at = @At("HEAD"))
	public void inventoryTickInject(World world, Entity entity, int slot, boolean selected, CallbackInfo callbackInfo) {
		owner = entity;
	}

	@Inject(method = "isDamageable", at = @At("RETURN"), cancellable = true)
	public void isDamageableInject(CallbackInfoReturnable<Boolean> cir) {
		if (scripts_difficulty_balancer$isBreakPrevented()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "isDamaged", at = @At("RETURN"), cancellable = true)
	public void isDamagedInject(CallbackInfoReturnable<Boolean> cir) {
		if (scripts_difficulty_balancer$isBreakPrevented()) {
			cir.setReturnValue(this.getDamage() > 0);
		} else {
			cir.setReturnValue(this.isDamageable() && this.getDamage() > 0);
		}
	}

	@Inject(method = "isItemEnabled", at = @At("RETURN"), cancellable = true)
	public void isItemEnabledInject(FeatureSet enabledFeatures, CallbackInfoReturnable<Boolean> cir) {
		if (scripts_difficulty_balancer$isBreakPrevented()) {
			cir.setReturnValue(false);
		}
	}
}
