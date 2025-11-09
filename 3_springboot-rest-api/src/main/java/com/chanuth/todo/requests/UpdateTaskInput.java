package com.chanuth.todo.requests;

import com.chanuth.todo.entities.TaskStatusEnum;

import java.util.Date;

public record UpdateTaskInput(TaskStatusEnum status, Date dueDate) {

}
