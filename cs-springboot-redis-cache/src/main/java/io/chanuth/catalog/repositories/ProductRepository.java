package io.chanuth.catalog.repositories;

import io.chanuth.catalog.entities.Product;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {

    // "join fetch" loads each product's category in the same query, instead of one extra query per product.
    @Query("select p from Product p join fetch p.category order by p.name")
    List<Product> findAllWithCategory();

    /** Every filter is optional: a null parameter switches that condition off. */
    @Query("""
            select p from Product p join fetch p.category c
            where (:name is null or lower(p.name) like concat('%', :name, '%'))
              and (:category is null or lower(c.name) = :category)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
              and (:available is null or p.available = :available)
            order by p.name
            """)
    List<Product> search(
            @Param("name") String name,
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("available") Boolean available);
}
