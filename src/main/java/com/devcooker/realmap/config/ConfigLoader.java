package com.devcooker.realmap.config;

import com.devcooker.realmap.Main;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigLoader {

    private final HoconConfigurationLoader loader;
    private CommentedConfigurationNode node;
    public static ConfigLoader instance;
    private RConfig config;

    public ConfigLoader(Path configDir) throws IOException {
        instance = this;
        if (!Files.exists(configDir)){
            Files.createDirectory(configDir);
        }
        Path imageDir = configDir.resolve("Images");
        if (!Files.exists(imageDir)){
            Files.createDirectory(imageDir);
        }
        Path configFile = configDir.resolve("realmap.conf");
        loader = HoconConfigurationLoader
                .builder()
                .path(configFile)
                .defaultOptions(ConfigurationOptions.defaults().shouldCopyDefaults(true))
                .build();

        this.load();
        this.save();

//        // copy spongie.png
//        if (!Files.exists(imageDir.resolve("spongie.png"))) {
//            Main.instance.container.openResource(URI.create("spongie.png")).ifPresent(inputStream -> {
//                try {
//                    Files.copy(inputStream, imageDir);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        if (!Files.exists(imageDir.resolve("cat.gif"))) {
//            Main.instance.container.openResource(URI.create("cat.gif")).ifPresent(inputStream -> {
//                try {
//                    Files.copy(inputStream, imageDir);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
    }

    public void load() throws ConfigurateException {
        node = loader.load();
        config = node.get(TypeToken.get(RConfig.class), new RConfig());
    }

    public void save() throws ConfigurateException {
        this.loader.save(this.node);
    }

    public RConfig getConfig() {
        return config;
    }
}
