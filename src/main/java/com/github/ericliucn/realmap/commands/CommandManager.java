package com.github.ericliucn.realmap.commands;

import com.github.ericliucn.realmap.Main;
import com.github.ericliucn.realmap.config.DataManager;
import com.github.ericliucn.realmap.images.ImageSaveTask;
import com.github.ericliucn.realmap.utils.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private static final List<Text> tips = new ArrayList<Text>(){{
        add(Utils.toText("&b/rmap create <file|url> <name> <file_name>"));
        add(Utils.toText("&b/rmap give <Player> <name>"));
        add(Utils.toText("&b/rmap del <name>"));
    }};

    private static final CommandSpec FILE = CommandSpec.builder()
            .executor((src, args) -> {
                String name = args.<String>getOne("name").get();
                String file = args.<String>getOne("file").get();
                try {
                    BufferedImage image = Main.INSTANCE.getDataManager().getBufferedImage(file, src);
                    if (image!=null){
                        ImageSaveTask imageSaveTask = new ImageSaveTask(image);
                        Main.INSTANCE.getDataManager().addToSave(name, imageSaveTask.getId());
                        if (src instanceof Player){
                            Player player = ((Player) src);
                            player.getInventory().offer(ItemStackUtil.fromNative(imageSaveTask.getItemStack()));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommandResult.success();
            })
            .arguments(
                    GenericArguments.seq(
                            GenericArguments.string(Text.of("name")),
                            GenericArguments.string(Text.of("file"))
                    )
            )
            .permission("realmap.create.file")
            .build();

    private static final CommandSpec URL = CommandSpec.builder()
            .executor((src, args) -> {
                String name = args.<String>getOne("name").get();
                URL url = args.<URL>getOne("url").get();
                try {
                    BufferedImage image = Main.INSTANCE.getDataManager().getDownloadImage(url.toString(), src);
                    if (image!=null){
                        ImageSaveTask imageSaveTask = new ImageSaveTask(image);
                        Main.INSTANCE.getDataManager().addToSave(name, imageSaveTask.getId());
                        if (src instanceof Player){
                            Player player = ((Player) src);
                            player.getInventory().offer(ItemStackUtil.fromNative(imageSaveTask.getItemStack()));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommandResult.success();
            })
            .arguments(
                    GenericArguments.seq(
                            GenericArguments.string(Text.of("name")),
                            GenericArguments.url(Text.of("url"))
                    )
            )
            .permission("realmap.create.url")
            .build();

    private static final CommandSpec CREATE = CommandSpec.builder()
            .executor((src, args) -> {
                src.sendMessage(Main.INSTANCE.getDataManager().getMsg("create_tip"));
                return CommandResult.success();
            })
            .child(FILE, "file")
            .child(URL, "url")
            .build();

    private static final CommandSpec GIVE = CommandSpec.builder()
            .executor((src, args) -> {
                Player player = args.<Player>getOne("player").get();
                String name = args.<String>getOne("name").get();
                int meta = Main.INSTANCE.getDataManager().getSave(name);
                if (meta < 0){
                    src.sendMessage(Main.INSTANCE.getDataManager().getMsg("no_such_save"));
                }else {
                    ItemStack itemStack = new ItemStack(Items.FILLED_MAP, 1, meta);
                    player.getInventory().offer(ItemStackUtil.fromNative(itemStack));
                }
                return CommandResult.success();
            })
            .arguments(
                    GenericArguments.seq(
                            GenericArguments.player(Text.of("player")),
                            GenericArguments.withSuggestions(
                                    GenericArguments.string(Text.of("name")),
                                    Main.INSTANCE.getDataManager().getSavedName()
                            )
                    )
            )
            .permission("realmap.give")
            .build();

    private static final CommandSpec DEL = CommandSpec.builder()
            .executor((src, args) -> {
                String name = args.<String>getOne(Text.of("name")).get();
                if (!Main.INSTANCE.getDataManager().getSavedName().contains(name)){
                    src.sendMessage(Main.INSTANCE.getDataManager().getMsg("no_such_save"));
                }else {
                    try {
                        int meta = Main.INSTANCE.getDataManager().getSave(name);
                        Main.INSTANCE.getDataManager().delSave(name, meta);
                        src.sendMessage(Main.INSTANCE.getDataManager().getMsg("del_success"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return CommandResult.success();
            })
            .arguments(
                    GenericArguments.withSuggestions(
                            GenericArguments.string(Text.of("name")),
                            Main.INSTANCE.getDataManager().getSavedName()
                    )
            )
            .permission("realmap.del")
            .build();

    private static final CommandSpec BASE = CommandSpec.builder()
            .executor((src, args) -> {
                PaginationList.builder()
                        .title(Utils.toText("&d&lRealMap"))
                        .padding(Utils.toText("&a="))
                        .contents(tips)
                        .sendTo(src);
                return CommandResult.success();
            })
            .child(CREATE, "create")
            .child(DEL, "del", "delete")
            .child(GIVE, "give")
            .build();

    public CommandManager(){
        Sponge.getCommandManager().register(Main.INSTANCE, BASE,"realmap", "rmap");
    }
}
