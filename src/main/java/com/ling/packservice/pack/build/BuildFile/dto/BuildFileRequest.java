package com.ling.packservice.pack.build.BuildFile.dto;

import com.ling.packservice.pack.build.Build;
import com.ling.packservice.pack.build.BuildFile.BuildFile;
import com.ling.packservice.pack.build.blob.Blob;

public record BuildFileRequest(String path, Blob blob, Build build) {
    public BuildFile toBuildFile() {
        return BuildFile.builder()
                .blob(blob)
                .path(path)
                .build(build)
                .build();
    }
}
