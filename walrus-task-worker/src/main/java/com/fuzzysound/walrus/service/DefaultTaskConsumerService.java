package com.fuzzysound.walrus.service;

import com.fuzzysound.walrus.common.exception.ConsistencyException;
import com.fuzzysound.walrus.task.model.taskSpec.BlockConfirmationTrackingTaskSpec;
import com.fuzzysound.walrus.task.model.taskSpec.DepositTrackingTaskSpec;
import com.fuzzysound.walrus.task.model.Task;
import com.fuzzysound.walrus.exception.RetryableTaskException;
import com.fuzzysound.walrus.exception.UnretryableTaskException;
import com.fuzzysound.walrus.task.model.taskSpec.WithdrawalTrackingTaskSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Primary
@RequiredArgsConstructor
public class DefaultTaskConsumerService implements TaskConsumerService {
    private final BlockConfirmationTrackingTaskConsumerService blockConfirmationTrackingTaskConsumerService;
    private final DepositTrackingTaskConsumerService depositTrackingTaskConsumerService;
    private final WithdrawalTrackingTaskConsumerService withdrawalTrackingTaskConsumerService;

    @Override
    public CompletableFuture<Void> consumeAsync(Task task) {
        return consumeAsyncInternal(task).handle((res, ex) -> {
            if (ex == null) {
                return res;
            } else if (ex instanceof IllegalArgumentException ||
                    ex instanceof ConsistencyException) {
                throw new UnretryableTaskException(ex);
            } else {
                throw new RetryableTaskException(ex);
            }
        });
    }

    private CompletableFuture<Void> consumeAsyncInternal(Task task) {
        if (task.getTaskSpec() instanceof BlockConfirmationTrackingTaskSpec) {
            return blockConfirmationTrackingTaskConsumerService.consumeAsync(task);
        } else if (task.getTaskSpec() instanceof DepositTrackingTaskSpec) {
            return depositTrackingTaskConsumerService.consumeAsync(task);
        } else if (task.getTaskSpec() instanceof WithdrawalTrackingTaskSpec) {
            return withdrawalTrackingTaskConsumerService.consumeAsync(task);
        } else {
            return CompletableFuture.runAsync(() -> {
                throw new IllegalArgumentException("Not supported task type.");
            });
        }
    }
}
