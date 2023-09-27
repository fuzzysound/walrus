package com.fuzzysound.walrus.task.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "last_confirmed_block_number", indexes = {
        @Index(name = "idx__dist_key", columnList = "dist_key")
})
@Getter
@Setter
public class LastConfirmedBlockNumberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dist_key", nullable = false, unique = true)
    private String distKey;

    @Column(name = "block_number", nullable = false)
    private String blockNumber;
}
