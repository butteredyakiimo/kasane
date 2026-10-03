package com.kasane.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagingTest {

    @Test
    void of_acceptsBoundaryValues() {
        PageRequest first = Paging.of(0, 1, Sort.by("id"));
        PageRequest max = Paging.of(5, Paging.MAX_SIZE, Sort.by("id"));

        assertThat(first.getPageNumber()).isZero();
        assertThat(first.getPageSize()).isEqualTo(1);
        assertThat(max.getPageNumber()).isEqualTo(5);
        assertThat(max.getPageSize()).isEqualTo(Paging.MAX_SIZE);
    }

    @Test
    void of_rejectsNegativePage_asBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> Paging.of(-1, 24, Sort.by("id")));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void of_rejectsSizeOutsideRange_asBadRequest() {
        for (int size : new int[] {0, -5, Paging.MAX_SIZE + 1, Integer.MAX_VALUE}) {
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> Paging.of(0, size, Sort.by("id")));
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
