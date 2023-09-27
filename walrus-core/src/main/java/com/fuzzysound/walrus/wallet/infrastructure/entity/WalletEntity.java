package com.fuzzysound.walrus.wallet.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wallet", indexes = {
        @Index(name = "idx__username", columnList = "username", unique = true),
        @Index(name = "idx__address", columnList = "address", unique = true)
})
@Getter
@Setter
public class WalletEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 30, nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String base64Password;

    @Column(name = "file_path", nullable = false, unique = true)
    private String filePath;

    @Column(name = "address", nullable = false, unique = true)
    private String address;

    @Column(name = "balance", nullable = false)
    private String balance;
}
