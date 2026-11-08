package io.chanuth.catalog.controllers;

import io.chanuth.catalog.dtos.CategoryListResponse;
import io.chanuth.catalog.dtos.CreateProductRequest;
import io.chanuth.catalog.dtos.ProductListResponse;
import io.chanuth.catalog.dtos.ProductResponse;
import io.chanuth.catalog.dtos.ProductSearchCriteria;
import io.chanuth.catalog.dtos.UpdateProductRequest;
import io.chanuth.catalog.services.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The controller knows nothing about caching: the annotations on the service do all of it.
 */
@RestController
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public CategoryListResponse categories() {
        return catalogService.categories();
    }

    @GetMapping("/products")
    public ProductListResponse allProducts() {
        return catalogService.allProducts();
    }

    /** Example: /products/search?name=iphone&category=phones&maxPrice=1000&available=true */
    @GetMapping("/products/search")
    public ProductListResponse search(@Valid ProductSearchCriteria criteria) {
        return catalogService.search(criteria);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.create(request));
    }

    @PatchMapping("/products/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return catalogService.update(id, request);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catalogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
