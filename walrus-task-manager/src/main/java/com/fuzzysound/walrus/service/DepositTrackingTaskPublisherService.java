package com.fuzzysound.walrus.service;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

public interface DepositTrackingTaskPublisherService {
    CompletableFuture<Void> publishAsync(BigInteger startBlockNumber, BigInteger endBlockNumber);
}
