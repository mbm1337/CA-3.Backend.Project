package routes;

import daos.CommentDAO;
import daos.RecipeDAO;
import daos.UserDAO;
import persistence.HibernateConfig;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

public class Route {
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryConfig(false);
    private static RecipeDAO recipeDAO = RecipeDAO.getInstance(emf);
    private static UserDAO userDAO = UserDAO.getInstance(emf);
    private static CommentDAO CommentDAO = daos.CommentDAO.getInstance(emf);
    private static RouteRecipe routeRecipe = new RouteRecipe(recipeDAO, userDAO);


    private static RouteComment routeComment = new RouteComment(CommentDAO, userDAO,recipeDAO);
    private static RouteUser routeUser = new RouteUser();


    public static EndpointGroup addRoutes() {
        return combineRoutes(routeRecipe.recipeRoutes(), routeUser.securityRoutes());
    }

    private static EndpointGroup combineRoutes(EndpointGroup... endpointGroups) {
        return () -> {
            for (EndpointGroup group : endpointGroups) {
                group.addEndpoints();
            }
        };
    }
}
