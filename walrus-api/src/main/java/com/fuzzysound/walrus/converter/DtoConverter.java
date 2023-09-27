package com.fuzzysound.walrus.converter;

import com.fuzzysound.walrus.dto.EventHistoryResponseDto;
import com.fuzzysound.walrus.dto.WalletResponseDto;
import com.fuzzysound.walrus.common.NumberUtils;
import com.fuzzysound.walrus.event.model.EventHistory;
import com.fuzzysound.walrus.wallet.model.WalrusWallet;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DtoConverter {
    public WalletResponseDto toWalletResponseDto(WalrusWallet wallet) {
        return WalletResponseDto.builder()
                .username(wallet.getUsername())
                .address(wallet.getAddress())
                .balance(NumberUtils.weiToEther(wallet.getBalance().toString()))
                .build();
    }

    public EventHistoryResponseDto toEventHistoryResponseDto(List<EventHistory> eventHistories) {
        return EventHistoryResponseDto.builder()
                .eventHistory(eventHistories.stream().map(this::toEventHistoryDto).toList())
                .build();
    }

    public EventHistoryResponseDto.EventHistoryDto toEventHistoryDto(EventHistory eventHistory) {
        EventHistoryResponseDto.EventType eventType =
                (eventHistory.getAddress().equals(eventHistory.getTransaction().getFrom()))
                ? EventHistoryResponseDto.EventType.WITHDRAWAL
                : EventHistoryResponseDto.EventType.DEPOSIT;
        return EventHistoryResponseDto.EventHistoryDto.builder()
                .eventType(eventType)
                .transactionStatus(eventHistory.getTransactionStatus())
                .blockConfirmationCount(eventHistory.getBlockConfirmationCount())
                .build();
    }
}
