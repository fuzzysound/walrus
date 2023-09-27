package com.fuzzysound.walrus.web3.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigInteger;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class WalrusTransaction {
    private final String transactionHash;
    private final String from;
    private final String to;
    private final BigInteger value;
}
