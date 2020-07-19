package com.github.ericliucn.realmap.config;

import com.github.ericliucn.realmap.Main;
import com.github.ericliucn.realmap.utils.ImageDownloadTask;
import com.github.ericliucn.realmap.utils.Utils;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.FMLServerHandler;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DataManager {

    private final File imgDir;
    private final File msgFile;
    private final File confDir;
    private final Properties properties;
    public static Map<Integer, int[]> colorMap = new HashMap<>();
    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode rootNode;
    private final File SAVE;
    private final Map<String, Integer> savedImg = new HashMap<>();

    public DataManager(File confDir) throws IOException {
        this.setColorMap();
        this.confDir = confDir;
        this.msgFile = new File(confDir, "message.properties");
        this.imgDir = new File(confDir, "images");
        this.properties = new Properties();
        this.SAVE = new File(confDir, "save.conf");
        this.loader = HoconConfigurationLoader.builder().setFile(this.SAVE).build();

        this.creatFileIfNotExist();
        this.copyExampleImg();
        this.loadProperties();
        this.loadSAVE();
    }

    private void creatFileIfNotExist() throws IOException {
        if (!this.msgFile.exists()){
            Main.INSTANCE.getPluginContainer().getAsset("message.properties").ifPresent(asset -> {
                try {
                    asset.copyToDirectory(this.confDir.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        if (!this.SAVE.exists()){
            this.SAVE.createNewFile();
        }

        if (!this.imgDir.exists()){
            this.imgDir.mkdir();
        }
    }

    private void copyExampleImg(){

        File spongie = new File(this.imgDir,"spongie.png");

        if (!spongie.exists()){
            Main.INSTANCE.getPluginContainer().getAsset("spongie.png").ifPresent(asset -> {
                try {
                    asset.copyToDirectory(this.imgDir.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void loadProperties() throws IOException {
        if (this.msgFile.exists()){
            this.properties.load(new FileInputStream(this.msgFile));
        }else {
            Main.INSTANCE.getLogger().error("Missing message.properties file");
        }
    }

    public Text getMsg(String key){
        String value = this.properties.getProperty(key);
        return value == null ? Utils.toText("&4Message Not Found") : Utils.toText(value);
    }

    @Nullable
    public BufferedImage getBufferedImage(String fileName, CommandSource source) throws IOException {

        if (fileExist(fileName)){
            return ImageIO.read(getImageFile(fileName));
        }else {
            source.sendMessage(this.getMsg("no_such_file"));
        }

        return null;
    }

    @Nullable
    public BufferedImage getDownloadImage(String url, CommandSource source) throws MalformedURLException {
        ImageDownloadTask task = new ImageDownloadTask(url);
        Future<BufferedImage> future = task.getImage();
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            source.sendMessage(getMsg("download_failed"));
        }
        return null;
    }

    private File getImageFile(String name){
        return new File(this.imgDir, name);
    }

    private boolean fileExist(String fileName) throws IOException {
        return this.getImageFile(fileName).exists() && ImageIO.read(this.getImageFile(fileName)) != null;
    }

    //read index-RGB data from "ColorTable.TXT", create a hash map
    private void setColorMap() throws IOException {
        Asset asset = Main.INSTANCE.getPluginContainer().getAsset("ColorTable.TXT").get();
        List<String> colors = asset.readLines();
        for (String string:colors
        ) {
            String[] ints = string.split(",");
            addValue(Integer.parseInt(ints[0]), Integer.parseInt(ints[1]), Integer.parseInt(ints[2]), Integer.parseInt(ints[3]));
        }
    }

    //add value to map
    private static void addValue(int index, int red, int green, int blue){
        int[] rgb = {red,green,blue};
        colorMap.put(index, rgb);
    }

    private void loadSAVE() throws IOException {
        this.rootNode = this.loader.load();
        this.savedImg.clear();
        for (Map.Entry<Object, ? extends CommentedConfigurationNode> node:rootNode.getNode("Save").getChildrenMap().entrySet()
             ) {
            this.savedImg.put(node.getKey().toString(), node.getValue().getInt());
        }
    }

    private void saveSAVE() throws IOException {
        this.loader.save(this.rootNode);
    }

    public void addToSave(String name, int meta) throws IOException {
        this.rootNode.getNode("Save", name).setValue(meta);
        this.savedImg.put(name, meta);
        this.saveSAVE();
    }

    public int getSave(String name){
        CommentedConfigurationNode node = this.rootNode.getNode("Save", name);
        return node.isVirtual() ? -1:node.getInt();
    }

    public Set<String> getSavedName(){
        return this.savedImg.keySet();
    }

    public void delSave(String name, int meta) throws IOException {
        this.savedImg.remove(name);
        this.rootNode.getNode("Save").removeChild(name);
        this.saveSAVE();

        File world = new File(FMLServerHandler.instance().getSavesDirectory(), "world");
        File data = new File(world, "data");
        File map = new File(data, "map_" + meta + ".dat");
        map.deleteOnExit();
        System.out.println(map.exists());
    }

}
