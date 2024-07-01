package com.koralix.techistry.api.plugins;

import com.koralix.techistry.api.TechistryInitializer;
import net.fabricmc.loader.api.ModContainer;

public record PluginContainer(ModContainer modContainer, TechistryInitializer initializer) {
}
