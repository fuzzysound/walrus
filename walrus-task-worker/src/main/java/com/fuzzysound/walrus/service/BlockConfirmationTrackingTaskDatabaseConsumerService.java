package com.fuzzysound.walrus.service;

import com.fuzzysound.walrus.facade.WalrusFacade;
import com.fuzzysound.walrus.task.model.taskSpec.BlockConfirmationTrackingTaskSpec;
import com.fuzzysound.walrus.task.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class BlockConfirmationTrackingTaskDatabaseConsumerService
    implements BlockConfirmationTrackingTaskConsumerService {
    private final WalrusFacade walrusFacade;

    @Override
    public CompletableFuture<Void> consumeAsync(Task task) {
        return CompletableFuture.runAsync(() -> consume(task));
    }

    private void consume(Task task) {
        if (task.getTaskSpec() instanceof BlockConfirmationTrackingTaskSpec taskSpec) {
            walrusFacade.updateBlockConfirmation(
                    taskSpec.getWitnessBlock(),
                    taskSpec.getTransaction(),
                    taskSpec.getBlockConfirmationCount()
            );
        } else {
            throw new IllegalArgumentException("Not supported task type.");
        }
    }
}
