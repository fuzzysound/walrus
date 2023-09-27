package com.fuzzysound.walrus.web3.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigInteger;
import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class WalrusBlock {
    private final String blockHash;
    private final BigInteger blockNumber;
    private final Long timestamp;
    private final List<WalrusTransaction> transactions;
}
