package com.ling.packservice.pack.build.BuildFile;

import com.ling.packservice.pack.build.Build;
import com.ling.packservice.pack.build.blob.Blob;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"build_id", "path"}
        )
)
public class BuildFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private Build build;

    @ManyToOne(optional = false)
    private Blob blob;

    @Column(nullable = false)
    private String path;
}
