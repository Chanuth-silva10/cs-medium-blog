package io.chanuth.httpclient.web;

import io.chanuth.httpclient.client.TaskApiClient;
import io.chanuth.httpclient.model.CreateTaskRequest;
import io.chanuth.httpclient.model.TaskDto;
import io.chanuth.httpclient.model.UpdateTaskRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Our own small API. Every method hands the work to the Task API through {@link TaskApiClient}.
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskApiClient taskApi;

    public TaskController(TaskApiClient taskApi) {
        this.taskApi = taskApi;
    }

    @GetMapping
    public List<TaskDto> myTasks() {
        return taskApi.myTasks();
    }

    @GetMapping("/{id}")
    public TaskDto getTask(@PathVariable String id) {
        return taskApi.getTask(id);
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskApi.createTask(request));
    }

    @PatchMapping("/{id}")
    public TaskDto updateTask(@PathVariable String id, @Valid @RequestBody UpdateTaskRequest request) {
        return taskApi.updateTask(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskApi.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
