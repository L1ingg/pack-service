package com.ling.packservice.pack.build;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BuildRepository extends JpaRepository<Build, UUID> {
    Page<Build> findAll(Pageable pageable);
}
