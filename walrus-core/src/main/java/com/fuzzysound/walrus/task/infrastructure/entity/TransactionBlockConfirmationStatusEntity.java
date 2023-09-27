package com.fuzzysound.walrus.task.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transaction_block_confirmation_status", indexes = {
        @Index(name = "idx__transaction_hash", columnList = "transaction_hash"),
        @Index(name = "idx__current_block_confirmation_count", columnList = "current_block_confirmation_count")
})
@Getter
@Setter
public class TransactionBlockConfirmationStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_hash", nullable = false, unique = true)
    private String transactionHash;

    @Column(name = "last_confirmed_block_number", nullable = false)
    private String lastConfirmedBlockNumber;

    @Column(name = "current_block_confirmation_count", nullable = false)
    private Integer currentBlockConfirmationCount;

    @Column(name = "isSettled", nullable = false)
    private Boolean isSettled;

    @Version
    private Integer version;
}
