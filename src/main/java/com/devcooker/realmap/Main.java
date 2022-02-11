package com.devcooker.realmap;

import com.devcooker.realmap.command.Commands;
import com.devcooker.realmap.config.ConfigLoader;
import com.devcooker.realmap.handler.MapUpdateTaskHandler;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

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
        new ConfigLoader(path);
    }

    @Listener
    public void onLoaded(final StartedEngineEvent<Server> event){
        new MapUpdateTaskHandler();
    }



    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event){

        final Command.Parameterized command = Command.builder()
                .addChild(Commands.create, "create")
                .addChild(Commands.give, "give")
                .addChild(Commands.toggle, "toggle")
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
