package com.ling.packservice.pack.build;

import com.ling.packservice.pack.build.BuildFile.BuildFileRepository;
import com.ling.packservice.pack.build.BuildFile.dto.BuildFileResponse;
import com.ling.packservice.pack.build.dto.BuildDownload;
import com.ling.packservice.pack.build.dto.BuildRequest;
import com.ling.packservice.pack.build.dto.BuildResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/build")
public class BuildController {
    private final BuildService buildService;
    private final BuildFileRepository buildFileRepository;

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BuildResponse> create(@ModelAttribute BuildRequest request) throws IOException, NoSuchAlgorithmException {
        Build build = buildService.create(request);
        return ResponseEntity.ok(BuildResponse.fromBuild(build));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<BuildResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<BuildResponse> responses =
                buildService
                        .getPage(PageRequest.of(page, Math.min(size, 50)))
                        .map(BuildResponse::fromBuild);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{buildId}")
    public ResponseEntity<BuildResponse> getById(@PathVariable UUID buildId) {
        return ResponseEntity.ok(BuildResponse.fromBuild(buildService.getById(buildId)));
    }

    @GetMapping("/{buildId}/files")
    public ResponseEntity<List<BuildFileResponse>> manifest(@PathVariable UUID buildId) {
        List<BuildFileResponse> responses = buildFileRepository.findAllByBuildId(buildId).stream().map(BuildFileResponse::fromBuildFile).toList();
        if (responses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{buildId}/files/download")
    public ResponseEntity<StreamingResponseBody> downloadAll(@PathVariable UUID buildId) {
        StreamingResponseBody body = outputStream -> buildService.downloadAll(buildId, outputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"build.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(body);
    }

    @GetMapping("/{buildId}/file")
    public ResponseEntity<StreamingResponseBody> getFileByPath(
            @PathVariable UUID buildId,
            @RequestParam String path
    ) {
        StreamingResponseBody body =
                outputStream -> buildService.download(
                        buildId,
                        path,
                        outputStream
                );

        String fileName = Path.of(path)
                .getFileName()
                .toString();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }
}
