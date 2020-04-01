package com.github.ericliucn.realmap.images;

import com.github.ericliucn.realmap.Main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static boolean isURL(String string){
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        return matcher.find();
    }

    public static boolean URLisImage(String URL) throws IOException {
        java.net.URL url = new URL(URL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        httpURLConnection.connect();

        BufferedImage image = ImageIO.read(httpURLConnection.getInputStream());

        return image != null;
    }

    public static boolean theImageExist(String imageName){
        Path path = Paths.get(Main.getFile().getPath(), "Images",imageName);
        File file = new File(path.toString());
        return file.exists();
    }

}
