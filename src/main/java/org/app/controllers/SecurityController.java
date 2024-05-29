package org.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.app.daos.UserDAO;
import org.app.dtos.TokenDTO;
import org.app.dtos.UserDTO;
import org.app.dtos.UserRoleDTO;
import org.app.exceptions.ApiException;
import org.app.exceptions.NotAuthorizedException;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManagerFactory;
import org.app.persistence.model.Role;
import org.app.persistence.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String timestamp = dateFormat.format(new Date());

    UserDAO userDAO;

    public SecurityController(EntityManagerFactory emf) {
        this.userDAO = UserDAO.getInstance(emf);
    }

    ObjectMapper objectMapper = new ObjectMapper();

    public Handler addRoleToUser() {
        return (ctx) -> {
            UserRoleDTO user = ctx.bodyAsClass(UserRoleDTO.class);
            User updatedUser = userDAO.addUserRole(user.getEmail(), user.getRole());
            if (updatedUser == null) {
                throw new NotAuthorizedException(HttpStatus.UNAUTHORIZED.getCode(), "Try again. Something went wrong. ", timestamp);
            } else {
                ctx.status(HttpStatus.CREATED).json(updatedUser);
            }
        };
    }

    public Handler getAllUsers(UserDAO dao) {
        return ctx -> {
            List<User> users = dao.getAllUsers();
            if (users.isEmpty()) {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "No users were found.", timestamp);
            } else {
                ctx.status(HttpStatus.OK).json(users);
            }
        };
    }

    public Handler getAllRoles(UserDAO dao) {
        return ctx -> {
            List<Role> roles = dao.getAllRoles();
            if (roles.isEmpty()) {
                throw new ApiException(HttpStatus.NOT_FOUND.getCode(), "No roles were found.", timestamp);
            } else {
                ctx.status(HttpStatus.OK).json(roles);
            }
        };
    }

    @Override
    public Handler register() {
        return (ctx) -> {
            UserDTO userInput = ctx.bodyAsClass(UserDTO.class);
            User created = userDAO.createUser(userInput.getEmail(), userInput.getPassword());
            if (created == null) {
                throw new NotAuthorizedException(HttpStatus.UNAUTHORIZED.getCode(), "Email is already taken. Try again. ", timestamp);
            } else {
                String token = createToken(new UserDTO(created));
                ctx.status(HttpStatus.CREATED).json(new TokenDTO(token, created));
            }
        };
    }

    @Override
    public Handler login() {
        return (ctx) -> {
            UserDTO user = ctx.bodyAsClass(UserDTO.class);
            User verifiedUserEntity = userDAO.verifyUser(user.getEmail(), user.getPassword());
            if (verifiedUserEntity == null) {
                throw new NotAuthorizedException(HttpStatus.UNAUTHORIZED.getCode(), "Wrong login information. Try again please.", timestamp);
            } else {
                String token = createToken(new UserDTO(verifiedUserEntity));
                ctx.status(200).json(new TokenDTO(token, verifiedUserEntity));
            }
        };
    }

    @Override
    public Handler deleteUser() {
        return (ctx) -> {
            String email = ctx.bodyAsClass(String.class);
            User deletedUser = userDAO.deleteUser(email);
            if (deletedUser == null) {
                throw new NotAuthorizedException(HttpStatus.UNAUTHORIZED.getCode(), "User not found. ", timestamp);
            } else {
                ctx.status(HttpStatus.OK).json(deletedUser);
            }
        };
    }

    @Override
    public String createToken(UserDTO user) {
        String ISSUER;
        String TOKEN_EXPIRE_TIME;
        String SECRET_KEY;

        if (System.getenv("DEPLOYED") != null) {
            ISSUER = System.getenv("ISSUER");
            TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
            SECRET_KEY = System.getenv("SECRET_KEY");
        } else {
            ISSUER = "Gruppe4";
            TOKEN_EXPIRE_TIME = "1800000";
            SECRET_KEY = "46afec1b015f722c6334baef96181cdf561813536093447ff285b0bc53fa9120";
        }
        return createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
    }

    @Override
    public boolean authorize(UserDTO user, Set<String> allowedRoles) {
        if (user == null || user.getRoles() == null) {
            return false;
        }

        Set<String> roles = user.getRoles();
        System.out.println("User roles: " + roles);

        for (String role : roles) {
            System.out.println("User role: " + role);
            if (allowedRoles.contains(role.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private String createToken(UserDTO user, String ISSUER, String TOKEN_EXPIRE_TIME, String SECRET_KEY) {
        try {
            JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .issuer(ISSUER)
                    .claim("email", user.getEmail())
                    .claim("roles", user.getRoles()); // Store roles as a set

            JWTClaimsSet claimsSet = claimsSetBuilder
                    .expirationTime(new Date(new Date().getTime() + Integer.parseInt(TOKEN_EXPIRE_TIME)))
                    .build();

            Payload payload = new Payload(claimsSet.toJSONObject());

            JWSSigner signer = new MACSigner(SECRET_KEY);
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
            JWSObject jwsObject = new JWSObject(jwsHeader, payload);
            jwsObject.sign(signer);
            String serializedToken = jwsObject.serialize();

            return serializedToken;
        } catch (JOSEException e) {
            e.printStackTrace();
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "Could not create token", timestamp);
        }
    }


    public Handler authenticate() {
        ObjectNode returnObject = objectMapper.createObjectNode();
        return (ctx) -> {
            if (ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }

            String header = ctx.header("Authorization");
            if (header == null) {
                ctx.status(HttpStatus.FORBIDDEN).json(returnObject.put("msg", "Authorization header missing"));
                return;
            }

            String token = header.split(" ")[1];
            if (token == null) {
                ctx.status(HttpStatus.FORBIDDEN).json(returnObject.put("msg", "Authorization header malformed"));
                return;
            }

            UserDTO verifiedTokenUser = verifyToken(token);
            if (verifiedTokenUser == null) {
                ctx.status(HttpStatus.FORBIDDEN).json(returnObject.put("msg", "Invalid User or Token"));
            }
            System.out.println("USER IN AUTHENTICATE: " + verifiedTokenUser);
            ctx.attribute("user", verifiedTokenUser);
        };
    }

    public UserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : "46afec1b015f722c6334baef96181cdf561813536093447ff285b0bc53fa9120";

        try {
            if (tokenIsValid(token, SECRET) && tokenNotExpired(token)) {
                return getUserWithRolesFromToken(token);
            } else {
                throw new NotAuthorizedException(HttpStatus.UNAUTHORIZED.getCode(), "Token is not valid", timestamp);
            }
        } catch (ParseException | JOSEException | NotAuthorizedException e) {
            e.printStackTrace();
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token", timestamp);
        }
    }

    public boolean tokenNotExpired(String token) throws ParseException, NotAuthorizedException {
        if (timeToExpire(token) > 0)
            return true;
        else
            throw new NotAuthorizedException(HttpStatus.FORBIDDEN.getCode(), "Token has expired", timestamp);
    }

    public boolean tokenIsValid(String token, String secret) throws ParseException, JOSEException, NotAuthorizedException {
        SignedJWT jwt = SignedJWT.parse(token);
        if (jwt.verify(new MACVerifier(secret)))
            return true;
        else
            throw new NotAuthorizedException(HttpStatus.FORBIDDEN.getCode(), "Token is not valid", timestamp);
    }

    public int timeToExpire(String token) throws ParseException, NotAuthorizedException {
        SignedJWT jwt = SignedJWT.parse(token);
        return (int) (jwt.getJWTClaimsSet().getExpirationTime().getTime() - new Date().getTime());
    }

    public UserDTO getUserWithRolesFromToken(String token) throws ParseException {
        SignedJWT jwt = SignedJWT.parse(token);
        // Get the roles claim as an array
        List<String> rolesList = (List<String>) jwt.getJWTClaimsSet().getClaim("roles");
        String email = jwt.getJWTClaimsSet().getClaim("email").toString();

        Set<String> rolesSet = rolesList.stream()
                .map(String::trim)
                .collect(Collectors.toSet());

        return new UserDTO(email, rolesSet);
    }
}
