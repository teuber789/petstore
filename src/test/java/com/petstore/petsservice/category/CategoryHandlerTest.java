package com.petstore.petsservice.category;

import com.petstore.petsservice.util.TestPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(CategoryRouter.class)
@Import(CategoryHandler.class)
public class CategoryHandlerTest {

    private static final String CATEGORIES_ROUTE = "/categories";

    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private CategoryService svc;

    private final Answer<Mono<Page<CategoryModel>>> emptyPageAnswer = invocation -> {
        var pageable = invocation.getArgument(0, Pageable.class);
        return Mono.just(new PageImpl<>(new ArrayList<CategoryModel>(), pageable, 0));
    };

    private String locationHeader(long id) {
        return String.format("/%d", id);
    }

    @Test
    public void create_validRequestBody_returnsNewlyCreatedCategory() {
        var id = 12345L;
        var reqBody = new CreateCategoryModel("Dragons");
        when(svc.create(argThat(input -> input.getName().equals(reqBody.getName())))).thenAnswer(invocation -> {
            var input = invocation.getArgument(0, CreateCategoryModel.class);
            var category = CategoryModel.builder()
                    .id(id)
                    .name(input.getName())
                    .build();
            return Mono.just(category);
        });

        webTestClient
                .post()
                .uri(CATEGORIES_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(reqBody))
                .exchange()
                .expectHeader()
                .location(locationHeader(id))
                .expectBody(CategoryModel.class)
                .value(category -> {
                    assertThat(category.getId()).isEqualTo(id);
                    assertThat(category.getName()).isEqualTo(reqBody.getName());
                });
    }

    @Test
    public void getAll_noPagingParamsGiven_defaultsToPage0AndSize20() {
        when(svc.getAll(any())).thenAnswer(emptyPageAnswer);

        webTestClient
                .get()
                .uri(CATEGORIES_ROUTE)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<TestPage<CategoryModel>>() {})
                .value(page -> {
                    assertThat(page.getNumber()).isEqualTo(0);
                    assertThat(page.getSize()).isEqualTo(20);
                });
    }

    @Test
    public void getAll_pagingParamsGiven_usesGivenPagingParams() {
        when(svc.getAll(any())).thenAnswer(emptyPageAnswer);

        int expectedPage = 123456;
        int expectedSize = 54321;
        var uri = String.format("%s?page=%d&size=%d", CATEGORIES_ROUTE, expectedPage, expectedSize);

        webTestClient
                .get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<TestPage<CategoryModel>>() {})
                .value(page -> {
                    assertThat(page.getNumber()).isEqualTo(expectedPage);
                    assertThat(page.getSize()).isEqualTo(expectedSize);
                });
    }

    @Test
    public void getAll_requestAValidCategoryPage_returnsExpectedResults() {
        var content = List.of(
                CategoryModel.builder().id(1L).name("Dogs").build(),
                CategoryModel.builder().id(2L).name("Cats").build()
        );
        when(svc.getAll(any())).thenAnswer(invocation -> {
            var pageable = invocation.getArgument(0, Pageable.class);
            return Mono.just(new PageImpl<>(content, pageable, content.size()));
        });

        webTestClient
                .get()
                .uri(CATEGORIES_ROUTE)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<TestPage<CategoryModel>>() {})
                .value(page -> assertThat(page.getContent())
                        .usingRecursiveComparison()
                        .isEqualTo(content));
    }

}
