package io.chanuth.clubs.web;

import io.chanuth.clubs.model.Runner;
import io.chanuth.clubs.model.RunningClub;
import io.chanuth.clubs.repository.ClubRepository;
import io.chanuth.clubs.repository.RunnerRepository;
import io.chanuth.clubs.web.Requests.ClubRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/clubs")
public class ClubController {

    private final ClubRepository clubs;
    private final RunnerRepository runners;

    public ClubController(ClubRepository clubs, RunnerRepository runners) {
        this.clubs = clubs;
        this.runners = runners;
    }

    // ---------- Part 1: write operations ----------

    @PostMapping
    public ResponseEntity<RunningClub> create(@Valid @RequestBody ClubRequest request) {
        RunningClub saved = clubs.save(new RunningClub(request.name().trim(), request.code().trim().toUpperCase(),
                request.address()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public RunningClub update(@PathVariable String id, @Valid @RequestBody ClubRequest request) {
        RunningClub club = find(id);
        club.update(request.name().trim(), request.code().trim().toUpperCase(), request.address());
        return clubs.save(club);
    }

    /** Deleting a club leaves its runners alone: they only had a reference from the club. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        clubs.delete(find(id));
        return ResponseEntity.noContent().build();
    }

    /** Adds runners to the club. Runners already in the club are kept and not added twice. */
    @PostMapping("/{id}/members")
    public RunningClub addMembers(@PathVariable String id, @RequestBody Set<String> runnerIds) {
        RunningClub club = find(id);
        runnerIds.forEach((runnerId) -> NotFoundException.ensureObjectId("Runner", runnerId));
        List<Runner> found = runners.findAllById(new LinkedHashSet<>(runnerIds));
        if (found.size() != runnerIds.size()) {
            Set<String> foundIds = new LinkedHashSet<>();
            found.forEach((runner) -> foundIds.add(runner.getId()));
            String missing = runnerIds.stream().filter((runnerId) -> !foundIds.contains(runnerId)).findFirst().orElse("?");
            throw new NotFoundException("Runner", missing);
        }
        club.addMembers(found);
        return clubs.save(club);
    }

    // ---------- Part 2: read operations ----------

    /** Optional filters. With no filter you get every club, by name descending. */
    @GetMapping
    public List<RunningClub> list(@RequestParam(required = false) String city,
                                  @RequestParam(required = false) String search) {
        if (city != null && !city.isBlank()) {
            return clubs.findByAddressCityIgnoreCase(city.trim());
        }
        if (search != null && !search.isBlank()) {
            return clubs.findByNameContainingIgnoreCaseOrderByNameDesc(search.trim());
        }
        return clubs.findAll(Sort.by(Sort.Direction.DESC, "name"));
    }

    @GetMapping("/{id}")
    public RunningClub one(@PathVariable String id) {
        return find(id);
    }

    /** The clubs a runner belongs to. */
    @GetMapping("/of-runner/{runnerId}")
    public List<RunningClub> ofRunner(@PathVariable String runnerId) {
        NotFoundException.ensureObjectId("Runner", runnerId);
        Runner runner = runners.findById(runnerId).orElseThrow(() -> new NotFoundException("Runner", runnerId));
        return clubs.findByMembersContaining(runner);
    }

    private RunningClub find(String id) {
        NotFoundException.ensureObjectId("Club", id);
        return clubs.findById(id).orElseThrow(() -> new NotFoundException("Club", id));
    }
}
