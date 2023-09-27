package com.fuzzysound.walrus.service;

import com.fuzzysound.walrus.facade.WalrusFacade;
import com.fuzzysound.walrus.task.model.Task;
import com.fuzzysound.walrus.task.model.taskSpec.WithdrawalTrackingTaskSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class WithdrawalTrackingTaskDatabaseConsumerService
        implements WithdrawalTrackingTaskConsumerService{
    private final WalrusFacade walrusFacade;
    @Override
    public CompletableFuture<Void> consumeAsync(Task task) {
        return CompletableFuture.runAsync(() -> consume(task));
    }

    private void consume(Task task) {
        if (task.getTaskSpec() instanceof WithdrawalTrackingTaskSpec taskSpec) {
            walrusFacade.postWithdraw(
                    taskSpec.getBlock(),
                    taskSpec.getTransaction(),
                    taskSpec.getBlock().getTimestamp()
            );
        } else {
            throw new IllegalArgumentException("Not supported task type.");
        }
    }
}
