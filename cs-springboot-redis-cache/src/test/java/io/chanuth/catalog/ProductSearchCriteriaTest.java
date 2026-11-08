package io.chanuth.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import io.chanuth.catalog.dtos.ProductSearchCriteria;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductSearchCriteriaTest {

    @Test
    void searchesThatMeanTheSameThingShareOneKey() {
        var a = new ProductSearchCriteria("  iPhone ", "Phones", null, new BigDecimal("1000"), true);
        var b = new ProductSearchCriteria("iphone", "phones", null, new BigDecimal("1000.00"), true);

        assertThat(a.cacheKey()).isEqualTo(b.cacheKey())
                .isEqualTo("name=iphone;category=phones;minPrice=*;maxPrice=1000;available=true");
    }

    @Test
    void missingFiltersAreExplicitAndDoNotCrash() {
        var none = new ProductSearchCriteria(null, "  ", null, null, null);

        assertThat(none.cacheKey()).isEqualTo("name=*;category=*;minPrice=*;maxPrice=*;available=*");
    }

    @Test
    void differentSearchesGetDifferentKeys() {
        var phones = new ProductSearchCriteria("iphone", "phones", null, null, null);
        var accessories = new ProductSearchCriteria("iphone", "accessories", null, null, null);

        assertThat(phones.cacheKey()).isNotEqualTo(accessories.cacheKey());
    }
}
