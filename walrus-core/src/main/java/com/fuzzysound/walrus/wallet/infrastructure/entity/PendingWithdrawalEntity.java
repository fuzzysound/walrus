package com.fuzzysound.walrus.wallet.infrastructure.entity;

import com.fuzzysound.walrus.wallet.model.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pending_withdrawal", indexes = {
        @Index(name = "idx__transaction_hash", columnList = "transaction_hash", unique = true),
        @Index(name = "idx__address", columnList = "address")
})
@Getter
@Setter
public class PendingWithdrawalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_hash", nullable = false, unique = true)
    private String transactionHash;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "pending_value", nullable = false)
    private String pendingValue;

    @Column(name = "withdrawal_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private WithdrawalStatus withdrawalStatus;

    @Version
    private Integer version;
}
