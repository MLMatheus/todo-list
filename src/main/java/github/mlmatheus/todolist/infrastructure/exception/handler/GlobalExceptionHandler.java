package github.mlmatheus.todolist.infrastructure.exception.handler;

import github.mlmatheus.todolist.infrastructure.exception.TarefaNaoEncontradaException;
import github.mlmatheus.todolist.infrastructure.exception.TokenInvalidoException;
import github.mlmatheus.todolist.infrastructure.exception.ValidacaoDominioException;
import github.mlmatheus.todolist.infrastructure.util.DateFormatterUtils;
import github.mlmatheus.todolist.service.dto.response.ErroResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Tratamento padronizado de erros da API. */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, List<String>> erros = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erros.computeIfAbsent(fieldError.getField(), chave -> new ArrayList<>())
                    .add(fieldError.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Erro de validação", erros);
    }

    @ExceptionHandler(ValidacaoDominioException.class)
    public ResponseEntity<ErroResponse> handleDominio(ValidacaoDominioException ex) {
        Map<String, List<String>> erros = new LinkedHashMap<>();
        erros.put(ex.getCampo(), List.of(ex.getMessage()));
        return build(HttpStatus.BAD_REQUEST, "Erro de validação", erros);
    }

    @ExceptionHandler(TarefaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrada(TarefaNaoEncontradaException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErroResponse> handleTokenInvalido(TokenInvalidoException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleInterno(Exception ex) {
        log.error("Erro interno não tratado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", null);
    }

    private ResponseEntity<ErroResponse> build(HttpStatus status, String mensagem,
                                               Map<String, List<String>> erros) {
        ErroResponse corpo = new ErroResponse(
                DateFormatterUtils.format(LocalDateTime.now(clock)),
                status.value(),
                mensagem,
                erros);
        return ResponseEntity.status(status).body(corpo);
    }
}
