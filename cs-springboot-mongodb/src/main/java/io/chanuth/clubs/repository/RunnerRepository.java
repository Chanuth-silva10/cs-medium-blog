package io.chanuth.clubs.repository;

import io.chanuth.clubs.model.Runner;
import io.chanuth.clubs.model.RunnerCategory;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Part 1 only needed the methods inherited from MongoRepository. Part 2 adds query methods:
 * Spring Data reads each method name and builds the MongoDB query from it.
 */
public interface RunnerRepository extends MongoRepository<Runner, String> {

    List<Runner> findByCategoryAndActive(RunnerCategory category, boolean active);

    List<Runner> findByCategoryIn(Collection<RunnerCategory> categories);

    List<Runner> findByNameContainingIgnoreCase(String keyword);

    List<Runner> findByBirthDateBetweenOrderByBirthDate(LocalDate from, LocalDate to);

    Optional<Runner> findFirstByOrderByBirthDateDesc();

    List<Runner> findFirst5ByOrderByBirthDate();

    long countByCategory(RunnerCategory category);
}
