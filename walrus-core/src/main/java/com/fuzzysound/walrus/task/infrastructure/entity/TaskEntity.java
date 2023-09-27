package com.fuzzysound.walrus.task.infrastructure.entity;

import com.fuzzysound.walrus.task.model.TaskStatus;
import com.fuzzysound.walrus.task.model.TaskType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "task", indexes = {
        @Index(name = "idx__status", columnList = "status")
})
@Getter
@Setter
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TaskType taskType;

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TaskStatus status;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "task_spec", length = 65535)
    private String taskSpec;
}
