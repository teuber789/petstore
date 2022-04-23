package com.petstore.petsservice.category;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CategoryRouter {

    @Bean
    public RouterFunction<ServerResponse> router(CategoryHandler categoryHandler) {
        return RouterFunctions
                .nest(
                        path("/categories"),
                        route(GET("").and(accept(MediaType.APPLICATION_JSON)), categoryHandler::getAll)
                                .andRoute(POST("").and(accept(MediaType.APPLICATION_JSON)), categoryHandler::create)
                );
    }
}
