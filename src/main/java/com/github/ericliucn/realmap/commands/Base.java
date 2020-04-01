package com.github.ericliucn.realmap.commands;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Base implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args)  {
        List<Text> list = new ArrayList<>();
        URL wikiURL = null;
        try {
            wikiURL = new URL("https://github.com/Eric-liucn/RealMap/wiki");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Text wiki = Text.builder("Click to open URL")
                .onClick(TextActions.openUrl(wikiURL))
                .color(TextColors.AQUA)
                .build();
        list.add(formatString("&5Version: &b0.1(BETA)"));
        list.add(Text.builder("Wiki: ").color(TextColors.DARK_PURPLE).append(wiki).build());

        PaginationList.builder()
                .contents(list)
                .title(formatString("&6RealMap"))
                .padding(formatString("&a="))
                .sendTo(src);

        return CommandResult.success();
    }

    public static CommandSpec build(){
        return CommandSpec.builder()
                .executor(new Base())
                .child(Create.build(),"create","c")
                .build();
    }

    private static Text formatString(String string){
        return TextSerializers.FORMATTING_CODE.deserialize(string);
    }
}
