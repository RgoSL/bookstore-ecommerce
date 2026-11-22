package com.vendramel.vendramel.base.presentation;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vendramel.vendramel.base.domain.excecoes.PagamentoRecusadoException;

/**
 * Converte as exceções em respostas JSON com a mensagem exibida ao cliente:
 * {"mensagem": "..."}.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PagamentoRecusadoException.class)
    public ResponseEntity<Map<String, String>> pagamentoRecusado(
            PagamentoRecusadoException ex) {
        return resposta(HttpStatus.PAYMENT_REQUIRED, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(
            IllegalArgumentException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> estadoInvalido(
            IllegalStateException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Map<String, String>> resposta(
            HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("mensagem", mensagem));
    }
}
