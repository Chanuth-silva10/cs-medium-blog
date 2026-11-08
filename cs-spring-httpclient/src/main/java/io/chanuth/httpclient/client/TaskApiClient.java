package io.chanuth.httpclient.client;

import io.chanuth.httpclient.model.CategoryDto;
import io.chanuth.httpclient.model.CreateCategoryRequest;
import io.chanuth.httpclient.model.CreateTaskRequest;
import io.chanuth.httpclient.model.TaskDto;
import io.chanuth.httpclient.model.UpdateTaskRequest;
import java.util.List;

/**
 * Everything we need from the DevCraft Task API. There are two implementations, one built on
 * RestTemplate and one on RestClient, so you can compare them side by side.
 */
public interface TaskApiClient {

    /** The tasks created by the service account. */
    List<TaskDto> myTasks();

    TaskDto getTask(String id);

    TaskDto createTask(CreateTaskRequest request);

    TaskDto updateTask(String id, UpdateTaskRequest request);

    void deleteTask(String id);

    List<CategoryDto> categories();

    CategoryDto createCategory(CreateCategoryRequest request);
}
