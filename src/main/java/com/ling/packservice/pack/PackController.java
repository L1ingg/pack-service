package com.ling.packservice.pack;

import com.ling.packservice.pack.dto.PackCreate;
import com.ling.packservice.pack.dto.PackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pack")
public class PackController {
    private final PackRepository packRepository;

    @GetMapping("/{packId}")
    public ResponseEntity<PackResponse> getById(@PathVariable UUID packId) {
        return packRepository.findById(packId)
                .map(PackResponse::fromPack)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PostMapping("/create")
    public ResponseEntity<PackResponse> create(@ModelAttribute PackCreate dto) {
        Pack pack = packRepository.save(dto.toPack());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PackResponse.fromPack(pack)
                );
    }
}
