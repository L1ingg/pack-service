package com.ling.packservice.server;

import com.ling.packservice.LoaderType;
import com.ling.packservice.pack.build.Build;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;

    private String host;
    private Integer port;

    @Enumerated(EnumType.STRING)
    private LoaderType type;

    private String version;

    @ManyToOne
    private Build build;


}
