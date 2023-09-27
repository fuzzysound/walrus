package com.fuzzysound.walrus.dto;

import com.fuzzysound.walrus.event.model.TransactionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@Jacksonized
public class EventHistoryResponseDto {
    List<EventHistoryDto> eventHistory;

    @Getter
    @Builder
    @Jacksonized
    public static class EventHistoryDto {
        private final EventType eventType;
        private final TransactionStatus transactionStatus;
        private final int blockConfirmationCount;
    }

    public enum EventType {
        DEPOSIT("입금"),
        WITHDRAWAL("출금");

        private final String desc;

        EventType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
