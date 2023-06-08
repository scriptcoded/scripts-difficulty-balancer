package io.nihlen.scriptsdifficultybalancer;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.nihlen.scriptsdifficultybalancer.state.ServerState;
import net.fabricmc.api.ModInitializer;
import static net.minecraft.server.command.CommandManager.*;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptsDifficultyBalancerMod implements ModInitializer {
	public static final String MODID = "scripts-difficulty-balancer";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		//LOGGER.info("Hello Fabric world!");

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
			literal("balance")
				.requires(source -> source.hasPermissionLevel(2))
				.then(argument("player", EntityArgumentType.player())
					.then(literal("keepInventory")
						.then(argument("enabled", BoolArgumentType.bool())
							.executes(context -> executeToggle(context, EntityArgumentType.getPlayer(context, "player"), BoolArgumentType.getBool(context, "enabled"), "keepInventory"))
						)
					)
					.then(literal("keepInventory")
							.executes(context -> queryToggle(context, EntityArgumentType.getPlayer(context, "player"), "keepInventory"))
					)
					.then(literal("keepXp")
						.then(argument("enabled", BoolArgumentType.bool())
							.executes(context -> executeToggle(context, EntityArgumentType.getPlayer(context, "player"), BoolArgumentType.getBool(context, "enabled"), "keepXp"))
						)
					)
					.then(literal("keepXp")
							.executes(context -> queryToggle(context, EntityArgumentType.getPlayer(context, "player"), "keepXp"))
					)
					.then(literal("preventBreakingNamedTools")
							.then(argument("enabled", BoolArgumentType.bool())
									.executes(context -> executeToggle(context, EntityArgumentType.getPlayer(context, "player"), BoolArgumentType.getBool(context, "enabled"), "preventBreakingNamedTools"))
							)
					)
					.then(literal("preventBreakingNamedTools")
							.executes(context -> queryToggle(context, EntityArgumentType.getPlayer(context, "player"), "preventBreakingNamedTools"))
					)
				)
				.executes(context -> {
					// For versions below 1.19, replace "Text.literal" with "new LiteralText".
					context.getSource().sendMessage(Text.literal("Called /balance with no arguments"));

					return 1;
				})
		));
	}

	private static int executeToggle(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, boolean enabled, String name) {
		var serverState = ServerState.getServerState(player);
		var playerState = serverState.getPlayerState(player);

		switch (name) {
			case "keepInventory":
				playerState.keepInventory = enabled;
				break;
			case "keepXp":
				playerState.keepXp = enabled;
				break;
			case "preventBreakingNamedTools":
				playerState.preventBreakingNamedTools = enabled;
				break;
			default:
				ctx.getSource().sendError(Text.literal("Option " + name + " not configurable."));
				break;
		}

		ctx.getSource().sendFeedback(Text.literal("Set " + name + " to " + enabled + " for " + player.getName().getString()), true);

		serverState.markDirty();

		return 1;
	}

	private static int queryToggle(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, String name) {
		var serverState = ServerState.getServerState(player);
		var playerState = serverState.getPlayerState(player);

		switch (name) {
			case "keepInventory":
				ctx.getSource().sendMessage(Text.literal(name + " for " + player.getName().getString() + ": " + playerState.keepInventory));
				break;
			case "keepXp":
				ctx.getSource().sendMessage(Text.literal(name + " for " + player.getName().getString() + ": " + playerState.keepXp));
				break;
			case "preventBreakingNamedTools":
				ctx.getSource().sendMessage(Text.literal(name + " for " + player.getName().getString() + ": " + playerState.preventBreakingNamedTools));
				break;
			default:
				ctx.getSource().sendError(Text.literal("Option " + name + " not configurable."));
				break;
		}

		serverState.markDirty();

		return 1;
	}
}