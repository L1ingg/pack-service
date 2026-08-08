package com.ling.packservice.pack.build.blob;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlobRepository extends JpaRepository<Blob, UUID> {
    boolean existsBlobByHash(String hash);
    Optional<Blob> findByHash(String hash);
}
