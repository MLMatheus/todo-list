package github.mlmatheus.todolist.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadados OpenAPI/Swagger e esquema de segurança bearer JWT. */
@Configuration
public class SwaggerConfiguration {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI todoListOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Todo List API")
                        .description("API de gerenciamento de tarefas para usuários autenticados via Google.")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT (ID token) do Google: validado por assinatura, expiração e audiência.")));
    }
}
