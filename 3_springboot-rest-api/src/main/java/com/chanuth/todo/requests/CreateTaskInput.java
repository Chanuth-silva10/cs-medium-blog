package com.chanuth.todo.requests;

import com.chanuth.todo.entities.Task;
import com.chanuth.todo.entities.TaskStatusEnum;

import java.util.Date;

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
