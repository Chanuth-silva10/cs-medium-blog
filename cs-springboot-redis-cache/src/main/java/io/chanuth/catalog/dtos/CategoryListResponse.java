package io.chanuth.catalog.dtos;

import io.chanuth.catalog.entities.Category;
import java.util.List;

public record CategoryListResponse(List<CategoryItem> items) {

    public record CategoryItem(Long id, String name, String description) {

        public static CategoryItem from(Category category) {
            return new CategoryItem(category.getId(), category.getName(), category.getDescription());
        }
    }
}
