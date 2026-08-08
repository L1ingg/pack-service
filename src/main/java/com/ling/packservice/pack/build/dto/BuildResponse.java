package com.ling.packservice.pack.build.dto;

import com.ling.packservice.pack.build.Build;

import java.util.UUID;

public record BuildResponse(UUID id, String name, String hash, String description) {
    public static BuildResponse fromBuild(Build build) {
        return new BuildResponse(build.getId(), build.getName(), build.getHash(), build.getDescription());
    }
}
