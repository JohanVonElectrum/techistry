package com.koralix.techistry.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TechistryCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TechistryCore.getInstance().forEachPlugin(plugin -> plugin.initializer().onClientInitialize());
    }
}
