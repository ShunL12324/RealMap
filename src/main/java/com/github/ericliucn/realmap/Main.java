package com.github.ericliucn.realmap;

import com.github.ericliucn.realmap.command.Commands;
import com.github.ericliucn.realmap.handler.MapUpdateTaskHandler;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Plugin("realmap")
public class Main {

    public static Main instance;
    public final Logger logger;
    public final PluginContainer container;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path path;

    public static Key<Value<String>> MAP_CREATOR;
    public static Key<Value<String>> MAP_NAME;
    public static Key<ListValue<MapCanvas>> MAP_FRAMES;
    public static Key<Value<Integer>> MAP_CURRENT_FRAME;

    @Inject
    public Main(final PluginContainer container, final Logger logger){
        instance = this;
        this.logger = logger;
        this.container = container;
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) throws IOException {
        if (!Files.exists(path)) Files.createDirectory(path);
        if (!Files.exists(path.resolve("Images"))) Files.createDirectory(path.resolve("Images"));
        Sponge.assetManager().asset("spongie.png").ifPresent(asset -> {
            try {
                asset.copyToDirectory(path.resolve("Images"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Listener
    public void onLoaded(final StartedEngineEvent<Server> event){
        new MapUpdateTaskHandler();
    }



    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event){

        final Command.Parameterized command = Command.builder()
                .addChild(Commands.create, "create")
                .addChild(Commands.test, "test")
                .executor(context -> CommandResult.success())
                .build();
        event.register(container, command, "realmap");
    }

    @Listener
    public void onRegisterData(final RegisterDataEvent event){
        MAP_NAME = Key.from(this.container, "map_name", String.class);
        event.register(DataRegistration.of(MAP_NAME, MapInfo.class));
        MAP_CREATOR = Key.from(this.container, "map_creator", String.class);
        event.register(DataRegistration.of(MAP_CREATOR, MapInfo.class));
        MAP_FRAMES = Key.fromList(this.container, "map_frames", MapCanvas.class);
        event.register(DataRegistration.of(MAP_FRAMES, MapInfo.class));
        MAP_CURRENT_FRAME = Key.from(this.container, "map_current_frame", Integer.class);
        event.register(DataRegistration.of(MAP_CURRENT_FRAME, MapInfo.class));
    }



    public Path getPath() {
        return path;
    }

}
