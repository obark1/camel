package com.prep.camel.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "rejected_import")
public class RejectedImportRowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long id;

    @Column(name = "raw_line", length = 500)
    private String rawLine;

    @Column
    private String reason;

    @Column
    private Instant created;

}
