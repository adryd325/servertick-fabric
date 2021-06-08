package net.cavoj.servertick;

import net.cavoj.servertick.extensions.MinecraftServerWithST;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerTick implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(NetworkC2S.PACKET_ENABLED, this::processTogglePacket);
        ServerTickEvents.END_SERVER_TICK.register((minecraftServer -> {
            ((MinecraftServerWithST)minecraftServer).tickST();
        }));
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            // TODO Load server config
        }
    }

    private boolean checkPlayerPrivilege(PlayerEntity player) {
        // TODO check server config
        return (player.getServer() != null && !player.getServer().isDedicated()) ||
               player.hasPermissionLevel(4);
    }

    private void processTogglePacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        boolean state = buf.readBoolean();
        server.execute(() -> {
            MinecraftServerWithST serverST = (MinecraftServerWithST)server;
            if (state) {
                if (checkPlayerPrivilege(player)) {
                    serverST.registerSTListener(player);
                }
            } else {
                serverST.removeSTListener(player);
            }
        });
    }
}
