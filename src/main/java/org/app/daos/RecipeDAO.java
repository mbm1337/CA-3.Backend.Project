package org.app.daos;

import org.app.persistence.model.Favorites;
import org.app.persistence.model.FavoritesId;
import org.app.persistence.model.Recipe;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.app.persistence.model.User;

import java.util.List;

public class RecipeDAO extends AbstractDAO<Recipe> {

    private static RecipeDAO instance;
    private static EntityManagerFactory emf;

    private RecipeDAO(EntityManagerFactory _emf, Class<Recipe> entityClass) {
        super(_emf, entityClass);
    }

    public static RecipeDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new RecipeDAO(emf, Recipe.class);
        }
        return instance;
    }

    public List<Recipe> getAllByEmail(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Recipe> query = em.createQuery("SELECT h FROM Recipe h WHERE h.user.email =:email", Recipe.class);
            query.setParameter("email", email);
            return query.getResultList();
        }
    }

    public List<Recipe> getAll() {
        try (EntityManager em = emf.createEntityManager()){
            TypedQuery<Recipe> query = em.createQuery("SELECT h FROM Recipe h", Recipe.class);
            return query.getResultList();
        }
    }

    @Override
    public int delete(int id) {
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Recipe recipeFound = em.find(Recipe.class, id);

            if (recipeFound != null) {
                User user = recipeFound.getUser();
                if (user != null) {
                    user.getRecipes().remove(recipeFound);
                }
                recipeFound.setUser(null);
                em.remove(recipeFound);
                em.getTransaction().commit();
                return 1;
            }
            return 0;
        }
    }


    public void addFavorite(String userEmail, int recipeId) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            User user = em.find(User.class, userEmail);
            Recipe recipe = em.find(Recipe.class, recipeId);
            Favorites favorite = new Favorites(new FavoritesId(userEmail, recipeId), user, recipe);
            em.persist(favorite);
            em.getTransaction().commit();
        }
    }

    public void removeFavorite(String userEmail, int recipeId) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Favorites favorite = em.find(Favorites.class, new FavoritesId(userEmail, recipeId));
            if (favorite != null) {
                em.remove(favorite);
            }
            em.getTransaction().commit();
        }
    }
}
