package com.petstore.petsservice.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;

    public CategoryServiceImpl(CategoryRepository repo) {
        this.repo = repo;
    }

    @Override
    public Mono<CategoryModel> create(CreateCategoryModel input) {
        return Mono.just(input)
                .map(Category::new)
                .flatMap(repo::save)
                .map(Category::toModel);
    }

    @Override
    public Mono<Page<CategoryModel>> getAll(Pageable pageable) {
        return repo.findBy(pageable)
                .map(Category::toModel)
                .collectList()
                .zipWith(this.repo.count())
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }
}
