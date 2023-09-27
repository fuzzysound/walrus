package com.fuzzysound.walrus.event.service;

import com.fuzzysound.walrus.event.model.EventHistory;

import java.util.List;

public interface EventHistoryService {
    void addEventHistory(EventHistory eventHistory);

    List<EventHistory> getEventHistories(String address, long from, long to, int size);

    List<EventHistory> getEventHistories(String address, int size);
}
