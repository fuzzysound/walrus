package com.fuzzysound.walrus;

import com.fuzzysound.walrus.service.TaskConsumerService;
import com.fuzzysound.walrus.task.model.DatabaseTask;
import com.fuzzysound.walrus.task.service.DatabaseTaskService;
import com.fuzzysound.walrus.exception.UnretryableTaskException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseTaskWorker {
    private final DatabaseTaskService databaseTaskService;
    private final TaskConsumerService taskConsumerService;

    @Scheduled(fixedDelayString = "${walrus.task-worker.delay}")
    public void run() {
        log.info("DatabaseTaskWorker running...");
        List<DatabaseTask> databaseTasks = databaseTaskService.getAllReadyTasks();
        if (databaseTasks.isEmpty()) {
            log.info("There is no ready task, skipping.");
            return;
        }
        databaseTasks.forEach(databaseTask -> taskConsumerService.consumeAsync(databaseTask.getTask())
                .handle((res, ex) -> {
                    if (ex == null) {
                        log.info("Task {} successfully completed.", databaseTask.getTaskId());
                        databaseTaskService.deleteTask(databaseTask);
                    } else if (ex instanceof UnretryableTaskException) {
                        log.error("Task {} failed with unretryable exception.", databaseTask.getTaskId(), ex);
                        databaseTaskService.setTaskUnretryable(databaseTask);
                    } else {
                        log.error("Task {} failed, though retryable, age {}.",
                                databaseTask.getTaskId(), databaseTask.getAge(), ex);
                        databaseTaskService.increaseTaskAge(databaseTask);
                    }
                    return res;
                }));
    }
}
