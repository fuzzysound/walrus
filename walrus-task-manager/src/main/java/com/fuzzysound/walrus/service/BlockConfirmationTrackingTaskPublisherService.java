package com.fuzzysound.walrus.service;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

public interface BlockConfirmationTrackingTaskPublisherService {
    CompletableFuture<Void> publishAsync(BigInteger lastBlockNumber);
}
