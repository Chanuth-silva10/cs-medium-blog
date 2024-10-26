package com.chanuth.todoapp.requests;

import com.chanuth.todoapp.entities.TaskStatusEnum;
import com.chanuth.todoapp.entities.Task;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(title = "CreateTaskInput", description = "Parameters required to create a task", required = true)
public record CreateTaskInput(String name, String description, TaskStatusEnum status, Date dueDate) {
    public Task toTask() {
        Task task = new Task();

        task.setName(name)
            .setDescription(description)
            .setStatus(status)
            .setDueDate(dueDate);

        return task;
    }
}
