package com.fuzzysound.walrus.service;

import com.fuzzysound.walrus.common.NumberUtils;
import com.fuzzysound.walrus.task.TaskUtils;
import com.fuzzysound.walrus.task.service.TaskService;
import com.fuzzysound.walrus.wallet.service.WalletService;
import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import com.fuzzysound.walrus.web3.service.Web3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DepositTrackingTaskDatabasePublisherService
        implements DepositTrackingTaskPublisherService {
    private final Web3Service web3Service;
    private final WalletService walletService;
    private final TaskService taskService;
    @Override
    public CompletableFuture<Void> publishAsync(BigInteger startBlockNumber, BigInteger endBlockNumber) {
        List<BigInteger> targetBlockNumbers = NumberUtils.getRangeList(
                startBlockNumber, endBlockNumber.add(BigInteger.ONE)
        );
        List<CompletableFuture<Void>> publisherFutures = targetBlockNumbers.stream()
                .map(blockNumber -> CompletableFuture.runAsync(() -> createAndPublishTask(blockNumber)))
                .toList();
        return CompletableFuture.allOf(publisherFutures.toArray(new CompletableFuture[0]));
    }

    private void createAndPublishTask(BigInteger blockNumber) {
        WalrusBlock block = web3Service.getBlockByNumber(blockNumber);
        List<WalrusTransaction> depositTransactions = walletService.filterTransactionsDepositToWalrus(block.getTransactions());
        depositTransactions.stream().map(transaction -> TaskUtils.createDepositTrackingTask(block, transaction))
                .forEach(taskService::publishTask);
    }
}
