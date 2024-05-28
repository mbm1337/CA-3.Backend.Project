package persistence;

import jakarta.persistence.EntityManagerFactory;
import lombok.NoArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import persistence.model.*;

import java.util.Properties;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class HibernateConfig {

    private static EntityManagerFactory entityManagerFactory;

    private static EntityManagerFactory getEntityManagerFactoryConfigIsDeployed() {
        try {
            Configuration configuration = new Configuration();
            Properties props = new Properties();

            String connectionUrl = System.getenv("CONNECTION_STR") + System.getenv("DB_NAME");
            String username = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");

            // Logging and null checks
            if (connectionUrl == null) {
                throw new NullPointerException("Environment variable CONNECTION_STR or DB_NAME is not set.");
            }
            if (username == null) {
                throw new NullPointerException("Environment variable DB_USERNAME is not set.");
            }
            if (password == null) {
                throw new NullPointerException("Environment variable DB_PASSWORD is not set.");
            }

            props.put("hibernate.connection.url", connectionUrl);
            props.put("hibernate.connection.username", username);
            props.put("hibernate.connection.password", password);
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            props.put("hibernate.archive.autodetection", "class");
            props.put("hibernate.current_session_context_class", "thread");
            props.put("hibernate.hbm2ddl.auto", "update");

            return getEntityManagerFactory(configuration, props);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static EntityManagerFactory buildEntityFactoryConfig() {
        try {
            Configuration configuration = new Configuration();
            Properties props = new Properties();

            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");

            // Logging and null checks
            if (dbUrl == null) {
                throw new NullPointerException("Environment variable DB_URL is not set.");
            }
            if (dbUser == null) {
                throw new NullPointerException("Environment variable DB_USER is not set.");
            }
            if (dbPassword == null) {
                throw new NullPointerException("Environment variable DB_PASSWORD is not set.");
            }

            props.put("hibernate.connection.url", dbUrl);
            props.put("hibernate.connection.username", dbUser);
            props.put("hibernate.connection.password", dbPassword);
            props.put("hibernate.show_sql", "true");
            props.put("hibernate.format_sql", "true");
            props.put("hibernate.use_sql_comments", "true");
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            props.put("hibernate.archive.autodetection", "class");
            props.put("hibernate.current_session_context_class", "thread");
            props.put("hibernate.hbm2ddl.auto", "update");

            return getEntityManagerFactory(configuration, props);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static EntityManagerFactory setupHibernateConfigurationForTesting() {
        try {
            Configuration configuration = new Configuration();
            Properties props = new Properties();
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.connection.driver_class", "org.testcontainers.jdbc.ContainerDatabaseDriver");
            props.put("hibernate.connection.url", "jdbc:tc:postgresql:15.3-alpine3.18:///test-db");
            props.put("hibernate.connection.username", "postgres");
            props.put("hibernate.connection.password", "postgres");
            props.put("hibernate.archive.autodetection", "class");
            props.put("hibernate.show_sql", "true");
            props.put("hibernate.hbm2ddl.auto", "create-drop");
            return getEntityManagerFactory(configuration, props);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static EntityManagerFactory getEntityManagerFactory(Configuration configuration, Properties props) {
        configuration.setProperties(props);
        getAnnotationConfiguration(configuration);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        System.out.println("Hibernate Java Config serviceRegistry created");

        SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
        return sf.unwrap(EntityManagerFactory.class);
    }

    private static void getAnnotationConfiguration(Configuration configuration) {
        configuration.addAnnotatedClass(Recipe.class);
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Role.class);
        configuration.addAnnotatedClass(Comment.class);
        configuration.addAnnotatedClass(Favorites.class);
        configuration.addAnnotatedClass(FavoritesId.class);
    }

    private static EntityManagerFactory getEntityManagerFactoryConfigDevelopment() {
        if (entityManagerFactory == null) entityManagerFactory = buildEntityFactoryConfig();
        return entityManagerFactory;
    }

    private static EntityManagerFactory getEntityManagerFactoryConfigTest() {
        if (entityManagerFactory == null) entityManagerFactory = setupHibernateConfigurationForTesting();
        return entityManagerFactory;
    }

    public static EntityManagerFactory getEntityManagerFactoryConfig(boolean isTest) {
        if (isTest) return getEntityManagerFactoryConfigTest();
        boolean isDeployed = (System.getenv("DEPLOYED") != null);
        if (isDeployed) return getEntityManagerFactoryConfigIsDeployed();
        return getEntityManagerFactoryConfigDevelopment();
    }
}
