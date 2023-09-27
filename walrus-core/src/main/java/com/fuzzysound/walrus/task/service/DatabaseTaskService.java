package com.fuzzysound.walrus.task.service;

import com.fuzzysound.walrus.task.infrastructure.converter.TaskEntityConverter;
import com.fuzzysound.walrus.task.infrastructure.entity.TaskEntity;
import com.fuzzysound.walrus.task.infrastructure.repository.TaskRepository;
import com.fuzzysound.walrus.task.model.DatabaseTask;
import com.fuzzysound.walrus.task.model.Task;
import com.fuzzysound.walrus.task.model.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DatabaseTaskService implements TaskService {
    private final TaskEntityConverter taskEntityConverter;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public void publishTask(Task task) {
        TaskEntity taskEntity = taskEntityConverter.toEntity(task);
        taskRepository.save(taskEntity);
    }

    public List<DatabaseTask> getAllReadyTasks() {
        return taskRepository.findAllByStatus(TaskStatus.READY).stream()
                .map(taskEntityConverter::fromEntity).toList();
    }

    @Transactional
    public void deleteTask(DatabaseTask databaseTask) {
        Optional<TaskEntity> taskEntityOptional = taskRepository.findById(databaseTask.getTaskId());
        taskEntityOptional.ifPresent(taskRepository::delete);
    }

    @Transactional
    public void setTaskUnretryable(DatabaseTask databaseTask) {
        taskRepository.updateStatus(databaseTask.getTaskId(), TaskStatus.UNRETRYABLE);
    }

    @Transactional
    public void increaseTaskAge(DatabaseTask databaseTask) {
        taskRepository.increaseAge(databaseTask.getTaskId());
    }
}
