package com.devcooker.realmap.command;

import com.devcooker.realmap.command.completer.FileNameCompleter;
import com.devcooker.realmap.handler.MapUpdateTaskHandler;
import com.devcooker.realmap.Main;
import com.devcooker.realmap.utils.ImageUtils;
import com.devcooker.realmap.utils.Utils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.service.permission.Subject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Commands {

    private static final Parameter.Value<String> mapNamePara = Parameter.string().key("mapName").build();
    private static final Parameter.Value<String> imagePara = Parameter.string().completer(new FileNameCompleter()).key("image").build();

    public static final Command.Parameterized create = Command.builder()
            .addParameters(mapNamePara)
            .addParameters(imagePara)
            .executor(context -> {
                try {
                    String mapName = context.requireOne(mapNamePara);
                    String imageName = context.requireOne(imagePara);

                    boolean duplicate = Sponge.server().mapStorage().allMapInfos().stream()
                            .filter(mapInfo -> mapInfo.get(Main.MAP_NAME).isPresent())
                            .anyMatch(mapInfo -> mapInfo.get(Main.MAP_NAME).get().equalsIgnoreCase(mapName));

                    if (duplicate){
                        return CommandResult.error(Utils.toComponent("&4duplicate map name!"));
                    }

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
                    MapUpdateTaskHandler.instance.addMapInfo(mapInfo);
                    return CommandResult.success();
                }catch (Exception e){
                    return CommandResult.error(Component.text("error"));
                }
            })
            .permission("realmap.create")
            .build();

    public static Command.Parameterized give = Command.builder()
            .executor(context -> {
                String name = context.requireOne(Parameter.string().key("map_name").build());
                ServerPlayer player = context.requireOne(Parameter.player().key("player").build());
                Sponge.server().mapStorage().allMapInfos().stream()
                        .filter(mapInfo -> mapInfo.get(Main.MAP_NAME).isPresent() && mapInfo.get(Main.MAP_NAME).get().equalsIgnoreCase(name))
                        .findFirst()
                        .ifPresent(mapInfo -> {
                            ItemStack itemStack = ItemStack.of(ItemTypes.FILLED_MAP);
                            itemStack.offer(Keys.MAP_INFO, mapInfo);
                            Utils.giveItem(itemStack, player);
                        });
                return CommandResult.success();
            })
            .addParameter(Parameter.player().key("player").build())
            .addParameter(
                    Parameter.string()
                            .key("map_name")
                            .completer((context, currentInput) -> Sponge.server().mapStorage().allMapInfos().stream()
                                    .filter(mapInfo -> mapInfo.get(Main.MAP_NAME).isPresent())
                                    .map(mapInfo -> mapInfo.get(Main.MAP_NAME).get())
                                    .map(CommandCompletion::of)
                                    .collect(Collectors.toList()))
                            .build()
            )
            .permission("realmap.give")
            .build();

    public static Command.Parameterized toggle = Command.builder()
            .executor(context -> {
                MapUpdateTaskHandler handler = MapUpdateTaskHandler.instance;
                handler.setStatus(!handler.getStatus());
                if (context.cause().root() instanceof Audience){
                    Audience audience = ((Audience) context.cause().root());
                    if (handler.getStatus()){
                        audience.sendMessage(Utils.toComponent("&aTurn on map update task"));
                    }else {
                        audience.sendMessage(Utils.toComponent("&aTurn off map update task"));
                    }
                }
                return CommandResult.success();
            })
            .permission("realmap.toggle")
            .build();

    public static Command.Parameterized test = Command.builder()
            .executor(context -> {
                Subject subject = context.subject();
                if (subject instanceof ServerPlayer){
                    ServerPlayer player = ((ServerPlayer) subject);
                    ItemStack itemStack = player.itemInHand(HandTypes.MAIN_HAND);
                    MapUpdateTaskHandler.instance.setStatus(!MapUpdateTaskHandler.instance.getStatus());


                }
                return CommandResult.success();
            })
            .build();

}
