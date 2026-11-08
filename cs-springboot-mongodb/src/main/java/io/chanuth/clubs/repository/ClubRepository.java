package io.chanuth.clubs.repository;

import io.chanuth.clubs.model.Runner;
import io.chanuth.clubs.model.RunningClub;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClubRepository extends MongoRepository<RunningClub, String> {

    List<RunningClub> findByNameContainingIgnoreCaseOrderByNameDesc(String keyword);

    /** Searches inside the nested address object: address.city. */
    List<RunningClub> findByAddressCityIgnoreCase(String city);

    /** Searches the references: clubs that have this runner as a member. */
    List<RunningClub> findByMembersContaining(Runner runner);
}
