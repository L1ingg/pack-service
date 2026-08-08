package com.ling.packservice.pack.build.BuildFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuildFileRepository extends JpaRepository<BuildFile, UUID> {
    List<BuildFile> findAllByBuildId(UUID buildId);
    Optional<BuildFile> findByBuildIdAndPath(UUID buildId, String path);
}
