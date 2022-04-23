package com.petstore.petsservice.category;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public record CategoryHandler(CategoryService svc) {

    private Mono<Pageable> extractPageableOrDefault(ServerRequest request) {
        var page = Integer.parseInt(request.queryParam("page").orElse("0"));
        var size = Integer.parseInt(request.queryParam("size").orElse("20"));
        var pageable = PageRequest.of(page, size);
        return Mono.just(pageable);
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CreateCategoryModel.class)
                .flatMap(svc::create)
                .flatMap(cat -> {
                    var relativeUri = URI.create(String.format("/%d", cat.getId()));
                    return ServerResponse
                            .created(relativeUri)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(cat));
                });
    }

    public Mono<ServerResponse> getAll(ServerRequest request) {
        return extractPageableOrDefault(request)
                .flatMap(pageable -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                                BodyInserters.fromPublisher(
                                        svc.getAll(pageable),
                                        new ParameterizedTypeReference<Page<CategoryModel>>() {
                                        }
                                ))
                );
    }
}
