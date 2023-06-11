package io.nihlen.scriptsdifficultybalancer;

import io.nihlen.scriptsdifficultybalancer.gui.PlayerListGui;
import io.nihlen.scriptsdifficultybalancer.gui.PlayerListScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class ScriptsDifficultyBalancerModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ScriptsDifficultyBalancerNetworkingConstants.OPEN_BALANCE_GUI, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                client.setScreen(new PlayerListScreen(new PlayerListGui()));
            });
        });
    }
}
