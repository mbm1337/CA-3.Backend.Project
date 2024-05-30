package org.app.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {
    private Integer id;
    private String name;
    private String ingredients;
    private String instructions;
    private String category;
    private String imageUrl;
}
