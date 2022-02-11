package com.devcooker.realmap.utils;

import com.devcooker.realmap.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.util.Ticks;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class Utils {


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

    public static String toPlain(Component component){
        return PlainTextComponentSerializer.plainText().serialize(component);
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
