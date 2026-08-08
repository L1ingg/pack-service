package com.ling.packservice.pack.dto;

import com.ling.packservice.pack.Pack;

public record PackCreate(String name, String description) {
    public Pack toPack() {
        return Pack.builder()
                .name(name)
                .description(description)
                .build();
    }
}
