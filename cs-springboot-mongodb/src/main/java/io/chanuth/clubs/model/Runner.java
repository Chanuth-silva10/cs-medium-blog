package io.chanuth.clubs.model;

import java.time.LocalDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "runners")
public class Runner extends BaseDocument {

    @Indexed
    private String name;

    private LocalDate birthDate;

    @Indexed
    private RunnerCategory category;

    private boolean active;

    protected Runner() {
        // For Spring Data, which fills the fields itself.
    }

    public Runner(String name, LocalDate birthDate, RunnerCategory category, boolean active) {
        this.name = name;
        this.birthDate = birthDate;
        this.category = category;
        this.active = active;
    }

    public void update(String name, LocalDate birthDate, RunnerCategory category, boolean active) {
        this.name = name;
        this.birthDate = birthDate;
        this.category = category;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public RunnerCategory getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }
}
