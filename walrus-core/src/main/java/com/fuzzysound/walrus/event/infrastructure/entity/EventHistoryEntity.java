package com.fuzzysound.walrus.event.infrastructure.entity;

import com.fuzzysound.walrus.event.model.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event_history", indexes = {
        @Index(name = "idx__transaction_hash__address__block_confirmation_count", columnList = "transaction_hash, address, block_confirmation_count", unique = true),
        @Index(name = "idx__address__timestamp", columnList = "address, timestamp")
})
@Getter
@Setter
public class EventHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_hash", nullable = false)
    private String transactionHash;

    @Column(name = "fromAddress", nullable = false)
    private String fromAddress;

    @Column(name = "toAddress", nullable = false)
    private String toAddress;

    @Column(name = "transferred_value", nullable = false)
    private String transferredValue;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "transaction_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Column(name = "block_confirmation_count", nullable = false)
    private Integer blockConfirmationCount;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;
}
