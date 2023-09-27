package com.fuzzysound.walrus;

import com.fuzzysound.walrus.service.DepositTrackingTaskPublisherService;
import com.fuzzysound.walrus.task.service.TaskProgressService;
import com.fuzzysound.walrus.web3.service.Web3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositTrackingTaskManager {
    private final Web3Service web3Service;
    private final TaskProgressService taskProgressService;
    private final DepositTrackingTaskPublisherService depositTrackingTaskPublisherService;

    @Scheduled(fixedDelayString = "${walrus.task-manager.delay.deposit-tracking}")
    public void run() {
        log.info("DepositTrackingTaskManager running...");
        BigInteger lastBlockNumber = web3Service.getLastBlockNumber();
        BigInteger lastConfirmedBlockNumber = taskProgressService.getLastConfirmedBlockNumber(
                WalrusTaskManagerKey.DEPOSIT_TRACKING.name()
        );
        if (lastConfirmedBlockNumber == null) {
            taskProgressService.updateLastConfirmedBlockNumber(
                    WalrusTaskManagerKey.DEPOSIT_TRACKING.name(), lastBlockNumber
            );
            log.info("DepositTrackingTaskManager done, started counting from block number {}.", lastBlockNumber);
        } else {
            BigInteger startBlockNumber = lastConfirmedBlockNumber.add(BigInteger.ONE);
            try {
                depositTrackingTaskPublisherService.publishAsync(startBlockNumber, lastBlockNumber)
                        .join();
                taskProgressService.updateLastConfirmedBlockNumber(
                        WalrusTaskManagerKey.DEPOSIT_TRACKING.name(), lastBlockNumber
                );
                log.info("DepositTrackingTaskManager done, block number from {} to {}.",
                        startBlockNumber, lastBlockNumber);
            } catch (CancellationException | CompletionException e) {
                log.error("DepositTrackingTaskManager run failed, block number from {} to {}.",
                        startBlockNumber, lastBlockNumber, e);
            }
        }
    }
}
