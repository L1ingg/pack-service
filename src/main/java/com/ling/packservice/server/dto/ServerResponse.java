package com.ling.packservice.server.dto;

import com.ling.packservice.LoaderType;
import com.ling.packservice.server.Server;

import java.util.UUID;

public record ServerResponse(UUID id, String name, String description, String host, Integer port, LoaderType type, String version, UUID buildId) {
    public static ServerResponse fromServer(Server server) {
        return new ServerResponse(
                server.getId(),
                server.getName(),
                server.getDescription(),
                server.getHost(),
                server.getPort(),
                server.getType(),
                server.getVersion(),
                server.getBuild() != null ? server.getBuild().getId() : null
        );
    }
}
