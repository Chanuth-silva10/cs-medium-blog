package io.chanuth.bookshelf.dtos;

/** The average is null when a book has no reviews yet. */
public record RatingSummary(Double average, Long count) {
}
