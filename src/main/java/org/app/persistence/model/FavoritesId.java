package org.app.persistence.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class FavoritesId implements Serializable {
    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "recipe_id")
    private int recipeId;
}
