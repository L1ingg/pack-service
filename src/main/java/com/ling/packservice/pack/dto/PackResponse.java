package com.ling.packservice.pack.dto;

import com.ling.packservice.pack.Pack;
import com.ling.packservice.pack.build.Build;

import java.util.List;
import java.util.UUID;

public record PackResponse(UUID id, String name, String description, List<UUID> buildIds) {
    public static PackResponse fromPack(Pack pack) {
        List<UUID> ids = pack.getBuilds().stream().map(Build::getId).toList();
        return new PackResponse(pack.getId(), pack.getName(), pack.getDescription(), ids);
    }
}
