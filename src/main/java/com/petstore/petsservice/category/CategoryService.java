package com.petstore.petsservice.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface CategoryService {
    Mono<CategoryModel> create(CreateCategoryModel input);
    Mono<Page<CategoryModel>> getAll(Pageable pageable);
}
