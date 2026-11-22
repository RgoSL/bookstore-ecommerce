package com.vendramel.vendramel.base.infra.mapper.Carrinho;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.vendramel.vendramel.base.domain.entidades.ItemCarrinho;
import com.vendramel.vendramel.base.infra.mapper.ItemCarrinho.ItemCarrinhoMapper;

public class CarrinhoMapper {

    public static com.vendramel.vendramel.base.domain.entidades.Carrinho paraDominio(
            com.vendramel.vendramel.base.infra.entidadesFrameWork.Carrinho carrinho) {

        if (carrinho == null) {
            return null;
        }

        var dominio = new com.vendramel.vendramel.base.domain.entidades.Carrinho();
        dominio.setId(carrinho.getId());
        dominio.setDataCriacao(carrinho.getDataCriacao());

        List<ItemCarrinho> itens = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        if (carrinho.getItens() != null) {
            for (var itemPersistido : carrinho.getItens()) {
                ItemCarrinho item = ItemCarrinhoMapper.paraDominio(itemPersistido);
                itens.add(item);

                if (item.getPrecoUnitario() != null) {
                    subtotal = subtotal.add(
                            item.getPrecoUnitario().multiply(
                                    BigDecimal.valueOf(item.getQuantidade())));
                }
            }
        }

        dominio.setItens(itens);
        dominio.setSubtotal(subtotal);

        return dominio;
    }
}
