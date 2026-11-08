package io.chanuth.catalog.configs;

/** One place for the cache names, so a typo cannot create a second, silently empty cache. */
public final class CacheNames {

    public static final String ALL_PRODUCTS = "all-products";
    public static final String PRODUCT_SEARCH = "product-search";
    public static final String CATEGORIES = "categories";

    private CacheNames() {
    }
}
