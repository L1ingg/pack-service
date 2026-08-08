package com.ling.packservice.pack.build.dto;

import org.springframework.web.multipart.MultipartFile;

public record BuildRequest(String name, String description, MultipartFile file) {
}
