package routes;

import controllers.CommentController;
import controllers.SecurityController;
import daos.CommentDAO;
import daos.RecipeDAO;
import daos.UserDAO;
import daos.CommentDAO;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;
import logger.CustomLogger;
import persistence.HibernateConfig;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RouteComment {

    private static CommentDAO commentDAO;
    private static UserDAO userDAO;
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryConfig(false);
    private static SecurityController securityController = new SecurityController(emf);
    private static CustomLogger customLogger = new CustomLogger();
    private static RecipeDAO recipeDAO = RecipeDAO.getInstance(emf);

    public RouteComment(CommentDAO commentDAO, UserDAO userDAO, RecipeDAO recipeDAO) {
        this.commentDAO = commentDAO;
        this.userDAO = userDAO;
        this.recipeDAO = recipeDAO;
    }

    public EndpointGroup commentRoutes() {
        return () -> {
            path("/comment", () -> {
                before(securityController.authenticate());
                path("/{recipe_id}", () -> {
                    get("/", customLogger.handleExceptions(CommentController.getAllByRecipeId(commentDAO)), Role.ANYONE);
                    post("/", customLogger.handleExceptions(CommentController.create(commentDAO, recipeDAO, userDAO)), Role.USER, Role.ADMIN);
                });
                put("/{user_id}", customLogger.handleExceptions(CommentController.update(commentDAO, userDAO)), Role.USER, Role.ADMIN);
                delete("/", customLogger.handleExceptions(CommentController.delete(commentDAO)), Role.USER, Role.ADMIN);
            });
        };
    }
}
