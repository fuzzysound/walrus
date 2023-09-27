package com.fuzzysound.walrus.service;

import com.fuzzysound.walrus.task.model.Task;

import java.util.concurrent.CompletableFuture;

public interface TaskConsumerService {
    CompletableFuture<Void> consumeAsync(Task task);
}
