package com.petstore.petsservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public class TestPage<T> extends PageImpl<T> {

    public TestPage(
            List<T> content,
            JsonNode pageable,
            Integer number,
            Integer size,
            Integer numberOfElements,
            Long totalElements,
            Long totalPages,
            JsonNode sort,
            Boolean first,
            Boolean last,
            Boolean empty
    ) {
        super(content, PageRequest.of(number, size), totalElements);
    }
}
