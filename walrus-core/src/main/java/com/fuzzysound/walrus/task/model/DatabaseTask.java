package com.fuzzysound.walrus.task.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DatabaseTask {
    private final long taskId;
    private final TaskType taskType;
    private final TaskStatus status;
    private final int age;
    private final Task task;
}
