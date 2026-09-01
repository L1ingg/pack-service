package com.ling.packservice.server;

import com.ling.packservice.pack.build.Build;
import com.ling.packservice.pack.build.BuildRepository;
import com.ling.packservice.pack.build.dto.BuildResponse;
import com.ling.packservice.server.dto.ServerCreate;
import com.ling.packservice.server.dto.ServerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/server")
public class ServerController {
    private final ServerRepository serverRepository;
    private final BuildRepository buildRepository;

    @GetMapping("/{serverId}")
    public ResponseEntity<ServerResponse> getById(@PathVariable UUID serverId) {
        return serverRepository.findById(serverId)
                .map(ServerResponse::fromServer)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/list")
    public ResponseEntity<List<ServerResponse>> getALl() {
        List<ServerResponse> servers = serverRepository.findAll().stream()
                .map(ServerResponse::fromServer)
                .toList();
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/{serverId}/build")
    public ResponseEntity<BuildResponse> getBuild(@PathVariable UUID serverId) {
        return serverRepository.findById(serverId)
                .map(server -> BuildResponse.fromBuild(server.getBuild()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<ServerResponse> create(@ModelAttribute ServerCreate dto) {
        Server server = this.serverRepository.save(dto.toServer());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ServerResponse.fromServer(server)
                );
    }

    @PutMapping("{serverId}/build")
    public ResponseEntity<?> setBuild(@PathVariable UUID serverId, @RequestParam UUID buildId) {

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Server not found"));

        Build build = buildRepository.findById(buildId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Build not found"));

        server.setBuild(build);
        serverRepository.save(server);

        return ResponseEntity.ok().build();
    }
}
