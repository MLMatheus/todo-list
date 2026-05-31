package github.mlmatheus.todolist.infrastructure.exception.handler;

import static org.assertj.core.api.Assertions.assertThat;

import github.mlmatheus.todolist.infrastructure.exception.TarefaNaoEncontradaException;
import github.mlmatheus.todolist.infrastructure.exception.TokenInvalidoException;
import github.mlmatheus.todolist.infrastructure.exception.ValidacaoDominioException;
import github.mlmatheus.todolist.service.dto.response.ErroResponse;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(
            Clock.fixed(Instant.parse("2026-05-30T10:00:00Z"), ZoneOffset.UTC));

    @SuppressWarnings("unused")
    private void dummy(String s) {
    }

    @Test
    void mapeiaValidacaoComMultiplosCamposEMensagens() throws Exception {
        Method method = getClass().getDeclaredMethod("dummy", String.class);
        MethodParameter param = new MethodParameter(method, 0);
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "obj");
        binding.addError(new FieldError("obj", "titulo", "O título é obrigatório"));
        binding.addError(new FieldError("obj", "titulo", "segundo erro"));
        binding.addError(new FieldError("obj", "prioridade", "prioridade inválida"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, binding);

        ResponseEntity<ErroResponse> resp = handler.handleValidacao(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErroResponse corpo = resp.getBody();
        assertThat(corpo).isNotNull();
        assertThat(corpo.httpStatus()).isEqualTo(400);
        assertThat(corpo.timestamp()).isEqualTo("2026-05-30 10:00:00.000");
        assertThat(corpo.erros()).containsKeys("titulo", "prioridade");
        assertThat(corpo.erros().get("titulo")).containsExactly("O título é obrigatório", "segundo erro");
    }

    @Test
    void mapeiaErroDeDominio() {
        ResponseEntity<ErroResponse> resp =
                handler.handleDominio(new ValidacaoDominioException("titulo", "O título é obrigatório"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().erros()).containsEntry("titulo",
                java.util.List.of("O título é obrigatório"));
    }

    @Test
    void mapeiaNaoEncontrada() {
        ResponseEntity<ErroResponse> resp =
                handler.handleNaoEncontrada(new TarefaNaoEncontradaException("t9"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().httpStatus()).isEqualTo(404);
        assertThat(resp.getBody().erros()).isNull();
    }

    @Test
    void mapeiaErroInterno() {
        ResponseEntity<ErroResponse> resp = handler.handleInterno(new RuntimeException("boom"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody().errorMessage()).isEqualTo("Erro interno do servidor");
    }

    @Test
    void mapeiaTokenInvalidoCom401() {
        ResponseEntity<ErroResponse> resp =
                handler.handleTokenInvalido(new TokenInvalidoException("Token sem a claim 'email'"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody().httpStatus()).isEqualTo(401);
    }
}
