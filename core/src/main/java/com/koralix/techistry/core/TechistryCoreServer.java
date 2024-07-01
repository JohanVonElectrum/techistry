package com.koralix.techistry.core;

import net.fabricmc.api.DedicatedServerModInitializer;

public class TechistryCoreServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        TechistryCore.getInstance().forEachPlugin(plugin -> plugin.initializer().onServerInitialize());
    }
}
