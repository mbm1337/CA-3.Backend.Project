package org.app.persistence.model;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Favorites {
    @EmbeddedId
    private FavoritesId id;

    @MapsId("userEmail")
    @JoinColumn(name = "user_email")
    @ManyToOne
    private User user;

    @MapsId("recipeId")
    @JoinColumn(name = "recipe_id")
    @ManyToOne
    private Recipe recipe;

}
