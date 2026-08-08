package com.ling.packservice.pack.build;

import com.ling.packservice.pack.build.BuildFile.BuildFile;
import com.ling.packservice.server.Server;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Entity
@Data
@NoArgsConstructor
public class Build {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, unique = true, length = 64)
    private String hash;

    @OneToMany(
            mappedBy = "build",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BuildFile> files;

    @OneToMany(mappedBy = "build")
    private List<Server> servers;
}
