package com.fuzzysound.walrus.task;

import com.fuzzysound.walrus.task.model.taskSpec.BlockConfirmationTrackingTaskSpec;
import com.fuzzysound.walrus.task.model.taskSpec.DepositTrackingTaskSpec;
import com.fuzzysound.walrus.task.model.Task;
import com.fuzzysound.walrus.task.model.taskSpec.WithdrawalTrackingTaskSpec;
import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;

public class TaskUtils {
    public static Task createDepositTrackingTask(WalrusBlock block, WalrusTransaction transaction) {
        DepositTrackingTaskSpec taskSpec = DepositTrackingTaskSpec.builder()
                .block(block)
                .transaction(transaction)
                .build();
        return Task.builder()
                .taskSpec(taskSpec)
                .build();
    }

    public static Task createWithdrawalTrackingTask(WalrusBlock block, WalrusTransaction transaction) {
        WithdrawalTrackingTaskSpec taskSpec = WithdrawalTrackingTaskSpec.builder()
                .block(block)
                .transaction(transaction)
                .build();
        return Task.builder()
                .taskSpec(taskSpec)
                .build();
    }

    public static Task createBlockConfirmationTrackingTask(
            WalrusBlock witnessBlock,
            WalrusTransaction transaction,
            int blockConfirmationCount
    ) {
        BlockConfirmationTrackingTaskSpec taskSpec = BlockConfirmationTrackingTaskSpec.builder()
                .witnessBlock(witnessBlock)
                .transaction(transaction)
                .blockConfirmationCount(blockConfirmationCount)
                .build();
        return Task.builder()
                .taskSpec(taskSpec)
                .build();
    }
}
