package controllers;

import daos.CommentDAO;
import daos.RecipeDAO;
import daos.UserDAO;
import dtos.CommentDTO;
import exceptions.ApiException;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import persistence.model.Comment;
import persistence.model.Recipe;
import persistence.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentController {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String timestamp = dateFormat.format(new Date());

    public static CommentDTO convertToDTO(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .text(comment.getText())
                .date(comment.getDate())
                .build();
    }

    public static Handler getAllByRecipeId(CommentDAO dao) {
        return ctx -> {
            Integer recipeId = Integer.parseInt(ctx.pathParam("recipe_id"));
            List<Comment> commentList = dao.getAllByRecipeId(recipeId);
            List<CommentDTO> dtoList = new ArrayList<>();
            for (Comment i : commentList) {
                dtoList.add(convertToDTO(i));
            }
            if (dtoList.isEmpty()) {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "No comments were found.", timestamp);
            } else {
                ctx.status(HttpStatus.OK).json(dtoList);
            }
        };
    }

    public static Handler delete(CommentDAO dao) {
        return ctx -> {
            int id = ctx.bodyAsClass(Integer.class);
            Comment foundComment = dao.getById(id);
            if (foundComment != null) {
                CommentDTO dto = convertToDTO(foundComment);
                dao.delete(foundComment.getId());
                ctx.status(HttpStatus.OK).json(dto);
            } else {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "Comment was not found: " + id, timestamp);
            }
        };
    }

    public static Handler create(CommentDAO dao, RecipeDAO recipeDAO, UserDAO userDAO) {
        return ctx -> {
            try {
                // Parse and validate input data
                Comment comment = ctx.bodyAsClass(Comment.class);
                Integer recipeId = Integer.parseInt(ctx.pathParam("recipe_id"));
                String userEmail = ctx.header("email");

                if (userEmail == null || userEmail.isEmpty()) {
                    throw new ApiException(HttpStatus.BAD_REQUEST.getCode(), "Email header is missing or empty.", timestamp);
                }

                // Retrieve associated entities
                Recipe recipe = recipeDAO.getById(recipeId);
                User user = userDAO.getByString(userEmail);

                if (recipe == null) {
                    throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "Recipe not found.", timestamp);
                }

                if (user == null) {
                    throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "User not found.", timestamp);
                }

                // Set the relations and create the comment
                comment.setRecipe(recipe);
                comment.setUser(user);
                comment.setDate(new Date()); // Set the date to the current date and time
                Comment createdComment = dao.create(comment);
                CommentDTO dto = convertToDTO(createdComment);

                if (dto != null) {
                    ctx.status(HttpStatus.OK).json(dto);
                } else {
                    throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "Couldn't create Comment.", timestamp);
                }
            } catch (NumberFormatException e) {
                throw new ApiException(HttpStatus.BAD_REQUEST.getCode(), "Invalid recipe ID format.", timestamp);
            } catch (ApiException e) {
                ctx.status(e.getStatusCode()).json(e.getMessage());
            } catch (Exception e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Unexpected error occurred: " + e.getMessage());
            }
        };
    }

    public static Handler update(CommentDAO dao, UserDAO userDAO) {
        return ctx -> {
            Comment comment = ctx.bodyAsClass(Comment.class);
            int i = dao.update(comment);
            if (i > 0) {
                CommentDTO dto = convertToDTO(comment);
                ctx.json(dto);
            } else {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "Comment not found for id: " + comment.getId(), timestamp);
            }
        };
    }
}
