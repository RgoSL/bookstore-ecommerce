package com.vendramel.vendramel.base.domain.excecoes;

/**
 * Lançada quando o Sistema de Cartão (simulado) não autoriza o pagamento.
 */
public class PagamentoRecusadoException extends RuntimeException {

    public PagamentoRecusadoException() {
        super("Pagamento não autorizado");
    }
}
