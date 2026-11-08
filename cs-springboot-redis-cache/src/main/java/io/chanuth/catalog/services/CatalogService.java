package io.chanuth.catalog.services;

import io.chanuth.catalog.configs.CacheNames;
import io.chanuth.catalog.dtos.CategoryListResponse;
import io.chanuth.catalog.dtos.CategoryListResponse.CategoryItem;
import io.chanuth.catalog.dtos.CreateProductRequest;
import io.chanuth.catalog.dtos.ProductListResponse;
import io.chanuth.catalog.dtos.ProductResponse;
import io.chanuth.catalog.dtos.ProductSearchCriteria;
import io.chanuth.catalog.dtos.UpdateProductRequest;
import io.chanuth.catalog.entities.Category;
import io.chanuth.catalog.entities.Product;
import io.chanuth.catalog.exceptions.NotFoundException;
import io.chanuth.catalog.repositories.CategoryRepository;
import io.chanuth.catalog.repositories.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SlowDatabase slowDatabase;

    public CatalogService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          SlowDatabase slowDatabase) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.slowDatabase = slowDatabase;
    }

    // ---------- reads: served from the cache when possible ----------

    /**
     * The first call runs the method and stores the answer in Redis; later calls skip the method entirely.
     * sync = true means that if many requests miss at the same moment, only one of them queries the
     * database and the others wait for its answer, instead of all hitting the database at once.
     */
    @Cacheable(cacheNames = CacheNames.ALL_PRODUCTS, key = "'all'", sync = true)
    @Transactional(readOnly = true)
    public ProductListResponse allProducts() {
        slowDatabase.read("all products");
        return ProductListResponse.of(productRepository.findAllWithCategory().stream().map(ProductResponse::from).toList());
    }

    /**
     * One cache entry per distinct search, keyed by the normalized criteria. No sync here on purpose: the
     * Redis lock covers the whole cache, so with sync two DIFFERENT searches would wait for each other.
     * A rare duplicate query for the same search is the cheaper price for a cache with many keys.
     */
    @Cacheable(cacheNames = CacheNames.PRODUCT_SEARCH, key = "#criteria.cacheKey()")
    @Transactional(readOnly = true)
    public ProductListResponse search(ProductSearchCriteria criteria) {
        slowDatabase.read("search " + criteria.cacheKey());
        return ProductListResponse.of(productRepository.search(
                        criteria.normalizedName(), criteria.normalizedCategory(),
                        criteria.minPrice(), criteria.maxPrice(), criteria.available())
                .stream().map(ProductResponse::from).toList());
    }

    @Cacheable(cacheNames = CacheNames.CATEGORIES, key = "'all'", sync = true)
    @Transactional(readOnly = true)
    public CategoryListResponse categories() {
        slowDatabase.read("categories");
        return new CategoryListResponse(categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryItem::from).toList());
    }

    // ---------- writes: every change empties the product caches ----------

    /**
     * Any change can alter the full list and an unknown number of search results, so we empty both
     * product caches completely. Trying to guess which search keys a change affects is how stale data
     * slips through; the next reads simply rebuild what is needed.
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.ALL_PRODUCTS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true)
    })
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Category category = categoryRepository.findByNameIgnoreCase(request.category().trim())
                .orElseThrow(() -> new NotFoundException("No category named " + request.category()));
        Product product = new Product()
                .setName(request.name().trim())
                .setDescription(request.description())
                .setPrice(request.price())
                .setAvailable(request.available() == null || request.available())
                .setCategory(category);
        return ProductResponse.from(productRepository.save(product));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.ALL_PRODUCTS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true)
    })
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No product with id " + id));
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.available() != null) {
            product.setAvailable(request.available());
        }
        return ProductResponse.from(product);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.ALL_PRODUCTS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true)
    })
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("No product with id " + id);
        }
        productRepository.deleteById(id);
    }
}
