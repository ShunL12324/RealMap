package com.github.ericliucn.realmap.utils;

import com.github.ericliucn.realmap.Main;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapColorTypes;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.registry.RegistryTypes;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ImageUtils {

    private static List<BufferedImage> getBufferedImages(String imageName){
        Path imagePath = Main.instance.getPath().resolve("Images");
        Path imageFilePath = imagePath.resolve(imageName);
        List<BufferedImage> bufferedImageList = new ArrayList<>();
        if (Files.exists(imageFilePath)){
            try {
                if (imageFilePath.toString().toLowerCase().endsWith(".gif")) {
                    GifDecoder.GifImage gifImage = GifDecoder.read(new FileInputStream(imageFilePath.toFile()));
                    for (int i = 0; i < gifImage.getFrameCount(); i++) {
                        bufferedImageList.add(gifImage.getFrame(i));
                    }
                } else {
                    bufferedImageList.add(ImageIO.read(imageFilePath.toFile()));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return bufferedImageList;
    }

    public static List<MapCanvas> getFrames(String imageName){
        List<MapCanvas> mapCanvasList = new ArrayList<>();
        for (BufferedImage image : getBufferedImages(imageName)) {
            mapCanvasList.add(getMapCanvas(image));
        }
        return mapCanvasList;
    }

    private static BufferedImage getResizedImage(BufferedImage inputImage, int width, int height){
        Image image = inputImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = outputImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();
        return outputImage;
    }

    private static MapCanvas getMapCanvas(BufferedImage bufferedImage){
        MapCanvas.Builder canvasBuilder = MapCanvas.builder();
        BufferedImage image = getResizedImage(bufferedImage, 128, 128);
        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                Color color = new Color(image.getRGB(i, j));
                canvasBuilder.paint(i, j, i, j, getSimilarPossibleColor(color));
            }
        }
        return canvasBuilder.build();
    }

    private static MapColor getSimilarPossibleColor(Color color){
        if (allPossibleColor == null) allPossibleColor = getAllPossibleColor();
        MapColor mapColor = MapColor.of(MapColorTypes.COLOR_BLACK);
        double maxSimilarity = 0;
        for (MapColor possibleColor : allPossibleColor) {
            Color javaPossibleColor = possibleColor.color().asJavaColor();
            double similarity = getSimilarity(javaPossibleColor, color);
            if (similarity > maxSimilarity){
                maxSimilarity = similarity;
                mapColor = possibleColor;
            }
        }
        return mapColor;
    }

    private static Collection<MapColor> allPossibleColor;

    private static Collection<MapColor> getAllPossibleColor(){
        Collection<MapColorType> mapColorTypes = Sponge.game()
                .registries()
                .registry(RegistryTypes.MAP_COLOR_TYPE)
                .stream()
                .collect(Collectors.toList());
        Collection<MapShade> mapShades = Sponge.game()
                .registries()
                .registry(RegistryTypes.MAP_SHADE)
                .stream()
                .collect(Collectors.toList());
        Collection<MapColor> mapColors = new ArrayList<>();
        for (MapColorType colorType : mapColorTypes) {
            for (MapShade mapShade : mapShades) {
                mapColors.add(MapColor.builder().baseColor(colorType).shade(mapShade).build());
            }
        }
        return mapColors;
    }

    private static double getSimilarity(Color colorOne, Color colorTwo){
        double similarity =
                Math.sqrt(
                        Math.pow((colorOne.getBlue() - colorTwo.getBlue()), 2) +
                                Math.pow((colorOne.getGreen() - colorTwo.getGreen()), 2) +
                                Math.pow((colorOne.getRed() -  colorTwo.getRed()), 2)
                );
        return 1 - (similarity/442);
    }

}
