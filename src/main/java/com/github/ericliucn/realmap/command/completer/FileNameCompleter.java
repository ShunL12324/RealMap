package com.github.ericliucn.realmap.command.completer;

import com.github.ericliucn.realmap.Main;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.registrar.tree.CommandCompletionProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileNameCompleter implements CommandCompletionProvider {
    @Override
    public List<CommandCompletion> complete(CommandContext context, String currentInput) {
        Path imagePath = Main.instance.getPath().resolve("Images");
        List<CommandCompletion> commandCompletions = new ArrayList<>();
        try {
            Files.list(imagePath).forEach(subPath -> commandCompletions.add(CommandCompletion.of(subPath.getFileName().toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return commandCompletions;
    }
}
