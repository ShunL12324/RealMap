package com.github.ericliucn.realmap;

import com.github.ericliucn.realmap.commands.Base;
import com.github.ericliucn.realmap.config.ColorData;
import com.github.ericliucn.realmap.config.Message;
import com.google.inject.Inject;
import org.bstats.sponge.Metrics2;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.metric.MetricsConfigManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Plugin(
        id = "realmap",
        name = "RealMap",
        description = "Display an image on your map !",
        authors = {
                "EricLiu"
        },
        version = "0.1"
)
public class Main {

    @Inject private PluginContainer pluginContainer;
    @Inject private Logger logger;
    @Inject @ConfigDir(sharedRoot = false) private File file;
    private static Main INSTANCE;

    public static File getFile() {
        return INSTANCE.file;
    }
    public static PluginContainer getPluginContainer() {
        return INSTANCE.pluginContainer;
    }
    public static Main getINSTANCE() {
        return INSTANCE;
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException {

        //init plugin instance
        INSTANCE = this;

        //init colorMap and create Images Dir
        ColorData.setColorMap();
        createImagesDir(file);

        //setup config file
        try {
            Message.copyMessageFile(file);
        }catch (IOException e){
            e.printStackTrace();
        }

        //register commands
        Sponge.getCommandManager().register(this, Base.build(),"realmap");
    }

    private static void createImagesDir(File file) throws IOException {
        File images = new File(file,"Images");
        if (!images.exists()){
            images.mkdir();
        }

        File spongie = new File(images,"spongie.png");

        if (!spongie.exists()){
            Asset asset = getPluginContainer().getAsset("spongie.png").get();
            asset.copyToDirectory(Paths.get(images.getPath()));
        }
    }
}
