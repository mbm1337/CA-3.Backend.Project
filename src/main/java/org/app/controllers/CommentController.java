package org.app.controllers;

import org.app.daos.CommentDAO;
import org.app.daos.RecipeDAO;
import org.app.daos.UserDAO;
import org.app.dtos.CommentDTO;
import org.app.exceptions.ApiException;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.app.persistence.model.Comment;
import org.app.persistence.model.Recipe;
import org.app.persistence.model.User;

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
                .userEmail(comment.getUser().getEmail()) // Set the user's email
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

    public static Handler delete(CommentDAO dao,UserDAO userDAO) {
        return ctx -> {
            String email = ctx.pathParam("user_id");
            int id = ctx.bodyAsClass(Integer.class);


            Comment foundComment = dao.getById(id);
            if(foundComment == null) {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "Comment was not found: " + id, timestamp);
            }

            User currentUser = userDAO.getByString(email);
            if (currentUser == null) {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "User was not found: " + email, timestamp);
            }

            boolean isAdmin = currentUser.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("admin"));
            boolean isOwner = foundComment.getUser().getEmail().equals(email);

            if (isAdmin || isOwner) {
                CommentDTO dto = convertToDTO(foundComment);
                dao.delete(foundComment.getId());
                ctx.status(HttpStatus.OK).json(dto);
            } else {
                throw new ApiException(HttpStatus.FORBIDDEN.getCode(), "You are not authorized to delete this comment", timestamp);
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
