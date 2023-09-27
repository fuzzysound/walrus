package com.fuzzysound.walrus.task.infrastructure.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuzzysound.walrus.task.infrastructure.entity.TaskEntity;
import com.fuzzysound.walrus.task.model.Task;
import com.fuzzysound.walrus.task.model.taskSpec.BlockConfirmationTrackingTaskSpec;
import com.fuzzysound.walrus.task.model.taskSpec.DepositTrackingTaskSpec;
import com.fuzzysound.walrus.task.model.taskSpec.TaskSpec;
import com.fuzzysound.walrus.task.model.DatabaseTask;
import com.fuzzysound.walrus.task.model.TaskStatus;
import com.fuzzysound.walrus.task.model.TaskType;
import com.fuzzysound.walrus.task.model.taskSpec.WithdrawalTrackingTaskSpec;
import org.springframework.stereotype.Component;

@Component
public class TaskEntityConverter {
    private final ObjectMapper objectMapper;

    public TaskEntityConverter() {
        this.objectMapper = new ObjectMapper();
    }
    public TaskEntity toEntity(Task model) {
        TaskSpec taskSpec = model.getTaskSpec();
        TaskType taskType = getTaskType(taskSpec);
        try {
            TaskEntity entity = new TaskEntity();
            entity.setTaskType(taskType);
            entity.setStatus(TaskStatus.READY);
            entity.setAge(0);
            entity.setTaskSpec(objectMapper.writeValueAsString(model.getTaskSpec()));
            return entity;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Task spec is invalid.", e);
        }
    }

    public DatabaseTask fromEntity(TaskEntity entity) {
        TaskSpec taskSpec = getTaskSpec(entity.getTaskType(), entity.getTaskSpec());
        Task task = Task.builder()
                .taskSpec(taskSpec)
                .build();
        return DatabaseTask.builder()
                .taskId(entity.getId())
                .taskType(entity.getTaskType())
                .status(entity.getStatus())
                .age(entity.getAge())
                .task(task)
                .build();
    }

    private TaskType getTaskType(TaskSpec taskSpec) {
        if (taskSpec instanceof BlockConfirmationTrackingTaskSpec) {
            return TaskType.BLOCK_CONFIRMATION_TRACKING;
        } else if (taskSpec instanceof DepositTrackingTaskSpec) {
            return TaskType.DEPOSIT_TRACKING;
        } else if (taskSpec instanceof WithdrawalTrackingTaskSpec) {
            return TaskType.WITHDRAWAL_TRACKING;
        } else {
            throw new IllegalArgumentException("Task spec is of not supported type.");
        }
    }

    private TaskSpec getTaskSpec(TaskType taskType, String rawTaskSpec) {
        try {
            if (taskType.equals(TaskType.BLOCK_CONFIRMATION_TRACKING)) {
                return objectMapper.readValue(rawTaskSpec, BlockConfirmationTrackingTaskSpec.class);
            } else if (taskType.equals(TaskType.DEPOSIT_TRACKING)) {
                return objectMapper.readValue(rawTaskSpec, DepositTrackingTaskSpec.class);
            } else if (taskType.equals(TaskType.WITHDRAWAL_TRACKING)) {
                return objectMapper.readValue(rawTaskSpec, WithdrawalTrackingTaskSpec.class);
            } else {
                throw new IllegalArgumentException("Task type is of not supported type.");
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Raw task spec is invalid.", e);
        }
    }
}
