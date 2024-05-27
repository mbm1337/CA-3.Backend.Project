package routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.SecurityController;
import daos.UserDAO;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;
import logger.CustomLogger;
import persistence.HibernateConfig;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RouteUser {
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryConfig(false);
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static SecurityController securityController = new SecurityController(emf);
    private static CustomLogger customLogger = new CustomLogger();
    private static UserDAO userDAO = UserDAO.getInstance(emf);

    public EndpointGroup securityRoutes() {
        return () -> {
            path("/auth", () -> {
                before(securityController.authenticate());
                post("/login", customLogger.handleExceptions(securityController.login()), Role.ANYONE);
                post("/register", customLogger.handleExceptions(securityController.register()), Role.ANYONE);
                post("/addroletouser", customLogger.handleExceptions(securityController.addRoleToUser()), Role.ADMIN);
                get("/users", customLogger.handleExceptions(securityController.getAllUsers(userDAO)), Role.ADMIN);
                get("/roles", customLogger.handleExceptions(securityController.getAllRoles(userDAO)), Role.ADMIN);
                delete("/delete", customLogger.handleExceptions(securityController.deleteUser()), Role.ADMIN);
            });
        };
    }
}

