package com.github.ericliucn.realmap.handler;

import com.github.ericliucn.realmap.Main;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MapUpdateTaskHandler {

    public static MapUpdateTaskHandler instance;
    private final Map<UUID, MapInfo> mapInfoMap;
    private final Map<UUID, Task> taskMap;
    private boolean on;

    public MapUpdateTaskHandler(){
        instance = this;
        mapInfoMap = new HashMap<>();
        taskMap = new ConcurrentHashMap<>();
        on = true;
        for (MapInfo info : Sponge.server().mapStorage().allMapInfos()) {
            if (info.supports(Main.MAP_FRAMES)){
                mapInfoMap.put(info.uniqueId(), info);
                createUpdateTask(info);
            }
        }
    }

    private void createUpdateTask(MapInfo mapInfo){
        Task task = Task.builder()
                .execute(()->{
                    if (on) {
                        List<MapCanvas> mapCanvasList = mapInfo.get(Main.MAP_FRAMES).get();
                        int nextFrameIndex = mapInfo.get(Main.MAP_CURRENT_FRAME).get() + 1;
                        if (nextFrameIndex >= mapCanvasList.size()) nextFrameIndex = 0;
                        MapCanvas mapCanvas = mapCanvasList.get(nextFrameIndex);
                        mapInfo.offer(Keys.MAP_CANVAS, mapCanvas);
                        mapInfo.offer(Main.MAP_CURRENT_FRAME, nextFrameIndex);
                    }
                })
                .interval(15, TimeUnit.MILLISECONDS)
                .plugin(Main.instance.container)
                .name("map.update." + mapInfo.uniqueId().toString())
                .build();
        taskMap.put(mapInfo.uniqueId(), task);
        Sponge.server().game().asyncScheduler().submit(task);
    }

    public void addMapInfo(MapInfo mapInfo){
        mapInfoMap.put(mapInfo.uniqueId(), mapInfo);
        createUpdateTask(mapInfo);
    }

    public Map<UUID, MapInfo> getMapInfoMap() {
        return mapInfoMap;
    }

    public Map<UUID, Task> getTaskMap() {
        return taskMap;
    }

    public void setStatus(boolean onValue){
        this.on = onValue;
    }

    public boolean getStatus(){
        return this.on;
    }

}
