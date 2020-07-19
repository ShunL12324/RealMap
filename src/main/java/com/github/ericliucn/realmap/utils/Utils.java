package com.github.ericliucn.realmap.utils;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.util.Collection;

public class Utils {

    public static Text toText(String string){
        return TextSerializers.FORMATTING_CODE.deserialize(string);
    }

    public static void addTree(File file, Collection<File> all) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                all.add(child);
                addTree(child, all);
            }
        }
    }
}
