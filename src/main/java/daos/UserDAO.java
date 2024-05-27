package daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import persistence.model.Role;
import persistence.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UserDAO implements ISecurityDAO {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String timestamp = dateFormat.format(new Date());

    private static UserDAO instance;
    private static EntityManagerFactory emf;

    public static UserDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new UserDAO();
        }
        return instance;
    }

    @Override
    public User getVerifiedUser(String username, String password) throws EntityNotFoundException {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
            query.setParameter("id", username);

            User user = em.find(User.class, username);

            if (user == null) {
                throw new EntityNotFoundException("No user found with username: " + username);
            }

            if (!user.verifyPassword(password)) {
                throw new EntityNotFoundException("Wrong password");
            }
            return user;
        }
    }


    @Override
    public User createUser(String email, String password) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            User existingUser = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (existingUser != null) {
                return null;
            }

            User user = new User(email, password);
            Role userRole = em.find(Role.class, "user");
            if (userRole == null) {
                userRole = new Role("user");
                em.persist(userRole);
            }
            user.addRole(userRole);
            em.persist(user);
            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public Role createRole(String role) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Role createdRole = new Role();
            createdRole.setName(role);
            em.persist(createdRole);
            em.getTransaction().commit();
            return createdRole;
        }
    }

    @Override
    public User addUserRole(String username, String role) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            User foundUser = em.find(User.class, username);
            Role foundRole = em.find(Role.class, role);

            // Check if either the user or the role was not found
            if (foundUser == null || foundRole == null) {
                em.getTransaction().rollback();
                return null;
            }

            foundUser.addRole(foundRole);
            em.merge(foundUser);
            em.getTransaction().commit();
            return foundUser;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User deleteUser(String username) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            User foundUser = em.find(User.class, username);
            em.remove(foundUser);
            em.getTransaction().commit();
            return foundUser;
        }catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("No user found with username: " + username);
        }
    }

    public List<User> getAllUsers() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
            return query.getResultList();
        }
    }

    public List<Role> getAllRoles() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Role> query = em.createQuery("SELECT r FROM Role r", Role.class);
            return query.getResultList();
        }
    }

    public User verifyUser(String email, String password) {
        try (EntityManager em = emf.createEntityManager()) {
            User user = em.find(User.class, email);
            if (user == null) {
                return null;
            }
            if (!user.verifyPassword(password)) {
                return null;
            }
            return user;
        }
    }


    public User getByString(String email) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        }
    }
}

