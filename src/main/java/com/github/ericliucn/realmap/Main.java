package com.github.ericliucn.realmap;

import com.github.ericliucn.realmap.commands.CommandManager;
import com.github.ericliucn.realmap.config.DataManager;
import com.github.ericliucn.realmap.utils.Utils;
import com.google.inject.Inject;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.io.IOException;

@Plugin(
        id = "realmap",
        name = "RealMap",
        description = "Display an image on your map !",
        authors = {
                "EricLiu"
        },
        version = "1.0"
)
public class Main {

    @Inject private PluginContainer pluginContainer;
    @Inject private Logger logger;
    @Inject @ConfigDir(sharedRoot = false) private File file;

    public static Main INSTANCE;
    private DataManager dataManager;
    private CommandManager commandManager;


    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException {

        //init plugin instance
        INSTANCE = this;

        this.dataManager = new DataManager(file);

        //register commands
        this.commandManager = new CommandManager();


    }

    @Listener
    public void reloadEvent(GameReloadEvent event) throws IOException {
        this.dataManager = new DataManager(file);
    }


    public Logger getLogger() {
        return logger;
    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
