package com.github.ericliucn.realmap.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImageDownloadTask {

    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final URL url;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.75 Safari/535.7";

    public ImageDownloadTask(String URL) throws MalformedURLException {
        this.url = new URL(URL);
    }

    public Future<BufferedImage> getImage(){
        return this.service.submit(()->{
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            urlConnection.connect();
            return ImageIO.read(urlConnection.getInputStream());
        });
    }
}
