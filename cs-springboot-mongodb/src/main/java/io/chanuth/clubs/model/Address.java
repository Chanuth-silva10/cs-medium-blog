package io.chanuth.clubs.model;

import jakarta.validation.constraints.NotBlank;

/** Not a document of its own: it is stored inside the club document as a nested object. */
public record Address(@NotBlank String city, @NotBlank String postalCode, @NotBlank String street) {
}
