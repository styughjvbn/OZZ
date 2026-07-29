package com.ssafy.ozz.monolith.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI ozzOpenApi() {
        String scheme = "JWT";
        return new OpenAPI()
                .info(new Info().title("OZZ API").version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(
                        scheme,
                        new SecurityScheme()
                                .name(scheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }

    @Bean
    GroupedOpenApi authApi() {
        return group("auth", "/api/auth/**", "/login/guest");
    }

    @Bean
    GroupedOpenApi userApi() {
        return group("user", "/api/users/**");
    }

    @Bean
    GroupedOpenApi clothesApi() {
        return group("clothes", "/api/clothes/**", "/api/categories/**", "/api/coordinates/**");
    }

    @Bean
    GroupedOpenApi favoriteApi() {
        return group("favorite", "/api/favorites/**");
    }

    @Bean
    GroupedOpenApi boardApi() {
        return group("board", "/api/boards/**", "/api/boardlikes/**", "/api/notifications/**");
    }

    @Bean
    GroupedOpenApi fileApi() {
        return group("file", "/api/file/**");
    }

    private static GroupedOpenApi group(String name, String... paths) {
        return GroupedOpenApi.builder().group(name).pathsToMatch(paths).build();
    }
}
