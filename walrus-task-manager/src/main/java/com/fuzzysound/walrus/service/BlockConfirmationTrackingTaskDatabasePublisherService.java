package com.fuzzysound.walrus.service;

import com.fuzzysound.walrus.task.TaskUtils;
import com.fuzzysound.walrus.task.model.Task;
import com.fuzzysound.walrus.task.model.TransactionBlockConfirmationStatus;
import com.fuzzysound.walrus.task.service.TaskService;
import com.fuzzysound.walrus.task.service.TaskProgressService;
import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import com.fuzzysound.walrus.web3.service.Web3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.fuzzysound.walrus.common.Web3Constants.*;

@Service
@RequiredArgsConstructor
public class BlockConfirmationTrackingTaskDatabasePublisherService
    implements BlockConfirmationTrackingTaskPublisherService {
    private final TaskService taskService;
    private final TaskProgressService taskProgressService;
    private final Web3Service web3Service;
    @Override
    public CompletableFuture<Void> publishAsync(BigInteger lastBlockNumber) {
        List<TransactionBlockConfirmationStatus> transactionBlockConfirmationStatusList =
                taskProgressService.getUncompletedTransactionBlockConfirmationStatusList();
        List<CompletableFuture<Void>> publisherFutures = transactionBlockConfirmationStatusList.stream()
                .map(transactionBlockConfirmationStatus -> CompletableFuture.runAsync(
                        () -> createAndPublishTask(transactionBlockConfirmationStatus, lastBlockNumber)
                ))
                .toList();
        return CompletableFuture.allOf(publisherFutures.toArray(new CompletableFuture[0]));
    }

    private void createAndPublishTask(TransactionBlockConfirmationStatus transactionBlockConfirmationStatus, BigInteger lastBlockNumber) {
        int numRound = lastBlockNumber
                .subtract(transactionBlockConfirmationStatus.getLastConfirmedBlockNumber())
                .min(BigInteger.valueOf(MAX_BLOCK_CONFIRMATION_COUNT - transactionBlockConfirmationStatus.getCurrentBlockConfirmationCount()))
                .max(BigInteger.ZERO)
                .intValue();
        for (int round = 1; round <= numRound; round++) {
            BigInteger witnessBlockNumber = transactionBlockConfirmationStatus.getLastConfirmedBlockNumber().add(BigInteger.valueOf(round));
            WalrusBlock witnessBlock = web3Service.getBlockByNumber(witnessBlockNumber);
            WalrusTransaction transaction = web3Service.getTransactionByHash(
                    transactionBlockConfirmationStatus.getTransactionHash()
            );
            Task task = TaskUtils.createBlockConfirmationTrackingTask(
                    witnessBlock,
                    transaction,
                    transactionBlockConfirmationStatus.getCurrentBlockConfirmationCount() + round
            );
            taskService.publishTask(task);
        }
    }
}
