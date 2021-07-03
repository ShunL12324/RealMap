package com.github.ericliucn.realmap;

import com.github.ericliucn.realmap.utils.Utils;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.awt.image.BufferedImage;
import java.io.IOException;
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

    public Key<Value<String>> MAP_CREATOR;
    public Key<Value<String>> MAP_NAME;

    @Inject
    public Main(final PluginContainer container, final Logger logger){
        instance = this;
        this.logger = logger;
        this.container = container;
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) throws IOException {
        if (!Files.exists(path)) Files.createDirectory(path);
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event){
        final Parameter.Value<String> mapNamePara = Parameter.string().key("mapName").build();
        final Parameter.Value<String> imageNamePara = Parameter.string().key("image").build();
        final Command.Parameterized command = Command.builder()
                .addChild(
                        Command.builder()
                                .addParameter(mapNamePara)
                                .addParameter(imageNamePara)
                                .executor(context -> {
                                    Object root = context.cause().root();
                                    String creatorName = "UNKNOWN";
                                    if (root instanceof SystemSubject){
                                        creatorName = "terminal";
                                    }else if (root instanceof ServerPlayer){
                                        creatorName = ((ServerPlayer) root).name();
                                    }

                                    String mapName = context.one(mapNamePara).get();
                                    String imageName = context.one(imageNamePara).get();

                                    BufferedImage bufferedImage = Utils.getBufferedImage(imageName);
                                    if (bufferedImage == null) return CommandResult.error(Component.text("invalid image address"));
                                    MapCanvas canvas = Utils.getMapCanvas(bufferedImage);
                                    MapInfo mapInfo = Sponge.server().mapStorage().createNewMapInfo().get();
                                    mapInfo.offer(this.MAP_NAME, mapName);
                                    mapInfo.offer(this.MAP_CREATOR, creatorName);
                                    mapInfo.offer(Keys.MAP_CANVAS, canvas);
                                    ItemStack map = ItemStack.of(ItemTypes.FILLED_MAP);
                                    map.offer(Keys.MAP_INFO, mapInfo);
                                    if (root instanceof ServerPlayer){
                                        ((ServerPlayer) root).inventory().offer(map);
                                    }
                                    return CommandResult.success();
                                })
                                .build(), "create")
                .executor(context -> CommandResult.success())
                .build();
        event.register(container, command, "realmap");
    }

    @Listener
    public void onRegisterData(final RegisterDataEvent event){
        final ResourceKey creatorKey = ResourceKey.of(this.container, "map_creator");
        final ResourceKey nameKey = ResourceKey.of(this.container, "map_name");

        this.MAP_CREATOR = Key.builder().key(creatorKey).elementType(String.class).build();
        this.MAP_NAME = Key.builder().key(nameKey).elementType(String.class).build();

        final DataProvider<Value<String>, String> creatorDataProvider = DataProvider.mutableBuilder()
                .key(MAP_CREATOR)
                .dataHolder(MapInfo.class)
                .set(((mapInfo, s) -> mapInfo.offer(this.MAP_CREATOR, s)))
                .get(mapInfo -> mapInfo.get(this.MAP_CREATOR).orElse("UNKNOWN"))
                .delete(mapInfo -> mapInfo.remove(this.MAP_CREATOR))
                .build();

        final DataProvider<Value<String>, String> nameDataProvider = DataProvider.mutableBuilder()
                .key(MAP_NAME)
                .dataHolder(MapInfo.class)
                .set(((mapInfo, s) -> mapInfo.offer(MAP_NAME, s)))
                .get(mapInfo -> mapInfo.get(this.MAP_NAME).orElse("UNKNOWN"))
                .delete(mapInfo -> mapInfo.remove(this.MAP_NAME))
                .build();

        final DataStore creatorDataStore = DataStore.of(this.MAP_CREATOR, DataQuery.of("map_creator"), MapInfo.class);
        final DataStore nameDataStore = DataStore.of(this.MAP_NAME, DataQuery.of("map_name"), MapInfo.class);

        final DataRegistration creatorDataRegistry = DataRegistration.builder()
                .dataKey(this.MAP_CREATOR)
                .store(creatorDataStore)
                .provider(creatorDataProvider)
                .build();

        final DataRegistration nameDataRegistry = DataRegistration.builder()
                .dataKey(this.MAP_NAME)
                .store(nameDataStore)
                .provider(nameDataProvider)
                .build();

        event.register(creatorDataRegistry);
        event.register(nameDataRegistry);
    }

    public Path getPath() {
        return path;
    }
}
