package com.ling.packservice.pack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PackRepository extends JpaRepository<Pack, UUID> {
}
