package com.petstore.petsservice.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    private Long id;
    private String name;

    // TODO TEST ME
    public Category(CreateCategoryModel createCategoryModel) {
        this.name = createCategoryModel.getName();
    }

    // TODO TEST ME
    public CategoryModel toModel() {
        return CategoryModel.builder()
                .id(this.id)
                .name(this.name)
                .build();
    }
}
