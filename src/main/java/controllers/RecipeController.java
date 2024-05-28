package controllers;

import daos.RecipeDAO;
import daos.UserDAO;
import dtos.RecipeDTO;
import exceptions.ApiException;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import persistence.model.Recipe;
import persistence.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecipeController {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String timestamp = dateFormat.format(new Date());

    public static RecipeDTO convertToDTO(Recipe recipe) {
        return RecipeDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .ingredients(recipe.getIngredients())
                .instructions(recipe.getInstructions())
                .imageUrl(recipe.getImageUrl())
                .build();
    }

    public static Handler getAllByEmail(RecipeDAO dao) {
        return ctx -> {
            String email = ctx.pathParam("user_id");
            List<Recipe> recipeList = dao.getAllByEmail(email);
            List<RecipeDTO> dtoList = new ArrayList<>();
            for (Recipe i : recipeList) {
                dtoList.add(convertToDTO(i));
            }
            if (dtoList.isEmpty()) {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "No recipe were found.", timestamp);
            } else {
                ctx.status(HttpStatus.OK).json(dtoList);
            }
        };
    }

    public static Handler getAll(RecipeDAO dao) {
        return ctx -> {
            List<Recipe> recipeList = dao.getAll();
            List<RecipeDTO> dtoList = new ArrayList<>();
            for (Recipe i : recipeList) {
                dtoList.add(convertToDTO(i));
            }
            if (dtoList.isEmpty()) {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "No recipe were found.", timestamp);
            } else {
                ctx.status(HttpStatus.OK).json(dtoList);
            }
        };
    }

    public static Handler delete(RecipeDAO dao) {
        return ctx -> {
            String email = ctx.pathParam("user_id");
            int id = ctx.bodyAsClass(Integer.class);
            Recipe foundRecipe = dao.getById(id);
            if (foundRecipe != null) {
                RecipeDTO dto = convertToDTO(foundRecipe);
                dao.delete(foundRecipe.getId());
                ctx.status(HttpStatus.OK).json(dto);
            } else {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "Recipe was not found: " + id, timestamp);
            }
        };
    }


    public static Handler getById(RecipeDAO dao) {
        return ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            try {
                Recipe foundRecipe = dao.getById(id);
                if (foundRecipe != null) {
                    RecipeDTO dto = convertToDTO(foundRecipe);
                    if (dto != null) {
                        ctx.status(HttpStatus.OK).json(dto);
                    }
                } else {
                    throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "Recipe was not found: " + id, timestamp);
                }
            } catch (NumberFormatException e) {
                // Handle invalid number format for id
                ctx.status(HttpStatus.BAD_REQUEST.getCode()).json("Invalid id format: " + e.getMessage());
            } catch (ApiException e) {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "Recipe was not found: " + id, timestamp);
            }
        };
    }

    public static Handler create(RecipeDAO dao) {
        return ctx -> {
            Recipe recipe = ctx.bodyAsClass(Recipe.class);
            Recipe createdRecipe = dao.create(recipe);
            RecipeDTO dto = convertToDTO(createdRecipe);
            if (dto != null) {
                ctx.status(HttpStatus.OK).json(dto);
            } else {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "Couldn't create Recipe.", timestamp);
            }
        };
    }

    public static Handler update(RecipeDAO recipeDAO, UserDAO userDAO) {
        return ctx -> {
            String email = ctx.pathParam("user_id");
            Recipe recipe = ctx.bodyAsClass(Recipe.class);
            User user = userDAO.getByString(email);
            if (user != null) {
                recipe.setUser(user);
                int i = recipeDAO.update(recipe);
                if (i > 0) {
                    RecipeDTO dto = convertToDTO(recipe);
                    ctx.json(dto);
                } else {
                    throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "Recipe not found for id: " + recipe.getId(), timestamp);
                }
            } else {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "User not found for email: " + user.getEmail(), timestamp);
            }
        };
    }


    public static Handler addFavorite(RecipeDAO dao) {
        return ctx -> {
            String userEmail = ctx.pathParam("user_email");
            int recipeId = Integer.parseInt(ctx.pathParam("recipe_id"));
            dao.addFavorite(userEmail, recipeId);
            ctx.status(HttpStatus.OK);
        };
    }

    public static Handler removeFavorite(RecipeDAO dao) {
        return ctx -> {
            String userEmail = ctx.pathParam("user_email");
            int recipeId = Integer.parseInt(ctx.pathParam("recipe_id"));
            dao.removeFavorite(userEmail, recipeId);
            ctx.status(HttpStatus.OK);
        };
    }
}

