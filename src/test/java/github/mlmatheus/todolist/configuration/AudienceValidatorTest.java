package github.mlmatheus.todolist.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class AudienceValidatorTest {

    private static final String AUDIENCE = "client-123.apps.googleusercontent.com";

    private final AudienceValidator validator = new AudienceValidator(AUDIENCE);

    private Jwt jwtComAudience(List<String> audiencias) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "none").subject("sub1");
        if (audiencias != null) {
            builder.claim("aud", audiencias);
        }
        return builder.build();
    }

    @Test
    void aceitaQuandoAudienceContemOEsperado() {
        Jwt jwt = jwtComAudience(List.of("outro", AUDIENCE));
        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    void rejeitaQuandoAudienceDiferente() {
        Jwt jwt = jwtComAudience(List.of("outro-client"));
        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }

    @Test
    void rejeitaQuandoSemAudience() {
        Jwt jwt = jwtComAudience(null);
        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }
}
