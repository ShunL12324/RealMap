package com.github.ericliucn.realmap.config;

import com.github.ericliucn.realmap.Main;
import org.spongepowered.api.asset.Asset;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.Properties;

public class Message {

    private static Properties properties = new Properties();
    private static File message;

    public static void copyMessageFile(File file) throws IOException {

        //create dir if it's not exist
        if (!file.exists()){
            file.mkdir();
        }

        //copy message.properties to dir if it's not exist;
        message = new File(file,"message.properties");
        if (!message.exists()){
            Asset asset = Main.getPluginContainer().getAsset("message.properties").get();
            asset.copyToDirectory(FileSystems.getDefault().getPath(file.getPath()));
        }

        //read the message file
        InputStream inputStream = new FileInputStream(new File(message.getAbsolutePath()));
        properties.load(inputStream);
    }

    //return the String
    public static String getMessage(String key){
        return properties.getProperty(key);
    }

    //reload message.properties
    public static void reloadProperties() throws IOException {
        InputStream inputStream = new FileInputStream(message.getPath());
        properties.load(inputStream);
    }
}
