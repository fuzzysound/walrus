package com.fuzzysound.walrus.task.infrastructure.repository;

import com.fuzzysound.walrus.task.infrastructure.entity.TaskEntity;
import com.fuzzysound.walrus.task.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findAllByStatus(TaskStatus status);
    @Modifying(clearAutomatically = true)
    @Query("update TaskEntity set status = :status where id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") TaskStatus status);

    @Modifying
    @Query("update TaskEntity set age = age + 1 where id = :id")
    void increaseAge(@Param("id") Long id);
}
