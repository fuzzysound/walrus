package com.fuzzysound.walrus.wallet.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigInteger;

@Getter
@Builder
public class WalrusWallet {
    private final String username;
    private final String password;
    private final String filePath;
    private final String address;
    private final BigInteger balance;
}
