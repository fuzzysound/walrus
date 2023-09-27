package com.fuzzysound.walrus.event.service;

import com.fuzzysound.walrus.event.infrastructure.converter.EventHistoryEntityConverter;
import com.fuzzysound.walrus.event.infrastructure.entity.EventHistoryEntity;
import com.fuzzysound.walrus.event.infrastructure.repository.EventHistoryRepository;
import com.fuzzysound.walrus.event.model.EventHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultEventHistoryService implements EventHistoryService {
    private final EventHistoryEntityConverter eventHistoryEntityConverter;
    private final EventHistoryRepository eventHistoryRepository;
    @Override
    public void addEventHistory(EventHistory eventHistory) {
        Optional<EventHistoryEntity> alreadyAdded = eventHistoryRepository
                .findByTransactionHashAndAddressAndBlockConfirmationCount(
                eventHistory.getTransaction().getTransactionHash(),
                eventHistory.getAddress(),
                eventHistory.getBlockConfirmationCount()
        );
        if (alreadyAdded.isPresent()) {
            return;
        }
        EventHistoryEntity eventHistoryEntity = eventHistoryEntityConverter.toEntity(eventHistory);
        eventHistoryRepository.save(eventHistoryEntity);
    }

    @Override
    public List<EventHistory> getEventHistories(String address, long from, long to, int size) {
        Pageable pageable = PageRequest.of(0, size);
        return eventHistoryRepository.findAllByAddressAndTimestampAfterAndTimestampBeforeOrderByTimestampDesc(
                address, from, to, pageable
        ).stream().map(eventHistoryEntityConverter::fromEntity).toList();
    }

    @Override
    public List<EventHistory> getEventHistories(String address, int size) {
        Pageable pageable = PageRequest.of(0, size);
        return eventHistoryRepository.findAllByAddressOrderByTimestampDesc(address, pageable).stream()
                .map(eventHistoryEntityConverter::fromEntity).toList();
    }


}
