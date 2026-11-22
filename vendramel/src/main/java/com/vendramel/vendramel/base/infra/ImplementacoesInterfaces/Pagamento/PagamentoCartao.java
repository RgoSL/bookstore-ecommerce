package com.vendramel.vendramel.base.infra.ImplementacoesInterfaces.Pagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.vendramel.vendramel.base.domain.entidades.Pagamento;
import com.vendramel.vendramel.base.domain.entidades.Pedido;
import com.vendramel.vendramel.base.domain.interfaces.PagamentoInterface;
import com.vendramel.vendramel.base.infra.repository.Pagamento.PagamentoJpaRepository;

/**
 * Sistema de Cartão (simulado).
 * Recusa a 1ª tentativa de pagamento de um pedido cujo total seja maior ou
 * igual a R$ 1.000,00. As tentativas seguintes são aprovadas.
 */
@Component("CARTAO")
public class PagamentoCartao implements PagamentoInterface {

    private static final BigDecimal LIMITE_RECUSA = new BigDecimal("1000.00");

    private final PagamentoJpaRepository pagamentoRepository;

    public PagamentoCartao(PagamentoJpaRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    @Override
    public Pagamento pagar(Pedido pedido, BigDecimal valorFinal) {

        boolean primeiraTentativa =
                pagamentoRepository.findAllByPedido_Id(pedido.getId()).isEmpty();

        boolean recusar = primeiraTentativa
                && pedido.getValorTotal() != null
                && pedido.getValorTotal().compareTo(LIMITE_RECUSA) >= 0;

        Pagamento pagamento = new Pagamento();
        pagamento.setFormaPagamento("CARTAO");
        pagamento.setValor(valorFinal);
        pagamento.setStatus(recusar ? "RECUSADO" : "AUTORIZADO");
        pagamento.setDataPagamento(LocalDateTime.now());

        return pagamento;
    }
}
