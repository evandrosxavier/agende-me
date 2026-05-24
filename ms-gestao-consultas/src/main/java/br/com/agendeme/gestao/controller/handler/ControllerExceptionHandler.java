package br.com.agendeme.gestao.controller.handler;

import br.com.agendeme.gestao.excecoes.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ControllerExceptionHandler {

    private static final String BASE_URL = "https://api.agendeme.com/errors/";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handlerBusinessException(BusinessException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(e.getHttpStatus(), e.getMessage());
        problemDetail.setTitle("Erro de Negócio");
        problemDetail.setType(URI.create(BASE_URL + "business-error"));
        return ResponseEntity.status(e.getHttpStatus()).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Erro de Validação");
        problemDetail.setDetail("Um ou mais campos falharam na validação.");
        problemDetail.setType(URI.create(BASE_URL + "validation-error"));
        List<String> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        problemDetail.setProperty("validation_errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handlerMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Parâmetro Inválido");
        problemDetail.setType(URI.create(BASE_URL + "validation-error"));
        Class<?> requiredType = e.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            String valoresValidos = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            problemDetail.setDetail("Valor inválido '" + e.getValue() + "' para o parâmetro '" + e.getName() +
                    "'. Valores aceitos: " + valoresValidos);
        } else {
            problemDetail.setDetail("Valor inválido '" + e.getValue() + "' para o parâmetro '" + e.getName() + "'.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handlerHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setType(URI.create(BASE_URL + "validation-error"));
        problemDetail.setTitle("Erro de Validação");

        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException invalidFormat && invalidFormat.getTargetType() != null
                && invalidFormat.getTargetType().isEnum()) {
            String campo = invalidFormat.getPath().isEmpty() ? "desconhecido"
                    : invalidFormat.getPath().get(invalidFormat.getPath().size() - 1).getFieldName();
            String valoresValidos = Arrays.stream(invalidFormat.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            problemDetail.setDetail("Valor inválido '" + invalidFormat.getValue() +
                    "' para o campo '" + campo + "'. Valores aceitos: " + valoresValidos);
        } else {
            problemDetail.setDetail("Erro ao processar a requisição. Verifique se todos os campos obrigatórios foram preenchidos corretamente.");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handlerBadCredentials(BadCredentialsException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setTitle("Erro de Autenticação");
        problemDetail.setDetail("Login ou senha inválidos.");
        problemDetail.setType(URI.create(BASE_URL + "authentication-error"));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ProblemDetail> handlerDisabled(DisabledException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problemDetail.setTitle("Usuário Inativo");
        problemDetail.setDetail("Usuário inativo. Entre em contato com o administrador.");
        problemDetail.setType(URI.create(BASE_URL + "disabled-user-error"));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }
}