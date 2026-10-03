package com.kasane.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Validated PageRequest construction for the public list endpoints. Without this, a
 * negative page made PageRequest.of throw IllegalArgumentException (surfacing as a 500),
 * and size was unbounded - harmless on today's tiny dataset, but an easy way to make
 * every request load the whole table.
 */
final class Paging {

    static final int MAX_SIZE = 100;

    private Paging() {}

    static PageRequest of(int page, int size, Sort sort) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 0");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "size must be between 1 and " + MAX_SIZE);
        }
        return PageRequest.of(page, size, sort);
    }
}
