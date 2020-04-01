package com.github.ericliucn.realmap.config;

import com.github.ericliucn.realmap.Main;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorData {

    public static Map<Integer, int[]> colorMap = new HashMap<>();

    //read index-RGB data from "ColorTable.TXT", create a hash map
    public static void setColorMap() throws IOException {
        Asset asset = Main.getPluginContainer().getAsset("ColorTable.TXT").get();
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
}
