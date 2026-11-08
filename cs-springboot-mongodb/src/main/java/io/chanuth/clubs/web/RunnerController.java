package io.chanuth.clubs.web;

import io.chanuth.clubs.model.Runner;
import io.chanuth.clubs.model.RunnerCategory;
import io.chanuth.clubs.model.RunningClub;
import io.chanuth.clubs.repository.RunnerRepository;
import io.chanuth.clubs.web.Requests.RunnerRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/runners")
public class RunnerController {

    private final RunnerRepository runners;
    private final MongoTemplate mongoTemplate;

    public RunnerController(RunnerRepository runners, MongoTemplate mongoTemplate) {
        this.runners = runners;
        this.mongoTemplate = mongoTemplate;
    }

    // ---------- Part 1: write operations ----------

    @PostMapping
    public ResponseEntity<Runner> create(@Valid @RequestBody RunnerRequest request) {
        Runner saved = runners.save(toRunner(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    /** Many runners in one request: one insert for the whole list. */
    @PostMapping("/bulk")
    public ResponseEntity<List<Runner>> createMany(@RequestBody List<@Valid RunnerRequest> requests) {
        List<Runner> saved = runners.saveAll(requests.stream().map(RunnerController::toRunner).toList());
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public Runner update(@PathVariable String id, @Valid @RequestBody RunnerRequest request) {
        Runner runner = find(id);
        runner.update(request.name(), request.birthDate(), request.category(), request.active());
        return runners.save(runner);
    }

    /**
     * A club keeps only the id of its runners. If we deleted the runner and did nothing else, the
     * clubs would keep an id that points to nothing. So we remove the id from every club first.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Runner runner = find(id);
        mongoTemplate.updateMulti(new Query(), new Update().pull("members", runner), RunningClub.class);
        runners.delete(runner);
        return ResponseEntity.noContent().build();
    }

    // ---------- Part 2: read operations ----------

    /** Optional filters. With no filter you get every runner, sorted by name. */
    @GetMapping
    public List<Runner> list(@RequestParam(required = false) RunnerCategory category,
                             @RequestParam(required = false) Boolean active,
                             @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return runners.findByNameContainingIgnoreCase(search.trim());
        }
        if (category != null && active != null) {
            return runners.findByCategoryAndActive(category, active);
        }
        return runners.findAll(Sort.by("name"));
    }

    @GetMapping("/{id}")
    public Runner one(@PathVariable String id) {
        return find(id);
    }

    @GetMapping("/by-categories")
    public List<Runner> byCategories(@RequestParam Set<RunnerCategory> categories) {
        return runners.findByCategoryIn(categories);
    }

    @GetMapping("/born-between")
    public List<Runner> bornBetween(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return runners.findByBirthDateBetweenOrderByBirthDate(from, to);
    }

    @GetMapping("/youngest")
    public Runner youngest() {
        return runners.findFirstByOrderByBirthDateDesc().orElseThrow(() -> new NotFoundException("Runner", "(none)"));
    }

    @GetMapping("/oldest")
    public List<Runner> oldest() {
        return runners.findFirst5ByOrderByBirthDate();
    }

    /** Pages start at 0: /runners/page?page=0&size=10&sort=name,asc */
    @GetMapping("/page")
    public PageResponse<Runner> page(@PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<Runner> page = runners.findAll(pageable);
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages());
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }

    private Runner find(String id) {
        NotFoundException.ensureObjectId("Runner", id);
        return runners.findById(id).orElseThrow(() -> new NotFoundException("Runner", id));
    }

    private static Runner toRunner(RunnerRequest request) {
        return new Runner(request.name(), request.birthDate(), request.category(), request.active());
    }
}
