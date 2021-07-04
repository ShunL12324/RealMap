package com.github.ericliucn.realmap.utils;

import com.github.ericliucn.realmap.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapColorType;
import org.spongepowered.api.map.color.MapColorTypes;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

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

    private static BufferedImage getResizedImage(BufferedImage inputImage, int width, int height){
        Image image = inputImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = outputImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();
        return outputImage;
    }


    public static MapCanvas getMapCanvas(BufferedImage bufferedImage){
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

    public static BufferedImage getBufferedImage(String imageName){
        BufferedImage bufferedImage = null;
        try {
            Path directory = Main.instance.getPath().resolve("Images");
            if (!Files.exists(directory)) Files.createDirectory(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            URL url = new URL(imageName);
            bufferedImage = ImageIO.read(url);
        } catch (IOException e) {
            try {
                Path file = Main.instance.getPath().resolve("Images").resolve(imageName);
                bufferedImage = ImageIO.read(file.toFile());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return bufferedImage;
    }

    public static Component toComponent(String s){
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    public static String toString(Component component){
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }


    public static MapInfo createNewMap(BufferedImage bufferedImage){
        Optional<MapInfo> optionalMapInfo = Sponge.server().mapStorage().createNewMapInfo();
        if (!optionalMapInfo.isPresent()) return null;
        MapInfo mapInfo = optionalMapInfo.get();
        MapCanvas mapCanvas = getMapCanvas(bufferedImage);
        mapInfo.offer(Keys.MAP_CANVAS, mapCanvas);
        mapInfo.offer(Keys.MAP_LOCKED, true);
        return mapInfo;
    }

    public static void giveItem(ItemStack itemStack, ServerPlayer serverPlayer){
        Entity entity = serverPlayer.world().createEntity(EntityTypes.ITEM, serverPlayer.position());
        entity.offer(Keys.ITEM_STACK_SNAPSHOT, itemStack.createSnapshot());
        entity.offer(Keys.PICKUP_DELAY, Ticks.of(1));
        serverPlayer.world().spawnEntity(entity);
    }

    public static List<Byte> getMapCanvasBytes(MapCanvas canvas){
        Optional<List<Byte>> optionalBytes = canvas.toContainer().getByteList(DataQuery.of("MapCanvas"));
        if (optionalBytes.isPresent() && optionalBytes.get().size() == 16384){
            return optionalBytes.get();
        }else {
            List<Byte> byteList = new ArrayList<>();
            for (int i = 0; i < 16384; i++) {
                byteList.add((byte) 0);
            }
            return byteList;
        }
    }

    public static MapCanvas fillMapCanvasWithBytes(MapCanvas canvas, List<Byte> byteList){
        DataContainer container = canvas.toContainer().set(DataQuery.of("MapCanvas"), byteList);
        return MapCanvas.builder().fromContainer(container).build();
    }


}
