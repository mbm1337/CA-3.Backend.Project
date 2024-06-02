package org.app.routes;

import org.app.controllers.CommentController;
import org.app.controllers.SecurityController;
import org.app.daos.CommentDAO;
import org.app.daos.RecipeDAO;
import org.app.daos.UserDAO;
import org.app.daos.CommentDAO;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;
import org.app.logger.CustomLogger;
import org.app.persistence.HibernateConfig;

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
                delete("/{user_id}", customLogger.handleExceptions(CommentController.delete(commentDAO,userDAO)), Role.USER, Role.ADMIN);
            });
        };
    }
}
