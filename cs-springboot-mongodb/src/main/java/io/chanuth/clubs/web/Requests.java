package io.chanuth.clubs.web;

import io.chanuth.clubs.model.Address;
import io.chanuth.clubs.model.RunnerCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** The bodies we accept. Documents are never read straight from the request. */
public final class Requests {

    private Requests() {
    }

    public record ClubRequest(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Size(min = 2, max = 6) String code,
            @NotNull @Valid Address address) {
    }

    public record RunnerRequest(
            @NotBlank @Size(max = 80) String name,
            @NotNull @Past LocalDate birthDate,
            @NotNull RunnerCategory category,
            @NotNull Boolean active) {
    }
}
