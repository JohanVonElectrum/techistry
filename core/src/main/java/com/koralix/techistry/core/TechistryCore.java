package com.koralix.techistry.core;

import com.koralix.techistry.api.TechistryInitializer;
import com.koralix.techistry.api.plugins.PluginContainer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class TechistryCore implements ModInitializer {
    private static TechistryCore INSTANCE = null;

    private final Map<String, PluginContainer> plugins = new HashMap<>();

    public static TechistryCore getInstance() {
        return INSTANCE;
    }

    @Override
    public void onInitialize() {
        INSTANCE = this;

        FabricLoader.getInstance().getEntrypointContainers("techistry", TechistryInitializer.class)
                .forEach(this::register);
    }

    private void register(EntrypointContainer<TechistryInitializer> entrypointContainer) {
        ModContainer container = entrypointContainer.getProvider();
        String modId = container.getMetadata().getId();
        TechistryInitializer initializer = entrypointContainer.getEntrypoint();

        PluginContainer pluginContainer = new PluginContainer(container, initializer);
        plugins.put(modId, pluginContainer);

        initializer.onInitialize();
    }

    public Optional<PluginContainer> getPlugin(String modId) {
        return Optional.ofNullable(plugins.get(modId));
    }

    public void forEachPlugin(Consumer<PluginContainer> consumer) {
        plugins.values().forEach(consumer);
    }
}
