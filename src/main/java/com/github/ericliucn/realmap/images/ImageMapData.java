package com.github.ericliucn.realmap.images;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.MapData;

import java.io.IOException;

public class ImageMapData {

    public MapData mapData;
    public ItemStack itemStack;

    public ImageMapData(EntityPlayerMP playerMP, String URLorFileName) throws IOException {
        int id = playerMP.getServerWorld().getUniqueDataId("map");
        mapData = new MapData("map_"+id);
        mapData.scale = (byte)0;
        mapData.zCenter = 999999;
        mapData.unlimitedTracking = false;
        mapData.trackingPosition = false;
        mapData.xCenter = 0;
        mapData.dimension = 999999;
        mapData.colors = ImageProcess.getMapDataOfURLorFileName(URLorFileName);
        mapData.markDirty();
        playerMP.getServerWorld().setData("map_"+id,mapData);

        this.itemStack = new ItemStack(Items.FILLED_MAP,1,id);
    }

}
