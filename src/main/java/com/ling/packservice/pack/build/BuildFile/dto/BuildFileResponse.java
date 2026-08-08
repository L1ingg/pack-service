package com.ling.packservice.pack.build.BuildFile.dto;

import com.ling.packservice.pack.build.BuildFile.BuildFile;

import java.util.UUID;

public record BuildFileResponse(UUID id, String hash, String path) {
    public static BuildFileResponse fromBuildFile(BuildFile file) {
        return new BuildFileResponse(file.getId(), file.getBlob().getHash(), file.getPath());
    }
}
