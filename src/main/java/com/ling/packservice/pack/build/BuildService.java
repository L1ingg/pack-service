package com.ling.packservice.pack.build;

import com.ling.packservice.pack.build.BuildFile.BuildFile;
import com.ling.packservice.pack.build.BuildFile.BuildFileRepository;
import com.ling.packservice.pack.build.blob.Blob;
import com.ling.packservice.pack.build.blob.BlobRepository;
import com.ling.packservice.pack.build.dto.BuildRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class BuildService {

    private final BuildRepository buildRepository;
    private final BlobRepository blobRepository;
    private final BuildFileRepository buildFileRepository;
    private final S3Client s3Client;

    public void downloadAll(UUID buildId, OutputStream outputStream) throws IOException {
        if (!buildRepository.existsById(buildId)) throw new EntityNotFoundException();
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            for (BuildFile file : buildFileRepository.findAllByBuildId(buildId)) {
                ZipEntry entry = new ZipEntry(file.getPath());
                zip.putNextEntry(entry);

                try (ResponseInputStream<GetObjectResponse> input =
                             s3Client.getObject(
                                     GetObjectRequest.builder()
                                             .bucket("packs")
                                             .key(getBlobKey(file.getBlob()))
                                             .build()
                             )) {

                    input.transferTo(zip);
                }

                zip.closeEntry();

            }
            zip.finish();
        }
    }

    public void download(UUID buildId, String path, OutputStream outputStream) throws IOException {
        BuildFile file = buildFileRepository.findByBuildIdAndPath(buildId, path).orElseThrow();

        try (ResponseInputStream<GetObjectResponse> input =
                     s3Client.getObject(
                             GetObjectRequest.builder()
                                     .bucket("packs")
                                     .key(getBlobKey(file.getBlob()))
                                     .build()
                     )) {

            input.transferTo(outputStream);
        }
    }

    private String getBlobKey(Blob blob) {
        String hash = blob.getHash();

        return "blobs/"
                + hash.substring(0, 2)
                + "/"
                + hash;
    }

    @Transactional
    public Build create(BuildRequest request) throws IOException, NoSuchAlgorithmException {
        Path tempDir = Files.createTempDirectory("build-");
        Path zipPath = tempDir.resolve("upload.zip");
        Path unpackDir = tempDir.resolve("unpacked");

        Files.createDirectories(unpackDir);

        try (InputStream in = request.file().getInputStream()) {
            Files.copy(in, zipPath, StandardCopyOption.REPLACE_EXISTING);
        }

        try {
            unzip(zipPath, unpackDir);

            List<BuildFile> buildFiles = new ArrayList<>();

            try (var walk = Files.walk(unpackDir)) {
                for (Path file : walk.filter(Files::isRegularFile).toList()) {
                    Path relativePath = unpackDir.relativize(file);
                    String hash = sha256(file);
                    long size = Files.size(file);

                    Blob blob = blobRepository.findByHash(hash)
                            .orElseGet(() -> {
                                String key = "blobs/"
                                        + hash.substring(0, 2)
                                        + "/"
                                        + hash;

                                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                        .bucket("packs")
                                        .key(key)
                                        .contentType("application/octet-stream")
                                        .contentLength(size)
                                        .build();

                                s3Client.putObject(
                                        putObjectRequest,
                                        RequestBody.fromFile(file)
                                );

                                return blobRepository.save(new Blob(hash, size));
                            });



                    BuildFile buildFile = BuildFile.builder()
                            .blob(blob)
                            .path(relativePath.toString().replace('\\', '/'))
                            .build();

                    buildFiles.add(buildFile);
                }
            }

            Build build = Build.builder()
                    .name(request.name())
                    .description(request.description())
                    .hash(generateBuildHash(buildFiles))
                    .files(buildFiles)
                    .build();

            buildFiles.forEach(file -> file.setBuild(build));

            return buildRepository.save(build);
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private String sha256(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    private String generateBuildHash(List<BuildFile> files) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        files.stream()
                .sorted(Comparator.comparing(BuildFile::getPath))
                .forEach(file -> {
                    String line =
                            file.getPath() + "\0"
                                    + file.getBlob().getHash() + "\0"
                                    + file.getBlob().getSize() + "\0";

                    digest.update(line.getBytes(StandardCharsets.UTF_8));
                });

        return HexFormat.of().formatHex(digest.digest());
    }

    private static void unzip(Path archive, Path destination) throws IOException {
        Files.createDirectories(destination);

        try (InputStream input = Files.newInputStream(archive);
             ZipInputStream zip = new ZipInputStream(input)) {

            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Path output = destination.resolve(entry.getName()).normalize();

                if (!output.startsWith(destination.normalize())) {
                    throw new IOException("Некорректный путь в архиве: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(output);
                } else {
                    Files.createDirectories(output.getParent());

                    try (OutputStream out = Files.newOutputStream(output)) {
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = zip.read(buffer)) != -1) {
                            out.write(buffer, 0, length);
                        }
                    }
                }

                zip.closeEntry();
            }
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (Files.notExists(root)) {
            return;
        }

        try (var walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException io) {
                throw io;
            }
            throw e;
        }
    }

    public Build getById(UUID id) {
        return buildRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Build not found"));
    }

    public Page<Build> getPage(Pageable pageable) {
        return buildRepository.findAll(pageable);
    }
}