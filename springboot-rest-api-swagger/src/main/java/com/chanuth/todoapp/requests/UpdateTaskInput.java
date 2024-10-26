package com.chanuth.todoapp.requests;

import com.chanuth.todoapp.entities.TaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(title = "UpdateTaskInput", description = "Parameters required to update a task", required = true)
public record UpdateTaskInput(TaskStatusEnum status, Date dueDate) {

}
