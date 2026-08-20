package com.macedxs.mx.task.service;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.service.AuthService;
import com.macedxs.mx.task.entity.TaskEntity;
import com.macedxs.mx.task.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskService taskService;

    @Test
    void shouldCreateAndUpdateTaskStatus() {
        UserEntity user = authService.register("Task User", "task.user@example.com", "Strong123!");

        TaskEntity task = taskService.createTask(user, "Implement MX task flow", "Create the first task pipeline");

        assertThat(task.getId()).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);

        TaskEntity updated = taskService.updateStatus(task.getId(), TaskStatus.RUNNING);
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.RUNNING);
    }
}
