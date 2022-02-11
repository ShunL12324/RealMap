package com.devcooker.realmap.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class RConfig {

    @Setting(value = "version")
    public String version = "2.0-SNAPSHOT";
}
