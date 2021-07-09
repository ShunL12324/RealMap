package com.github.ericliucn.realmap.command;

import com.github.ericliucn.realmap.Main;
import com.github.ericliucn.realmap.command.completer.FileNameCompleter;
import com.github.ericliucn.realmap.handler.MapInfoHandler;
import com.github.ericliucn.realmap.utils.ImageUtils;
import com.github.ericliucn.realmap.utils.Utils;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.service.permission.Subject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Commands {

    private static final Parameter.Value<String> mapNamePara = Parameter.string().key("mapName").build();
    private static final Parameter.Value<String> imagePara = Parameter.string().completer(new FileNameCompleter()).key("image").build();

    public static final Command.Parameterized create = Command.builder()
            .permission("realmap.command.create")
            .addParameters(mapNamePara)
            .addParameters(imagePara)
            .executor(context -> {
                try {
                    String mapName = context.requireOne(mapNamePara);
                    String imageName = context.requireOne(imagePara);

                    String creatorName = "UNKNOWN";
                    Object root = context.cause().root();
                    if (root instanceof ServerPlayer){
                        creatorName = ((ServerPlayer) root).name();
                    }else if (root instanceof SystemSubject){
                        creatorName = "terminal";
                    }

                    List<MapCanvas> mapCanvasList = ImageUtils.getFrames(imageName);
                    if (mapCanvasList.size() == 0) return CommandResult.error(Utils.toComponent("&4Unable to get image file"));
                    Optional<MapInfo> optionalMapInfo = Sponge.server().mapStorage().createNewMapInfo();
                    if (!optionalMapInfo.isPresent()) return CommandResult.error(Utils.toComponent("&4Unable to create new map"));
                    MapInfo mapInfo = optionalMapInfo.get();


                    mapInfo.offer(Main.MAP_CURRENT_FRAME,0);
                    mapInfo.offer(Main.MAP_FRAMES, mapCanvasList);
                    mapInfo.offer(Main.MAP_CREATOR, creatorName);
                    mapInfo.offer(Main.MAP_NAME, mapName);
                    mapInfo.offer(Keys.MAP_CANVAS, mapCanvasList.get(0));
                    mapInfo.offer(Keys.MAP_LOCKED, true);


                    if (root instanceof ServerPlayer){
                        ServerPlayer player = ((ServerPlayer) root);
                        ItemStack itemStack = ItemStack.of(ItemTypes.FILLED_MAP);
                        itemStack.offer(Keys.MAP_INFO, mapInfo);
                        Utils.giveItem(itemStack, player);
                    }
                    MapInfoHandler.instance.reload();
                    return CommandResult.success();
                }catch (Exception e){
                    return CommandResult.error(Component.text("error"));
                }
            })
            .build();

    public static Command.Parameterized test = Command.builder()
            .executor(context -> {
                Subject subject = context.subject();
                if (subject instanceof ServerPlayer){
                    ServerPlayer player = ((ServerPlayer) subject);
                    ItemStack itemStack = player.inventory().hotbar().slot(player.inventory().hotbar().selectedSlotIndex()).get().peek();
                    if (itemStack.type().equals(ItemTypes.FILLED_MAP.get())){
                        MapInfo mapInfo = itemStack.get(Keys.MAP_INFO).get();
                        MapCanvas mapCanvas = mapInfo.get(Keys.MAP_CANVAS).get();

                        List<Byte> byteList = new ArrayList<>();
                        for (int i = 0; i < 16384; i++) {
                            byteList.add((byte) 0x30);
                        }
                        DataContainer newContainer = mapCanvas.toContainer().set(DataQuery.of("MapCanvas"), byteList);


                        mapInfo.offer(Keys.MAP_CANVAS, MapCanvas.builder().fromContainer(newContainer).build());
                        itemStack.offer(Keys.MAP_INFO, mapInfo);
                    }

                }
                return CommandResult.success();
            })
            .build();

}
