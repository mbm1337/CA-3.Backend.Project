package daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import persistence.model.Comment;

import java.util.List;

public class CommentDAO extends AbstractDAO<Comment> {

    private static CommentDAO instance;
    private static EntityManagerFactory emf;

    private CommentDAO(EntityManagerFactory _emf, Class<Comment> entityClass) {
        super(_emf, entityClass);
    }

    public static CommentDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new CommentDAO(emf, Comment.class);
        }
        return instance;
    }

    public List<Comment> getAllByRecipeId(Integer recipeId) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Comment> query = em.createQuery("SELECT c FROM Comment c WHERE c.recipe.id =:recipeId", Comment.class);
            query.setParameter("recipeId", recipeId);
            return query.getResultList();
        }
    }

    public List<Comment> getAll() {
        try (EntityManager em = emf.createEntityManager()){
            TypedQuery<Comment> query = em.createQuery("SELECT c FROM Comment c", Comment.class);
            return query.getResultList();
        }
    }

    @Override
    public int delete(int id) {
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Comment commentFound = em.find(Comment.class, id);

            if (commentFound != null) {
                em.remove(commentFound);
                em.getTransaction().commit();
                return 1;
            }
            return 0;
        }
    }
}
