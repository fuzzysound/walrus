package com.fuzzysound.walrus;

import com.fuzzysound.walrus.service.BlockConfirmationTrackingTaskPublisherService;
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
public class BlockConfirmationTrackingTaskManager {
    private final Web3Service web3Service;
    private final BlockConfirmationTrackingTaskPublisherService blockConfirmationTrackingTaskPublisherService;

    @Scheduled(fixedDelayString = "${walrus.task-manager.delay.block-confirmation-tracking}")
    public void run() {
        log.info("BlockConfirmationTrackingTaskManager running...");
        BigInteger lastBlockNumber = web3Service.getLastBlockNumber();
        try {
            blockConfirmationTrackingTaskPublisherService.publishAsync(lastBlockNumber)
                    .join();
            log.info("BlockConfirmationTrackingTaskManager done, counting up to block number {}.", lastBlockNumber);
        } catch (CancellationException | CompletionException e) {
            log.error("BlockConfirmationTrackingTaskManager run failed, block to {}.", lastBlockNumber, e);
        }
    }
}
