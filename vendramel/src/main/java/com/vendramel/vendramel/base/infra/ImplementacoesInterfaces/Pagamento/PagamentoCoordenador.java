package com.vendramel.vendramel.base.infra.ImplementacoesInterfaces.Pagamento;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vendramel.vendramel.base.domain.Services.CarrinhoService;
import com.vendramel.vendramel.base.domain.Services.LivroService;
import com.vendramel.vendramel.base.domain.Services.PagamentoService;
import com.vendramel.vendramel.base.domain.Services.PedidoService;
import com.vendramel.vendramel.base.domain.entidades.Pagamento;
import com.vendramel.vendramel.base.domain.excecoes.PagamentoRecusadoException;
import com.vendramel.vendramel.base.domain.interfaces.PagamentoRepository;
import com.vendramel.vendramel.base.domain.interfaces.ProcessarPagamentoInterface;
import com.vendramel.vendramel.base.domain.regras.StatusPedido;
import com.vendramel.vendramel.base.infra.mapper.Pedido.PedidoMapper;
import com.vendramel.vendramel.base.infra.repository.Pedido.PedidoJpaRepository;

@Service
public class PagamentoCoordenador implements ProcessarPagamentoInterface {

    private final PagamentoService pagamentoService;
    private final PagamentoRepository pagamentoRepository;
    private final PedidoService pedidoService;
    private final LivroService livroService;
    private final CarrinhoService carrinhoService;
    private final PedidoJpaRepository pedidoJpaRepository;

    public PagamentoCoordenador(
            PagamentoService pagamentoService,
            PagamentoRepository pagamentoRepository,
            PedidoService pedidoService,
            LivroService livroService,
            CarrinhoService carrinhoService,
            PedidoJpaRepository pedidoJpaRepository) {

        this.pagamentoService = pagamentoService;
        this.pagamentoRepository = pagamentoRepository;
        this.pedidoService = pedidoService;
        this.livroService = livroService;
        this.carrinhoService = carrinhoService;
        this.pedidoJpaRepository = pedidoJpaRepository;
    }

    /**
     * Uma única transação. Se o pagamento for recusado, a tentativa recusada
     * é gravada (para a regra "só a 1ª tentativa é recusada") e a transação
     * NÃO é desfeita; o pedido continua em processamento.
     * Qualquer outro erro (por exemplo, estoque insuficiente) desfaz tudo.
     */
    @Override
    @Transactional(noRollbackFor = PagamentoRecusadoException.class)
    public Pagamento executar(
            int pedidoId,
            String tipo,
            int parcelas,
            BigDecimal valorFinal) {

        // 1. Busca o pedido com bloqueio
        var pedidoPersistencia = pedidoJpaRepository
                .buscarPorIdComBloqueio(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Pedido não encontrado."));

        // 2. Só pedidos em processamento podem receber pagamento
        if (!StatusPedido.EM_PROCESSAMENTO.equalsIgnoreCase(
                pedidoPersistencia.getStatus())) {
            throw new IllegalStateException(
                    "Este pedido não está disponível para pagamento.");
        }

        // 3. Converte o pedido para domínio
        var pedido = PedidoMapper.paraDominio(pedidoPersistencia);

        // 4. Executa a estratégia de pagamento (Pix ou Cartão)
        Pagamento pagamento = pagamentoService.pagar(
                pedido,
                tipo,
                parcelas,
                valorFinal
        );

        // 5. Persiste a tentativa (aprovada ou recusada)
        Pagamento pagamentoSalvo = pagamentoRepository.salvar(
                pagamento,
                pedidoId
        );

        // 6. Pagamento recusado: o pedido continua em processamento
        if (!"APROVADO".equalsIgnoreCase(pagamentoSalvo.getStatus())) {
            throw new PagamentoRecusadoException();
        }

        // 7. Atualiza o estoque
        livroService.atualizarEstoque(pedido.getItens());

        // 8. Confirma o pedido
        pedidoService.confirmarPedido(pedidoId);

        // 9. Destrói os itens do carrinho
        int clienteId = pedidoPersistencia.getCliente().getId();
        var carrinho = carrinhoService.visualizarCarrinho(clienteId);
        carrinhoService.destruirItens(carrinho.getId());

        return pagamentoSalvo;
    }
}
