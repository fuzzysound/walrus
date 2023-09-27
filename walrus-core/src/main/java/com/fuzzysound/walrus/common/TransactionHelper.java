package com.fuzzysound.walrus.common;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
public class TransactionHelper {
    @Transactional
    public <T> T executeTransaction(Supplier<T> supplier) {
        return supplier.get();
    }

    @Transactional
    public void executeTransaction(Runnable runnable) {
        runnable.run();
    }
}
