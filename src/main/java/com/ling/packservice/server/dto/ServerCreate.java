package com.ling.packservice.server.dto;

import com.ling.packservice.LoaderType;
import com.ling.packservice.server.Server;

public record ServerCreate(String name, String description, String host, Integer port, LoaderType type, String version) {
    public Server toServer() {
        return Server.builder()
                .name(name)
                .description(description)
                .host(host)
                .port(port)
                .type(type)
                .version(version)
                .build();
    }
}
