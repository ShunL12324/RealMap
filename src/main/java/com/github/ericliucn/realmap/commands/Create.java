package com.github.ericliucn.realmap.commands;

import com.github.ericliucn.realmap.Main;
import com.github.ericliucn.realmap.config.Message;
import com.github.ericliucn.realmap.images.ImageMapData;
import com.github.ericliucn.realmap.images.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;

public class Create implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player){
            String URLorFileName = args.<String>getOne("URL_OR_FILENAME").get();
            EntityPlayerMP player = FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUsername(src.getName());
            if (Utils.isURL(URLorFileName)) {
                //is URL
                try {
                    //the result of the URL is an image?
                    if (Utils.URLisImage(URLorFileName)) {
                        //Yes, creat itemStack and put into player's inventory
                        giveTheMap(player,URLorFileName);
                    }else {
                        //No, not a valid URL
                        src.sendMessage(Text.of(TextColors.DARK_RED,Message.getMessage("NotImageURL")));
                    }
                } catch (IOException ignored){}
            }else {
                //Not URL, it's a file name
                //Is the file exist?
                if (Utils.theImageExist(URLorFileName)){
                    //Yes
                    try {
                        giveTheMap(player,URLorFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    //No
                    src.sendMessage(Text.of(TextColors.DARK_RED,Message.getMessage("NoImage")));
                }
            }
        }else {
            //Only run by player
            src.sendMessage(Text.of(TextColors.DARK_RED,Message.getMessage("OnlyRunByPlayer")));
        }

        return CommandResult.success();
    }

    public static CommandSpec build(){
        return CommandSpec.builder()
                .executor(new Create())
                .arguments(
                        GenericArguments.remainingJoinedStrings(Text.of("URL_OR_FILENAME"))
                )
                .build();
    }

    private static void giveTheMap(EntityPlayerMP playerMP, String URLorFileName) throws IOException {
        ImageMapData imageMapData = new ImageMapData(playerMP,URLorFileName);
        Sponge.getScheduler().createTaskBuilder()
                .execute(()->{
                    playerMP.inventory.addItemStackToInventory(imageMapData.itemStack);
                })
                .async()
                .submit(Main.getINSTANCE());
    }


}
