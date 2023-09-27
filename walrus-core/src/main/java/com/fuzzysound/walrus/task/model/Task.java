package com.fuzzysound.walrus.task.model;

import com.fuzzysound.walrus.task.model.taskSpec.TaskSpec;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Task {
    private final TaskSpec taskSpec;
}
