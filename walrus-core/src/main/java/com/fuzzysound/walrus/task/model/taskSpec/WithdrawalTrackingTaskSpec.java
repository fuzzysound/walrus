package com.fuzzysound.walrus.task.model.taskSpec;

import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class WithdrawalTrackingTaskSpec implements TaskSpec {
    private final WalrusBlock block;
    private final WalrusTransaction transaction;
}
