package com.petstore.petsservice.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(CategoryServiceImpl.class)
public class CategoryServiceImplTest {

    private static Pageable pageable = PageRequest.of(0, 20);

    @Autowired
    private CategoryService svc;
    @MockBean
    private CategoryRepository repo;

    @Test
    public void create_validInput_returnsNewlyCreatedCategory() {
        var id = 12345L;
        var input = new CreateCategoryModel("Dragons");
        when(repo.save(argThat(unsaved -> unsaved.getName().equals(input.getName())))).thenAnswer(invocation -> {
            var unsaved = invocation.getArgument(0, Category.class);
            var saved = Category.builder()
                    .id(id)
                    .name(unsaved.getName())
                    .build();
            return Mono.just(saved);
        });

        StepVerifier
                .create(svc.create(input))
                .assertNext(t -> {
                    assertThat(t.getId()).isEqualTo(id);
                    assertThat(t.getName()).isEqualTo(input.getName());
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void getAll_noResults_returnsEmptyPage() {
        when(repo.findBy(eq(pageable))).thenReturn(Flux.empty());
        when(repo.count()).thenReturn(Mono.just(0L));

        StepVerifier
                .create(svc.getAll(pageable))
                .assertNext(page -> {
                    assertThat(page.getNumber()).isEqualTo(pageable.getPageNumber());
                    assertThat(page.getSize()).isEqualTo(pageable.getPageSize());
                    assertThat(page.getTotalElements()).isEqualTo(0L);
                    assertThat(page.getPageable()).isEqualTo(pageable);
                    assertThat(page.getContent().size()).isEqualTo(0L);
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void getAll_matchingResults_returnsCategoryModelsPage() {
        var categories = IntStream.range(0, pageable.getPageSize())
                .mapToObj(i -> Category.builder().id((long) i).name(String.format("Unicorn %d", i)).build())
                .toList();
        var count = pageable.getPageSize() * 10;
        when(repo.findBy(eq(pageable))).thenReturn(Flux.fromIterable(categories));
        when(repo.count()).thenReturn(Mono.just((long) count));

        StepVerifier
                .create(svc.getAll(pageable))
                .assertNext(page -> {
                    assertThat(page.getNumber()).isEqualTo(pageable.getPageNumber());
                    assertThat(page.getSize()).isEqualTo(pageable.getPageSize());
                    assertThat(page.getTotalElements()).isEqualTo(count);
                    assertThat(page.getPageable()).isEqualTo(pageable);

                    assertThat(page.getContent().size()).isEqualTo(categories.size());
                    var models = page.getContent();
                    IntStream.range(0, categories.size())
                            .forEach(i -> {
                                assertThat(models.get(i).getId()).isEqualTo(categories.get(i).getId());
                                assertThat(models.get(i).getName()).isEqualTo(categories.get(i).getName());
                            });
                })
                .expectComplete()
                .verify();
    }
}
