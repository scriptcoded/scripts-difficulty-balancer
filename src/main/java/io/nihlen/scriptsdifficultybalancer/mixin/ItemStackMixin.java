package io.nihlen.scriptsdifficultybalancer.mixin;

import io.nihlen.scriptsdifficultybalancer.ItemStackExt;
import io.nihlen.scriptsdifficultybalancer.ScriptsDifficultyBalancerMod;
import io.nihlen.scriptsdifficultybalancer.state.ServerState;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExt {

	private Entity owner;


	public boolean isBreakPrevented () {
		if (owner == null) return false;
		if (!(owner instanceof PlayerEntity)) return false;

		var serverState = ServerState.getServerState((PlayerEntity)owner);
		var playerState = serverState.getPlayerState((PlayerEntity)owner);

		if (!playerState.preventBreakingNamedTools) return false;

		var itemStack = (ItemStack)(Object)this;

		if (!itemStack.hasCustomName()) return false;

		var damage = itemStack.getDamage();
		var maxDamage = itemStack.getMaxDamage();
		var isAboutToBreak = damage == maxDamage - 1;

		return isAboutToBreak;
	}

	@Inject(method = "inventoryTick", at = @At("HEAD"))
	public void inventoryTick(World world, Entity entity, int slot, boolean selected, CallbackInfo callbackInfo) {
		owner = entity;
	}

	@Inject(method = "isDamageable", at = @At("RETURN"), cancellable = true)
	public void isDamageable(CallbackInfoReturnable<Boolean> cir) {
		if (isBreakPrevented()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "isItemEnabled", at = @At("RETURN"), cancellable = true)
	public void isItemEnabled(FeatureSet enabledFeatures, CallbackInfoReturnable<Boolean> cir) {
		if (isBreakPrevented()) {
			cir.setReturnValue(false);
		}
	}
}
