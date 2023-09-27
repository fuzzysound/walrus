package com.fuzzysound.walrus.service;

import com.fuzzysound.walrus.facade.WalrusFacade;
import com.fuzzysound.walrus.task.model.taskSpec.DepositTrackingTaskSpec;
import com.fuzzysound.walrus.task.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DepositTrackingTaskDatabaseConsumerService
        implements DepositTrackingTaskConsumerService {
    private final WalrusFacade walrusFacade;

    @Override
    public CompletableFuture<Void> consumeAsync(Task task) {
        return CompletableFuture.runAsync(() -> consume(task));
    }

    private void consume(Task task) {
        if (task.getTaskSpec() instanceof DepositTrackingTaskSpec taskSpec) {
            walrusFacade.postDeposit(
                    taskSpec.getBlock(),
                    taskSpec.getTransaction()
            );
        } else {
            throw new IllegalArgumentException("Not supported task type.");
        }
    }
}
