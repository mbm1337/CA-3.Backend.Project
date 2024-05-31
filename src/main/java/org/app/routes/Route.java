package org.app.routes;

import org.app.daos.CommentDAO;
import org.app.daos.RecipeDAO;
import org.app.daos.UserDAO;
import org.app.persistence.HibernateConfig;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

public class Route {
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryConfig(false);
    private static RecipeDAO recipeDAO = RecipeDAO.getInstance(emf);
    private static UserDAO userDAO = UserDAO.getInstance(emf);
    private static CommentDAO commentDAO = CommentDAO.getInstance(emf);
    private static routes.RouteRecipe routeRecipe = new routes.RouteRecipe(recipeDAO, userDAO);


    private static RouteComment routeComment = new RouteComment(commentDAO, userDAO,recipeDAO);
    private static RouteUser routeUser = new RouteUser();


    public static EndpointGroup addRoutes() {
        return combineRoutes(routeRecipe.recipeRoutes(), routeComment.commentRoutes(), routeUser.securityRoutes());
    }

    private static EndpointGroup combineRoutes(EndpointGroup... endpointGroups) {
        return () -> {
            for (EndpointGroup group : endpointGroups) {
                group.addEndpoints();
            }
        };
    }
}
