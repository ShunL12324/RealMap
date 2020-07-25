package com.github.ericliucn.realmap.config;

import com.github.ericliucn.realmap.Main;
import com.github.ericliucn.realmap.utils.ImageDownloadTask;
import com.github.ericliucn.realmap.utils.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.fml.server.FMLServerHandler;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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

    private void loadProperties() {
        if (this.msgFile.exists()){
            try {
                this.properties.load(new FileInputStream(this.msgFile));
            }catch (IOException e){
                e.printStackTrace();
            }
        }else {
            Main.INSTANCE.getLogger().error("Missing message.properties file");
        }
    }

    public Text getMsg(String key){
        String value = this.properties.getProperty(key);
        return value == null ? Utils.toText("&4Message Not Found") : Utils.toText(value);
    }

    @Nullable
    public BufferedImage getBufferedImage(String fileName, CommandSource source) {

        try {
            if (fileExist(fileName)){
                return ImageIO.read(getImageFile(fileName));
            }else {
                source.sendMessage(this.getMsg("no_such_file"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public BufferedImage getDownloadImage(String url, CommandSource source){
        try {
            ImageDownloadTask task = new ImageDownloadTask(url);
            Future<BufferedImage> future = task.getImage();
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                source.sendMessage(getMsg("download_failed"));
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }

    private File getImageFile(String name){
        return new File(this.imgDir, name);
    }

    private boolean fileExist(String fileName){
        try {
            return this.getImageFile(fileName).exists() && ImageIO.read(this.getImageFile(fileName)) != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //read index-RGB data from "ColorTable.TXT", create a hash map
    private void setColorMap(){
        Asset asset = Main.INSTANCE.getPluginContainer().getAsset("ColorTable.TXT").get();
        List<String> colors = null;
        try {
            colors = asset.readLines();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void loadSAVE(){
        try {
            this.rootNode = this.loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.savedImg.clear();
        for (Map.Entry<Object, ? extends CommentedConfigurationNode> node:rootNode.getNode("Save").getChildrenMap().entrySet()
             ) {
            this.savedImg.put(node.getKey().toString(), node.getValue().getInt());
        }
    }

    private void saveSAVE(){
        try {
            this.loader.save(this.rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToSave(String name, int meta) {
        this.rootNode.getNode("Save", name).setValue(meta);
        this.savedImg.put(name, meta);
        this.saveSAVE();
    }

    public int getSave(String name){
        CommentedConfigurationNode node = this.rootNode.getNode("Save", name);
        return node.isVirtual() ? -1:node.getInt();
    }

    public boolean saveExists(String name){
        for (Map.Entry<Object, ? extends CommentedConfigurationNode> node:rootNode.getNode("Save").getChildrenMap().entrySet()
        ) {
            if (node.getKey().toString().equals(name)) return true;
        }
        return false;
    }

    //Only for tab completing
    public Set<String> getSavedName(){
        return this.savedImg.keySet();
    }

    public void delSave(@Nullable String name, int meta) {
        if (name != null){
            this.savedImg.remove(name);
            this.rootNode.getNode("Save").removeChild(name);
            this.saveSAVE();
        }

        //remove map itemstack if possible
        Sponge.getServer().getOnlinePlayers().forEach(player -> {
            ItemStack itemStack = new ItemStack(Items.FILLED_MAP, 1, meta);
            EntityPlayerMP playerMP = ((EntityPlayerMP) player);
            playerMP.inventory.mainInventory.forEach(stack -> {
                if (stack.isItemEqual(itemStack)){
                    stack.shrink(stack.getCount());
                }
            });

        });

        //delete map_*.dat file
        File root = FMLServerHandler.instance().getSavesDirectory();
        String path = Paths.get(root.getPath(), "world", "data", "map_" + meta + ".dat").toString();
        File map = new File(path);
        if (map.exists()){
            map.delete();
        }

        //delete map data from cache
        World world = FMLServerHandler.instance().getServer().getWorld(0);
        MapStorage mapStorage = world.getMapStorage();
        String id = "map_" + meta;
        if (mapStorage!=null){
            if (mapStorage.loadedDataMap.containsKey(id)){
                mapStorage.loadedDataList.remove(mapStorage.loadedDataMap.get(id));
                mapStorage.loadedDataMap.remove(id);
                //refresh the idcount.dat and idcount cache
                this.setMapCount(mapStorage);
            }
        }

    }

    public void setMapCount(MapStorage storage) {
        File root = FMLServerHandler.instance().getSavesDirectory();
        String path = Paths.get(root.getPath(), "world", "data", "idcounts.dat").toString();
        String dataPath = Paths.get(root.getPath(), "world", "data").toString();
        File data= new File(dataPath);
        File count = new File(path);

        //get the max number of map_().dat
        Collection<File> collection = new HashSet<>();
        Utils.addTree(data, collection);
        Set<Integer> files = collection.stream()
                .filter(file -> file.getName().startsWith("map_"))
                .map(File::getName)
                .map(string -> string.replace("map_", "").replace(".dat", ""))
                .map(Integer::valueOf)
                .collect(Collectors.toSet());
        int max = files.size() == 0 ? 0 : Collections.max(files);

        try {
            //get current count and set it
            NBTTagCompound tagCompound = CompressedStreamTools.read(count);
            assert tagCompound != null;
            tagCompound.setShort("map", (short) max);
            // flush cache
            storage.idCounts.put("map", ((short) max));
            //write to idcount.dat
            CompressedStreamTools.write(tagCompound, count);

            //save all map data(maybe not necessary)
            World world = FMLServerHandler.instance().getServer().getWorld(0);
            Objects.requireNonNull(world.getMapStorage()).saveAllData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
