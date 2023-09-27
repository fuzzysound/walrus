package com.fuzzysound.walrus.task.model.taskSpec;

import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class BlockConfirmationTrackingTaskSpec implements TaskSpec {
    private final WalrusBlock witnessBlock;
    private final WalrusTransaction transaction;
    private final int blockConfirmationCount;
}
