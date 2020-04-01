package com.github.ericliucn.realmap.images;

import com.github.ericliucn.realmap.Main;
import com.github.ericliucn.realmap.config.ColorData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImageProcess {

    private static BufferedImage getImage(String urlString) throws IOException {
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            httpURLConnection.connect();

            BufferedImage originImage = ImageIO.read(httpURLConnection.getInputStream());

            return getResizedImage(originImage);

        }catch (Exception e){
            return null;
        }
    }

    private static BufferedImage getResizedImage(BufferedImage originImage){
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TRANSLUCENT);

        Graphics2D graphics2D = image.createGraphics();
        graphics2D.drawImage(originImage, 0, 0, 128, 128, null);
        graphics2D.dispose();

        return image;
    }

    private static int getIndexOfTheRGB(int red, int green, int blue){
        Map<Integer, Double> similarities = new HashMap<>();
        for (Map.Entry<Integer,int[]> entry: ColorData.colorMap.entrySet()
        ) {
            int r = entry.getValue()[0];
            int g = entry.getValue()[1];
            int b = entry.getValue()[2];

            double similarity = Math.pow((red - r), 2) + Math.pow((green - g),2) + Math.pow((blue - b),2);
            similarities.put(entry.getKey(),similarity);
        }

        double min = Collections.min(similarities.values());

        for (Map.Entry<Integer,Double> entry:similarities.entrySet()){
            if (entry.getValue() <= min){
                return entry.getKey();
            }
        }

        return 4;
    }

    public static byte[] getMapDataOfURLorFileName(String URL) throws IOException {
        byte[] colors = new byte[16384];
        BufferedImage image;
        if (Utils.isURL(URL)){
            image = getImage(URL);
        }else {
            Path path = Paths.get(Main.getFile().getPath(),"Images",URL);
            File file = new File(path.toString());
            image = getResizedImage(ImageIO.read(file));
        }
        if (image!=null) {
            int n = 0;
            for (int i = 0; i < 128; i++) {
                for (int j = 0; j < 128; j++) {
                    Color color = new Color(image.getRGB(j, i));
                    int index = getIndexOfTheRGB(color.getRed(), color.getGreen(), color.getBlue());
                    colors[n] = (byte) index;
                    n += 1;
                }
            }
        }else {
            for (int i = 0; i < 16384 ; i++) {
                colors[i] = (byte) 4;
            }
        }
        return colors;
    }

}
