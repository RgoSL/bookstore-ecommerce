package com.vendramel.vendramel.base.domain.regras;

/**
 * RN05: status possíveis de um pedido.
 */
public final class StatusPedido {

    public static final String EM_PROCESSAMENTO = "EM_PROCESSAMENTO";
    public static final String CONFIRMADO = "CONFIRMADO";
    public static final String EM_TRANSPORTE = "EM_TRANSPORTE";
    public static final String FINALIZADO = "FINALIZADO";
    public static final String CANCELADO = "CANCELADO";

    private StatusPedido() {
    }
}
