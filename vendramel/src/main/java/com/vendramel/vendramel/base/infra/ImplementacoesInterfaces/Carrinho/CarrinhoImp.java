package com.vendramel.vendramel.base.infra.ImplementacoesInterfaces.Carrinho;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.vendramel.vendramel.base.domain.interfaces.CarrinhoInterface;
import com.vendramel.vendramel.base.domain.entidades.Carrinho;
import com.vendramel.vendramel.base.infra.repository.Carrinho.CarrinhoJpaRepository;
import com.vendramel.vendramel.base.infra.repository.ItemCarrinho.ItemCarrinhoJpaRepository;
import com.vendramel.vendramel.base.infra.mapper.Carrinho.CarrinhoMapper;


@Repository
@Transactional
public class CarrinhoImp implements CarrinhoInterface {

    private final CarrinhoJpaRepository carrinhoRepository;
    private final ItemCarrinhoJpaRepository itemCarrinhoRepository;

    public CarrinhoImp(
            CarrinhoJpaRepository carrinhoRepository,
            ItemCarrinhoJpaRepository itemCarrinhoRepository) {

        this.carrinhoRepository = carrinhoRepository;
        this.itemCarrinhoRepository = itemCarrinhoRepository;
    }

    @Override
    public Carrinho visualizarCarrinho(int idCliente) {

        var existente = carrinhoRepository.findByCliente_Id(idCliente);

        if (existente.isEmpty()) {
            // Cliente ainda não adicionou nenhum livro: carrinho vazio
            Carrinho vazio = new Carrinho();
            vazio.setItens(new ArrayList<>());
            vazio.setSubtotal(BigDecimal.ZERO);
            return vazio;
        }

        var carrinho = existente.get();

        carrinho.setItens(
            itemCarrinhoRepository.findAllByCarrinho_Id(carrinho.getId())
        );

        return CarrinhoMapper.paraDominio(carrinho);
    }

    @Override
    public Carrinho alterarQuantidade(int idItem, int quantidade) {

        if (quantidade <= 0) {
            throw new IllegalArgumentException(
                "A quantidade deve ser maior que zero."
            );
        }

        var item = itemCarrinhoRepository.findById(idItem)
                .orElseThrow(() ->
                    new IllegalArgumentException("Item não encontrado."));

        item.setQuantidade(quantidade);

        itemCarrinhoRepository.save(item);

        return visualizarCarrinho(item.getCarrinho().getCliente().getId());
    }

    @Override
    public void removerItem(int idItem) {

        var item = itemCarrinhoRepository.findById(idItem)
                .orElseThrow(() ->
                    new IllegalArgumentException("Item não encontrado."));

        itemCarrinhoRepository.delete(item);
    }

    @Override
    public void destruirItens(int idCarrinho) {

        itemCarrinhoRepository.excluirPorCarrinhoId(idCarrinho);

        itemCarrinhoRepository.flush();

    }
    
}
