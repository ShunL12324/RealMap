package com.github.ericliucn.realmap.utils;

import com.github.ericliucn.realmap.Main;
import org.spongepowered.api.map.MapCanvas;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageUtils {

    private static Map<Integer, BufferedImage> getBufferedImages(String imageName){
        Path imagePath = Main.instance.getPath().resolve("Images");
        Path imageFilePath = imagePath.resolve(imageName);
        Map<Integer, BufferedImage> bufferedImageMap = new HashMap<>();
        if (Files.exists(imageFilePath)){
            if (imageName.toLowerCase().endsWith("gif")) {
                try {
                    GifDecoder.GifImage gifImage = GifDecoder.read(new FileInputStream(imageFilePath.toFile()));
                    for (int i = 0; i < gifImage.getFrameCount(); i++) {
                        bufferedImageMap.put(i, gifImage.getFrame(i));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                try {
                    bufferedImageMap.put(0, ImageIO.read(imageFilePath.toFile()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(1);
        return bufferedImageMap;
    }

    public static Map<String, List<Byte>> getFrames(String imageName){
        Map<Integer, BufferedImage> bufferedImageMap = getBufferedImages(imageName);
        Map<String, List<Byte>> byteMap = new HashMap<>();
        for (Map.Entry<Integer, BufferedImage> entry : bufferedImageMap.entrySet()) {
            MapCanvas mapCanvas = Utils.getMapCanvas(entry.getValue());
            List<Byte> bytes = Utils.getMapCanvasBytes(mapCanvas);
            byteMap.put(String.valueOf(entry.getKey()), bytes);
        }
        return byteMap;
    }

}
