package com.github.ericliucn.realmap.images;

import com.github.ericliucn.realmap.config.DataManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImageSaveTask {

    private final BufferedImage preImage;
    private final int id;

    public ImageSaveTask(BufferedImage image){
        this.preImage = image;
        World world = FMLServerHandler.instance().getServer().getWorld(0);
        this.id = world.getUniqueDataId("map");
        MapData mapData = new MapData("map_"+id);
        mapData.scale = (byte)0;
        mapData.zCenter = 999999;
        mapData.unlimitedTracking = false;
        mapData.trackingPosition = false;
        mapData.xCenter = 0;
        mapData.dimension = 999999;
        mapData.colors = getColorData();
        mapData.markDirty();
        world.setData("map_"+id,mapData);
    }

    public ItemStack getItemStack(){
        return new ItemStack(Items.FILLED_MAP, 1, this.id);
    }

    public int getId() {
        return id;
    }

    private BufferedImage getResizedImage(BufferedImage originImage){
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TRANSLUCENT);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.drawImage(originImage, 0, 0, 128, 128, null);
        graphics2D.dispose();
        return image;
    }

    private int getIndexOfTheRGB(int red, int green, int blue){
        Map<Integer, Double> similarities = new HashMap<>();
        for (Map.Entry<Integer,int[]> entry: DataManager.colorMap.entrySet()
        ) {
            int r = entry.getValue()[0];
            int g = entry.getValue()[1];
            int b = entry.getValue()[2];

            double similarity = Math.pow((red - r), 2) + Math.pow((green - g),2) + Math.pow((blue - b),2);
            similarities.put(entry.getKey(),similarity);
        }

        double min = Collections.min(similarities.values());

        for (Map.Entry<Integer,Double> entry:similarities.entrySet()){
            if (entry.getValue() <= min){
                return entry.getKey();
            }
        }

        return 4;
    }

    private byte[] getColorData() {
        BufferedImage image = getResizedImage(preImage);
        byte[] colors = new byte[16384];
        int n = 0;
        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                Color color = new Color(image.getRGB(j, i));
                int index = getIndexOfTheRGB(color.getRed(), color.getGreen(), color.getBlue());
                colors[n] = (byte) index;
                n += 1;
            }
        }
        return colors;
    }
}
