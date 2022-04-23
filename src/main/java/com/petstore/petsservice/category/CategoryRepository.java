package com.petstore.petsservice.category;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;

public interface CategoryRepository extends ReactiveSortingRepository<Category, Long> {
    Flux<Category> findBy(Pageable pageable);
}
