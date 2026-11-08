package io.chanuth.httpclient.web;

import io.chanuth.httpclient.client.TaskApiClient;
import io.chanuth.httpclient.model.CategoryDto;
import io.chanuth.httpclient.model.CreateCategoryRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final TaskApiClient taskApi;

    public CategoryController(TaskApiClient taskApi) {
        this.taskApi = taskApi;
    }

    @GetMapping
    public List<CategoryDto> categories() {
        return taskApi.categories();
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskApi.createCategory(request));
    }
}
