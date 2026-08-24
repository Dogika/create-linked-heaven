package com.dogika.lh.group;

import net.minecraft.server.MinecraftServer;

public final class ServerRegistryAccess {

    private static volatile MinecraftServer currentServer;

    private ServerRegistryAccess() {
    }

    public static void setServer(MinecraftServer server) {
        currentServer = server;
    }

    public static void clear() {
        currentServer = null;
    }

    public static LinkGroupRegistry registryOrNull() {
        MinecraftServer server = currentServer;
        return server == null ? null : LinkGroupRegistry.get(server);
    }
}
