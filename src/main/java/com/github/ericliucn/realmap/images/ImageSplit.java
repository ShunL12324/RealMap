package com.github.ericliucn.realmap.images;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class ImageSplit {

    private Map<Integer[], BufferedImage> imageMap;
    private final BufferedImage image;
    private final int xSize;
    private final int ySize;

    public ImageSplit(BufferedImage image, int xSize, int ySize){
        this.image = image;
        this.xSize = xSize;
        this.ySize = ySize;
        this.genSubImages();
    }


    private BufferedImage getResizedImage(){
        BufferedImage bufferedImage = new BufferedImage(xSize * 128, ySize * 128, BufferedImage.TRANSLUCENT);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, xSize * 128, ySize * 128, null);
        graphics2D.dispose();
        return bufferedImage;
    }

    private void genSubImages(){
        for (int i = 0; i < xSize ; i++) {
            for (int j = 0; j < ySize ; j++) {
                BufferedImage sub = this.getResizedImage().getSubimage(i * 128, j * 128, 128, 128);
                Integer[] pos = new Integer[]{i, j};
                imageMap.put(pos, sub);
            }
        }
    }

    public Map<Integer[], BufferedImage> getImageMap() {
        return imageMap;
    }
}
