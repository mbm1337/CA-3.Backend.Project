package org.app.routes;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {ANYONE, USER, ADMIN}
