package routes;

import controllers.RecipeController;
import controllers.SecurityController;
import daos.RecipeDAO;
import daos.UserDAO;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;
import logger.CustomLogger;
import persistence.HibernateConfig;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RouteRecipe {

    private static RecipeDAO recipeDAO;
    private static UserDAO userDAO;
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryConfig(false);
    private static SecurityController securityController = new SecurityController(emf);
    private static CustomLogger customLogger = new CustomLogger();

    public RouteRecipe(RecipeDAO recipeDAO, UserDAO userDAO) {
        this.recipeDAO = recipeDAO;
        this.userDAO = userDAO;
    }

    public EndpointGroup recipeRoutes() {
        return () -> {
            path("/recipe", () -> {
                before(securityController.authenticate());
                get("/", customLogger.handleExceptions(RecipeController.getAll(recipeDAO)), Role.ANYONE);
                get("/{id}", customLogger.handleExceptions(RecipeController.getById(recipeDAO)), Role.ANYONE);
                get("/personal/{user_id}", customLogger.handleExceptions(RecipeController.getAllByEmail(recipeDAO)), Role.USER, Role.ADMIN);
                post("/", customLogger.handleExceptions(RecipeController.create(recipeDAO)), Role.USER, Role.ADMIN);
                put("/{user_id}", customLogger.handleExceptions(RecipeController.update(recipeDAO, userDAO)), Role.USER, Role.ADMIN);
                delete("/{user_id}", customLogger.handleExceptions(RecipeController.delete(recipeDAO)), Role.USER, Role.ADMIN);
            });
        };
    }
}
