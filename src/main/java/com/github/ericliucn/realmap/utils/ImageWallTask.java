package com.github.ericliucn.realmap.utils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.awt.image.BufferedImage;
import java.util.Map;


public class ImageWallTask {

    private final EntityPlayerMP playerMP;
    private final RayTraceResult result;
    private final Map<Integer[], BufferedImage> imageMap;
    private final int xSize;
    private final int ySize;

    public ImageWallTask(EntityPlayerMP playerMP, RayTraceResult result, Map<Integer[], BufferedImage> imageMap, int xSize, int ySize){
        this.playerMP = playerMP;
        this.result = result;
        this.imageMap = imageMap;
        this.xSize = xSize;
        this.ySize = ySize;
    }

    private void createWall(){
        BlockPos pos = result.getBlockPos();
        EnumFacing side = result.sideHit;



    }
}
