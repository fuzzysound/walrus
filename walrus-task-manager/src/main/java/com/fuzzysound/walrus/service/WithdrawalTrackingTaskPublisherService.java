package com.fuzzysound.walrus.service;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

public interface WithdrawalTrackingTaskPublisherService {
    CompletableFuture<Void> publishAsync(BigInteger startBlockNumber, BigInteger endBlockNumber);
}
